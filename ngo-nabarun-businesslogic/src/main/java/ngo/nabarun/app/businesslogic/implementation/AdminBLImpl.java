package ngo.nabarun.app.businesslogic.implementation;

import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.IAdminBL;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail.WorkDetailFilter;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.TriggerEvent;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;
@Service
@Slf4j
public class AdminBLImpl extends BaseBLImpl implements IAdminBL {
	
	@Override
	public Map<String, String> generateApiKey(List<String> scopes) {
		return commonDO.generateAPIKey(scopes);
	}


	@Async
	@Override
	public void cronTrigger(List<TriggerEvent> triggers,Map<String,String> parameters) {
		for(TriggerEvent trigger:triggers) {
			try {
				DonationDetailFilter filter;
				switch (trigger) {
				case CREATE_DONATION:
					List<UserDTO> users = userDO.retrieveAllUsers(null, null, new UserDetailFilter()).getContent();
					Calendar cal = Calendar.getInstance();
					cal.setTime(CommonUtils.getSystemDate());
					cal.set(Calendar.DAY_OF_MONTH, 1);
					Date startDate = cal.getTime();
					cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
					Date endDate = cal.getTime();

					for (UserDTO user : users) {
						if (!CommonUtils.isCurrentMonth(user.getAdditionalDetails().getCreatedOn())
								&& !donationDO.checkIfDonationRaised(user.getProfileId(), startDate, endDate)) {
							DonationDetail donationDetail = new DonationDetail();
							donationDetail.setDonorDetails(BusinessObjectConverter.toUserDetail(user,businessHelper.getDomainKeyValues()));
							donationDetail.setEndDate(endDate);
							donationDetail.setIsGuest(false);
							donationDetail.setStartDate(startDate);
							donationDetail.setDonationType(DonationType.REGULAR);
							try {
								DonationDTO donation = donationDO.raiseDonation(donationDetail);
								log.info("Automatically raised donation id : " + donation.getId());
							} catch (Exception e) {
								log.error("Exception occured during automatic donation creation ", e);
							}
							Thread.sleep(2000);
						}
					}
					break;
				case DONATION_REMINDER_EMAIL:
					filter= new DonationDetailFilter();
					filter.setDonationStatus(List.of(DonationStatus.PENDING));
					Map<UserDTO, List<DonationDTO>> pendingDonations=donationDO.retrieveDonations(null, null, filter).getContent().stream().filter(f->f.getGuest() == Boolean.FALSE).collect(Collectors.groupingBy(g->g.getDonor()));
					for(Entry<UserDTO, List<DonationDTO>> donations:pendingDonations.entrySet()) {
						CorrespondentDTO recipient= CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO).email(donations.getKey().getEmail()).name(donations.getKey().getName()).build();
						List<Map<String, Object>> donation_vars=donations.getValue().stream().map(m->{
							try {
								return m.toMap(businessHelper.getDomainKeyValues());
							} catch (Exception e) {}
							return null;
						}).collect(Collectors.toList());
						Map<String, Object> user_vars=donations.getKey().toMap(businessHelper.getDomainKeyValues());
						commonDO.sendEmail(BusinessConstants.EMAILTEMPLATE__DONATION_REMINDER, List.of(recipient),Map.of("donations",donation_vars,"user",user_vars));
					}
					break;
				case UPDATE_DONATION:
					filter= new DonationDetailFilter();
					filter.setDonationType(List.of(DonationType.REGULAR));
					filter.setDonationStatus(List.of(DonationStatus.RAISED));
					List<DonationDTO> raisedDonations=donationDO.retrieveDonations(null, null, filter).getContent();
					for(DonationDTO donation:raisedDonations) {
						DonationDetail updates= new DonationDetail();
						updates.setDonationStatus(DonationStatus.PENDING);
						donationDO.updateDonation(donation.getId(), updates, null);
						Thread.sleep(1000);
					}
					break;
				case SYNC_USERS:
					boolean syncRole=parameters.get("sync_role") == null ? false : parameters.get("sync_role").equalsIgnoreCase("Y");
					userDO.syncUserDetail(syncRole);
					break;
				case TASK_REMINDER_EMAIL:
					WorkDetailFilter workFilter= new  WorkDetailFilter();
					workFilter.setCompleted(false);
				     Map<UserDTO, List<WorkDTO>> groupedByPendingWithUsers = requestDO.retrieveAllWorkItems(null, null, workFilter).getContent().stream()
				             .filter(work -> work.getPendingWithUsers() != null) // Handle null pendingWithUsers lists
				             .flatMap(work -> work.getPendingWithUsers().stream()
				                 .map(user -> new AbstractMap.SimpleEntry<>(user, work))) // Create pairs of UserDTO and WorkDTO
				             .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

					for(Entry<UserDTO, List<WorkDTO>> workitem:groupedByPendingWithUsers.entrySet()) {
						List<Map<String, Object>> task_vars=workitem.getValue().stream().map(m->{
							try {
								return m.toMap(businessHelper.getDomainKeyValues());
							} catch (Exception e) {}
							return null;
						}).collect(Collectors.toList());
						CorrespondentDTO recipient= CorrespondentDTO.builder().emailRecipientType(EmailRecipientType.TO).email(workitem.getKey().getEmail()).name(workitem.getKey().getName()).build();
						Map<String, Object> user_vars=workitem.getKey().toMap(businessHelper.getDomainKeyValues());
						commonDO.sendEmail(BusinessConstants.EMAILTEMPLATE__WORKITEM_REMINDER, List.of(recipient),Map.of("workItems",task_vars,"user",user_vars,"currentDate",CommonUtils.formatDateToString(CommonUtils.getSystemDate(), "dd MMM yyyy", "IST")));}
					break;
				default:
					break;
				}
			}catch (Exception e) {
			}
			
		}	
	}
}
