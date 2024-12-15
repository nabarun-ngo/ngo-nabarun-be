package ngo.nabarun.app.businesslogic.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.HistoryRefType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AccountDTO.AccountDTOFilter;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.DonationDTO.DonationDTOFilter;
import ngo.nabarun.app.infra.service.IDonationInfraService;

@Component
@Slf4j
public class DonationDO extends AccountDO {

	@Autowired
	private IDonationInfraService donationInfraService;

	public Paginate<DonationDTO> retrieveDonations(Integer index, Integer size, DonationDetailFilter filter) {
		DonationDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new DonationDTOFilter();
			filterDTO.setDonationId(filter.getDonationId());
			filterDTO.setDonationStatus(filter.getDonationStatus());
			filterDTO.setDonationType(filter.getDonationType());
			filterDTO.setDonorName(filter.getDonorName());
			filterDTO.setFromDate(filter.getFromDate());
			filterDTO.setToDate(filter.getToDate());
			filterDTO.setIsGuestDonation(filter.getIsGuest());
			filterDTO.setPaidAccountId(filter.getPaidToAccountId());
			filterDTO.setDonorId(filter.getDonorId());	
		}
		Page<DonationDTO> page = donationInfraService.getDonations(index, size, filterDTO);
		return new Paginate<DonationDTO>(page);
	}

	
	public Paginate<DonationDTO> retrieveUserDonations(Integer index, Integer size, String id, IdType idType)
			throws Exception {
		UserDTO userDTO = userInfraService.getUser(id, idType, false);
		DonationDTOFilter filterDTO = new DonationDTOFilter();
		filterDTO.setDonorId(userDTO.getProfileId());
		Page<DonationDTO> donationPage = donationInfraService.getDonations(index, size, filterDTO).map(m -> {
			m.setDonor(userDTO);
			return m;
		});
		return new Paginate<DonationDTO>(donationPage);
	}
	
	public void convertMemberToGuestAndCloseAccount(String id) throws Exception {
		DonationDTOFilter filter = new DonationDTOFilter();
		filter.setDonorId(id);
		List<DonationDTO> donations = donationInfraService.getDonations(null, null, filter).getContent();
		
		Optional<DonationDTO> unResolved=donations.stream().filter(d->{
			try {
				return !businessDomainHelper.isResolvedDonation(d.getStatus());
			} catch (Exception e) {}
			return false;
		}).findFirst();
		businessDomainHelper.throwBusinessExceptionIf(()->unResolved.isPresent(), ExceptionEvent.UNRESOLVED_DONATION_EXISTS);
		
		AccountDTOFilter filterDTO= new AccountDTOFilter();
		filterDTO.setProfileId(id);
		List<AccountDTO> accounts = accountInfraService.getAccounts(null, null, filterDTO).getContent();
		List<AccountDTO> accountsWithBalance = accounts.stream().filter(f->f.getCurrentBalance() != 0.0).collect(Collectors.toList());
		businessDomainHelper.throwBusinessExceptionIf(()-> accountsWithBalance.size() > 0, ExceptionEvent.ACCOUNT_WITH_BALANCE_EXISTS);
		
		
		for(DonationDTO donation:donations) {
			DonationDTO don = new DonationDTO();
			don.setGuest(true);
			don.setComment("Auto converted to guest donation");
			DonationDTO updatedDonation =donationInfraService.updateDonation(donation.getId(), don);
			historyInfraService.logUpdate(HistoryRefType.DONATION, updatedDonation.getId(),SecurityUtils.getAuthUser(),
					donation.toHistoryMap(businessDomainHelper.getDomainKeyValues()),
					updatedDonation.toHistoryMap(businessDomainHelper.getDomainKeyValues()));
			
		}
		
		for(AccountDTO account:accounts) {
			accountInfraService.deleteAccount(account.getId());
		}
	}
	
	
	public void autoRaiseRegularDonation(List<UserDTO> users) throws Exception {

		Calendar cal = Calendar.getInstance();
		cal.setTime(CommonUtils.getSystemDate());
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date startDate = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date endDate = cal.getTime();

		for (UserDTO user : users) {
			if (!CommonUtils.isCurrentMonth(user.getAdditionalDetails().getCreatedOn())
					&& !checkIfDonationRaised(user.getProfileId(), startDate, endDate)) {
				DonationDetail donationDetail = new DonationDetail();
				donationDetail.setDonorDetails(BusinessObjectConverter.toUserDetail(user,businessDomainHelper.getDomainKeyValues()));
				donationDetail.setEndDate(endDate);
				donationDetail.setIsGuest(false);
				donationDetail.setStartDate(startDate);
				donationDetail.setDonationType(DonationType.REGULAR);
				try {
					DonationDTO donation = raiseDonation(donationDetail);
					log.info("Automatically raised donation id : " + donation.getId());
				} catch (Exception e) {
					log.error("Exception occured during automatic donation creation ", e);
				}
				Thread.sleep(2000);
			}
		}

	}

	public DonationDTO raiseDonation(DonationDetail donationDetail) throws Exception {
		DonationDTO donationDTO = businessDomainHelper.convertToDonationDTO(donationDetail);
		
		UserDTO donor;
		if (donationDetail.getIsGuest() == Boolean.TRUE) {
			donor = new UserDTO();
			donationDTO.setId(generateDonationId());
			donationDTO.setAmount(donationDetail.getAmount());
			donationDTO.setType(DonationType.ONETIME);
			donationDTO.setGuest(true);
			//donationDTO.setStatus(DonationStatus.RAISED);
			donor.setName(donationDetail.getDonorDetails().getFullName());
			donor.setEmail(
					donationDetail.getDonorDetails() == null ? null : donationDetail.getDonorDetails().getEmail());
			donor.setPhoneNumber(donationDetail.getDonorDetails() == null ? null
					: donationDetail.getDonorDetails().getPrimaryNumber());
			donationDTO.setDonor(donor);
			donationDTO.setForEventId(donationDetail.getEvent() == null ? null : donationDetail.getEvent().getId());
			donationDTO = donationInfraService.createDonation(donationDTO);

		} else {
			donor = userInfraService.getUser(donationDetail.getDonorDetails().getId(), IdType.ID, false);
			//donor = donationDetail.getDonorDetails();
			UserAdditionalDetailsDTO addnlDet=donor.getAdditionalDetails();
			if (donationDTO.getType() == DonationType.REGULAR && addnlDet.getDonPauseStartDate() != null && addnlDet.getDonPauseEndDate() !=null) {
				Date today= CommonUtils.getSystemDate();
				if(today.after(addnlDet.getDonPauseStartDate()) && today.before(addnlDet.getDonPauseEndDate())) {
					donationDTO.setStatus(DonationStatus.PAY_LATER);
				}
			}
			donationDTO.setId(generateDonationId());
			if(donationDetail.getAmount() != null) {
				donationDTO.setAmount(donationDetail.getAmount());
			}
			donationDTO.setDonor(donor);
			donationDTO.setForEventId(
					donationDetail.getEvent() != null && donationDetail.getDonationType() == DonationType.ONETIME
							? donationDetail.getEvent().getId()
							: null);

			donationDTO.setGuest(false);
			donationDTO.setStartDate(
					donationDetail.getDonationType() == DonationType.REGULAR ? donationDetail.getStartDate() : null);
			donationDTO.setEndDate(
					donationDetail.getDonationType() == DonationType.REGULAR ? donationDetail.getEndDate() : null);
			donationDTO.setType(donationDetail.getDonationType());
			donationDTO.setDonor(donor);
			donationDTO = donationInfraService.createDonation(donationDTO);
			historyInfraService.logCreation(HistoryRefType.DONATION, donationDTO.getId(),SecurityUtils.getAuthUser(),
					donationDTO.toHistoryMap(businessDomainHelper.getDomainKeyValues()));
			
			updateAndSendDashboardCounts(donor.getUserId(), data->{
				Map<String,String> map= new HashMap<>();
				try {
					DonationSummary pendings = retrieveDonationSummary(donor.getProfileId());
					map.put(BusinessConstants.attr_DB_pendingDonationAmount, String.valueOf(pendings.getOutstandingAmount()));
				} catch (Exception e) {
					log.error("Error retrieveing donations",e);
				}
				return map;
			});
		}
		

		CorrespondentDTO recipient = CorrespondentDTO.builder().name(donor.getName())
				.emailRecipientType(EmailRecipientType.TO).email(donor.getEmail()).mobile(donor.getPhoneNumber())
				.build();
		Map<String, Object> donation_vars=donationDTO.toMap(businessDomainHelper.getDomainKeyValues());
		sendEmail(BusinessConstants.EMAILTEMPLATE__DONATION_CREATE_REGULAR, List.of(recipient),Map.of("donation",donation_vars));
		return donationDTO;
	}


	public boolean checkIfDonationRaised(String userId, Date startDate, Date endDate) {
		DonationDTOFilter filterDTO = new DonationDTOFilter();
		filterDTO.setDonorId(userId);
		filterDTO.setDonationType(List.of(DonationType.REGULAR));

		List<DonationDTO> donations = donationInfraService.getDonations(null, null, filterDTO).getContent();
		return donations.stream().filter(c -> c.getStatus() != DonationStatus.CANCELLED)
				.anyMatch(contribution -> (startDate.compareTo(contribution.getEndDate()) < 0
						|| startDate.compareTo(contribution.getEndDate()) == 0)
						&& (endDate.compareTo(contribution.getStartDate()) > 0
								|| endDate.compareTo(contribution.getStartDate()) == 0));

	}

	public DonationDTO updateDonation(String id, DonationDetail request, String loggedInUserId) throws Exception {
		DonationDTO donation = retrieveDonation(id);
		/**
		 * Do not allow to update amount if resolved
		 */
		if (request.getDonationStatus() != DonationStatus.UPDATE_MISTAKE && businessDomainHelper.isResolvedDonation(donation.getStatus())) {
			throw new BusinessException("No updates are allowed on settled donations.");
		}
		AccountDTO paidTo=null;
		if(request.getDonationStatus() == DonationStatus.PAID) {
			paidTo= retrieveAccount(request.getReceivedAccount().getId());
			if (paidTo.getAccountStatus() == AccountStatus.INACTIVE) {
				throw new BusinessException("Donation cannot be paid to an Inactive account.");
			}
		}
		
		DonationDTO updatedDetail = new DonationDTO();
		updatedDetail.setAmount(request.getAmount());
		updatedDetail.setStatus(request.getDonationStatus());
		
		if (donation.getGuest() == Boolean.TRUE) {
			UserDTO donor = new UserDTO();
			donor.setName(request.getDonorDetails() == null ? null : request.getDonorDetails().getFullName());
			donor.setEmail(request.getDonorDetails() == null ? null : request.getDonorDetails().getEmail());
			donor.setPhoneNumber(
					request.getDonorDetails() == null ? null : request.getDonorDetails().getPrimaryNumber());
			updatedDetail.setDonor(donor);
		}

		if (donation.getStatus() != DonationStatus.PAID && request.getDonationStatus() == DonationStatus.PAID) {
			updatedDetail.setAmount(null);// not allowing amount change if status is paid
			updatedDetail.setPaidOn(request.getPaidOn());
			updatedDetail.setPaymentMethod(request.getPaymentMethod());
			updatedDetail
					.setUpiName(request.getPaymentMethod() == PaymentMethod.UPI ? request.getPaidUsingUPI() : null);

			UserDTO auth_user = userInfraService.getUser(loggedInUserId, IdType.AUTH_USER_ID, false);
			updatedDetail.setConfirmedBy(auth_user);
			updatedDetail.setConfirmedOn(CommonUtils.getSystemDate());

			TransactionDTO newTxn = new TransactionDTO();
			newTxn.setToAccount(paidTo);
			newTxn.setTxnAmount(donation.getAmount());
			newTxn.setTxnDate(request.getPaidOn() == null ? donation.getPaidOn(): request.getPaidOn());
			newTxn.setTxnRefId(donation.getId());
			newTxn.setTxnRefType(TransactionRefType.DONATION);
			newTxn.setTxnStatus(TransactionStatus.SUCCESS);
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setTxnDescription("Donation amount for id " + donation.getId());
			TransactionDTO newTxnDet = createTransaction(newTxn,auth_user);

			updatedDetail.setTransactionRefNumber(newTxnDet.getId());

			AccountDTO accDTO = new AccountDTO();
			accDTO.setAccountName(paidTo.getAccountName());
			accDTO.setId(paidTo.getId());
			updatedDetail.setPaidToAccount(accDTO);
			updatedDetail.setComment(request.getRemarks());

		} else if (request.getDonationStatus() == DonationStatus.PAYMENT_FAILED) {
			updatedDetail.setPaymentFailDetail(request.getPaymentFailureDetail());
		} else if (request.getDonationStatus() == DonationStatus.PAY_LATER) {
			updatedDetail.setPayLaterReason(request.getLaterPaymentReason());
		} else if (request.getDonationStatus() == DonationStatus.CANCELLED) {
			updatedDetail.setCancelReason(request.getCancelletionReason());
		}else if (donation.getStatus() == DonationStatus.PAID && request.getDonationStatus() == DonationStatus.UPDATE_MISTAKE) {
			UserDTO auth_user = userInfraService.getUser(loggedInUserId, IdType.AUTH_USER_ID, false);
			revertTransaction(id, TransactionRefType.DONATION, TransactionStatus.SUCCESS,auth_user);
			updatedDetail.setTransactionRefNumber("");
		}
		if (request.isPaymentNotified()) {
			updatedDetail.setPaymentNotificationDate(CommonUtils.getSystemDate());
			updatedDetail.setIsPaymentNotified(request.isPaymentNotified());
		}
		updatedDetail = donationInfraService.updateDonation(donation.getId(), updatedDetail);
		historyInfraService.logUpdate(HistoryRefType.DONATION, updatedDetail.getId(),SecurityUtils.getAuthUser(),
				donation.toHistoryMap(businessDomainHelper.getDomainKeyValues()),
				updatedDetail.toHistoryMap(businessDomainHelper.getDomainKeyValues()));
		boolean isResolved = businessDomainHelper.isResolvedDonation(updatedDetail.getStatus());
		UserDTO user=updatedDetail.getDonor();
		if(user != null && user.getUserId() != null && (isResolved || updatedDetail.getStatus() == DonationStatus.UPDATE_MISTAKE)) {
			updateAndSendDashboardCounts(user.getUserId(), data->{
				Map<String,String> map= new HashMap<>();
				try {
					DonationSummary pendings = retrieveDonationSummary(user.getProfileId());
					map.put(BusinessConstants.attr_DB_pendingDonationAmount, String.valueOf(pendings.getOutstandingAmount()));
				} catch (Exception e) {
					log.error("Error retrieveing donations",e);
				}
				return map;
			});
		}
		return updatedDetail;
	}

	

	public DonationSummary retrieveDonationSummary(String id) throws Exception {
		List<DonationStatus> outStatus = businessDomainHelper.getOutstandingDonationStatus();
		DonationDTOFilter filterDTO = new DonationDTOFilter();
		filterDTO.setDonorId(id);
		filterDTO.setDonationStatus(outStatus);
		List<DonationDTO> outDons = donationInfraService.getDonations(null, null, filterDTO).getContent();
		DonationSummary dsmry = new DonationSummary();

		dsmry.setOutstandingAmount(outDons.stream().mapToDouble(DonationDTO::getAmount).sum());
		dsmry.setHasOutstanding(dsmry.getOutstandingAmount() > 0);

		List<String> months = new ArrayList<>();
		for (DonationDTO outDon : outDons) {
			if (outDon.getType() != DonationType.ONETIME) {
				months.addAll(CommonUtils.getMonthsBetween(outDon.getStartDate(), outDon.getEndDate()));
			}
		}
		dsmry.setOutstandingMonths(months);
		return dsmry;
	}

	public DonationDTO retrieveDonation(String id) {
		return donationInfraService.getDonation(id);
	}

	
	
	

	
}
