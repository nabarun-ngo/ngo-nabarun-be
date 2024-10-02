package ngo.nabarun.app.businesslogic.implementation;

import java.net.URL;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.ICommonBL;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail.WorkDetailFilter;
import ngo.nabarun.app.businesslogic.domain.CommonDO;
import ngo.nabarun.app.businesslogic.domain.DonationDO;
import ngo.nabarun.app.businesslogic.domain.RequestDO;
import ngo.nabarun.app.businesslogic.domain.UserDO;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.RefDataType;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.enums.TriggerEvent;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;

@Slf4j
@Service
public class CommonBLImpl implements ICommonBL {
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private BusinessDomainHelper businessHelper;

	@Autowired
	private CommonDO commonDO;
	
	@Autowired
	private DonationDO donationDO;
	
	@Autowired
	private UserDO userDO;
	
	@Autowired
	private RequestDO requestDO;
	
	@Override
	public void uploadDocuments(MultipartFile[] files, String docIndexId, DocumentIndexType docIndexType) throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notNull(docIndexId,"docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");
		
		for (MultipartFile file : files) {
			DocumentDetailUpload doc= new DocumentDetailUpload();
			doc.setContent(file.getBytes());
			doc.setContentType(file.getContentType());
			doc.setOriginalFileName(file.getOriginalFilename());
			commonDO.uploadDocument(doc, docIndexId, docIndexType);
		}
	}
	
	@Override
	public void uploadDocuments(List<DocumentDetailUpload> files,String docIndexId, DocumentIndexType docIndexType) throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notEmpty(files,"Files cannot be empty !");
		Assert.notNull(docIndexId,"docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");
		
		for (DocumentDetailUpload file : files) {
			commonDO.uploadDocument(file, docIndexId, docIndexType);
		}		
	}

	@Override
	public URL getDocumentUrl(String docId) throws Exception {
		Assert.notNull(docId,"docId must not be null !");
		return commonDO.getDocumentUrl(docId);
	}
	
	@Override
	public boolean deleteDocument(String docId) throws Exception {
		Assert.notNull(docId,"docId must not be null !");
		return commonDO.deleteDocument(docId);
	}

	@Override
	public void clearSystemCache(List<String> names) {
		if(names == null || names.size() == 0) {
			System.out.println(cacheManager.getCacheNames());
			cacheManager.getCacheNames().stream().forEach(name->cacheManager.getCache(name).clear());
		}else {
			names.stream().forEach(name->cacheManager.getCache(name).clear());
		}
	}
	
	@Override
	public Map<String,List<KeyValue>> getReferenceData(List<RefDataType> names,Map<String,String> attr) throws Exception {
		Map<String,List<KeyValue>> obj=new HashMap<>();
		if(names == null || names.contains(RefDataType.DONATION)) {
			DonationType type=null;
			DonationStatus currStatus=null;
			if(attr != null && attr.containsKey("donationType") && attr.containsKey("currentDonationStatus")) {
				type=DonationType.valueOf(attr.get("donationType"));
				currStatus=DonationStatus.valueOf(attr.get("currentDonationStatus"));
			}
			obj.putAll(businessHelper.getDonationRefData(type, currStatus));
		}
		
		if(names == null || names.contains(RefDataType.USER)) {
			String stateCode=null;
			String countryCode=null;
			if(attr != null && attr.containsKey("countryCode")) {
				countryCode=attr.get("countryCode");
			}
			if(attr != null && attr.containsKey("countryCode") && attr.containsKey("stateCode")) {
				stateCode=attr.get("stateCode");
				countryCode=attr.get("countryCode");
			}
			obj.putAll(businessHelper.getUserRefData(countryCode,stateCode));
		}
		if(names == null || names.contains(RefDataType.ACCOUNT)) {
			obj.putAll(businessHelper.getAccountRefData());
		}
		if(names == null || names.contains(RefDataType.WORKFLOW)) {
			RequestType type=null;
			if(attr != null && attr.containsKey("workflowType")) {
				type=RequestType.valueOf(attr.get("workflowType"));
			}
			obj.putAll(businessHelper.getWorkflowRefData(type));
		}
		return obj;
	}
	
	@Override
	public Map<String,List<KeyValue>> getReferenceData(List<RefDataType> names) throws Exception {
		return getReferenceData(names,null);
	}
	
	@Override
	public List<AdditionalField> getReferenceFields(String identifier) throws Exception {
		return businessHelper.findAddtlFieldDTOList(identifier).stream().filter(f->!f.isHidden()).map(BusinessObjectConverter::toAdditionalField).collect(Collectors.toList());
	}

	@Override
	public Paginate<Map<String,String>> getNotifications(Integer pageIndex, Integer pageSize) {
		return commonDO.getNotifications(pageIndex, pageSize);
	}


	@Override
	public void manageNotification(String action,Map<String, Object> payload) throws Exception {
		String userId= SecurityUtils.getAuthUserId();
		switch(action.toUpperCase()) {
		case "SAVE_TOKEN":
			commonDO.saveNotificationToken(userId, payload.get("token").toString());
			break;
		case "DELETE_TOKEN":
			commonDO.removeNotificationToken(userId,payload.get("token").toString());
			break;
		case "UPDATE_NOTIFICATION":
			commonDO.updateNotification(payload.get("id").toString(), payload);
			break;
		}
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
					userDO.syncUserDetail(parameters);
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
