package ngo.nabarun.app.businesslogic.implementation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary.PayableAccDetail;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.AccountType;
import ngo.nabarun.app.common.enums.AdditionalFieldSource;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AccountDTO.AccountDTOFilter;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.DonationDTO.DonationDTOFilter;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.service.IAccountInfraService;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IDonationInfraService;
import ngo.nabarun.app.infra.service.ITransactionInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
@Slf4j
public class DonationBLImpl implements IDonationBL {

	@Autowired
	private IUserInfraService userInfraService;

	@Autowired
	private IDonationInfraService donationInfraService;

	@Autowired
	private IDocumentInfraService documentInfraService;

	@Autowired
	private ITransactionInfraService transactionInfraService;

	@Autowired
	private IAccountInfraService accountInfraService;

	@Autowired
	private GenericPropertyHelper propertyHelper;

	@Autowired
	private BusinessHelper businessHelper;

	@Override
	public DonationDetail raiseDonation(DonationDetail donationDetail) throws Exception {
		DonationDTO donationDTO = new DonationDTO();
		/**
		 * Custom field
		 */
		if (donationDetail.getAdditionalFields() != null) {
			List<FieldDTO> fieldDTO = new ArrayList<>();
			for (AdditionalField addfield : donationDetail.getAdditionalFields()) {
				fieldDTO.add(businessHelper.findAddtlFieldAndConvertToFieldDTO(AdditionalFieldSource.DONATION,addfield));
			}
			donationDTO.setAdditionalFields(fieldDTO);
		}
		UserDTO donor = new UserDTO();

		if (donationDetail.getIsGuest() == Boolean.TRUE) {
			donationDTO.setId(businessHelper.generateDonationId());
			donationDTO.setAmount(donationDetail.getAmount());
			donationDTO.setType(DonationType.ONETIME);
			donationDTO.setGuest(true);
			donationDTO.setStatus(DonationStatus.RAISED);
			donor.setName(donationDetail.getDonorDetails().getFullName());
			donor.setEmail(
					donationDetail.getDonorDetails() == null ? null : donationDetail.getDonorDetails().getEmail());
			donor.setPhoneNumber(donationDetail.getDonorDetails() == null ? null
					: donationDetail.getDonorDetails().getPrimaryNumber());
			donationDTO.setDonor(donor);
			donationDTO.setForEventId(donationDetail.getEvent() == null ? null : donationDetail.getEvent().getId());
			donationDTO = donationInfraService.createDonation(donationDTO);

		} else {
			if (donationDetail.getDonationType() == DonationType.REGULAR
					&& donationDetail.getStartDate().after(donationDetail.getEndDate())) {
				throw new BusinessException("StartDate cannot be after end date");
			}
			if (donationDetail.getDonationType() == DonationType.REGULAR
					&& checkIfDonationRaised(donationDetail.getDonorDetails().getId(), donationDetail.getStartDate(),
							donationDetail.getEndDate())) {
				throw new BusinessException(BusinessExceptionMessage.DONATION_ALREADY_RAISED.getMessage());
			}
			donor = userInfraService.getUser(donationDetail.getDonorDetails().getId(), IdType.ID, false);
			if (!donor.getAdditionalDetails().isActiveContributor()) {
				throw new BusinessException("Donor is not active");
			}

			donationDTO.setId(businessHelper.generateDonationId());
			donationDTO.setAmount(donationDetail.getAmount());
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
			donationDTO.setStatus(DonationStatus.RAISED);
			donationDTO.setType(donationDetail.getDonationType());
			donationDTO = donationInfraService.createDonation(donationDTO);
			donationDTO.setDonor(donor);
		}
		CorrespondentDTO recipient = CorrespondentDTO.builder().name(donor.getName())
				.emailRecipientType(EmailRecipientType.TO).email(donor.getEmail()).mobile(donor.getPhoneNumber())
				.build();
		businessHelper.sendEmail(BusinessConstants.EMAILTEMPLATE__DONATION_CREATE_REGULAR, List.of(recipient),
				Map.of("user", donor, "donation", donationDTO));
		return DTOToBusinessObjectConverter.toDonationDetail(donationDTO, null, null);
	}

	@Override
	public void autoRaiseDonation() {
		Double amount = 120.0;

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date startDate = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date endDate = cal.getTime();

		List<UserDTO> users = userInfraService.getUsers(null, null, null).getContent();
		for (UserDTO user : users) {

			if (user.getAdditionalDetails().isActiveContributor()
					&& !CommonUtils.isCurrentMonth(user.getAdditionalDetails().getCreatedOn())
					&& !checkIfDonationRaised(user.getProfileId(), startDate, endDate)) {
				DonationDTO donationDTO = new DonationDTO();
				donationDTO.setId(businessHelper.generateDonationId());
				donationDTO.setAmount(amount);
				donationDTO.setDonor(user);
				donationDTO.setEndDate(endDate);
				donationDTO.setGuest(false);
				donationDTO.setStartDate(startDate);
				donationDTO.setStatus(DonationStatus.RAISED);
				donationDTO.setType(DonationType.REGULAR);
				donationDTO = donationInfraService.createDonation(donationDTO);
				log.info("Automatically raised donation id : " + donationDTO.getId());
			}
		}

	}

	private boolean checkIfDonationRaised(String userId, Date startDate, Date endDate) {
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

	@Override
	public Paginate<DonationDetail> getUserDonations(String id, Integer index, Integer size) throws Exception {
		UserDTO userDTO = userInfraService.getUser(id, IdType.ID, false);
		DonationDTOFilter filterDTO = new DonationDTOFilter();
		filterDTO.setDonorId(userDTO.getProfileId());
		Page<DonationDetail> donationPage = donationInfraService.getDonations(index, size, filterDTO).map(m -> {
			m.setDonor(userDTO);
			return DTOToBusinessObjectConverter.toDonationDetail(m);
		});
		return new Paginate<DonationDetail>(donationPage);
	}

	@Override
	public Paginate<DonationDetail> getLoggedInUserDonations(Integer index, Integer size) throws Exception {
		if (SecurityUtils.isAuthenticated()) {
			UserDTO userDTO = userInfraService
					.getUser(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
							: SecurityUtils.getAuthUserId(), IdType.AUTH_USER_ID, false);
			DonationDTOFilter filterDTO = new DonationDTOFilter();
			filterDTO.setDonorId(userDTO.getProfileId());
			Page<DonationDetail> donationPage = donationInfraService.getDonations(index, size, filterDTO).map(m -> {
				m.setDonor(userDTO);
				return DTOToBusinessObjectConverter.toDonationDetail(m);
			});
			return new Paginate<DonationDetail>(donationPage);
		}
		return null;
	}

	@Override
	public Paginate<DonationDetail> getDonations(Integer index, Integer size, DonationDetailFilter filter) {
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
		}
		Page<DonationDetail> page = donationInfraService.getDonations(index, size, filterDTO)
				.map(DTOToBusinessObjectConverter::toDonationDetail);
		return new Paginate<DonationDetail>(page);
	}

	@Override
	public List<DocumentDetail> getDonationDocument(String donationId) {
		return documentInfraService.getDocumentList(donationId, DocumentIndexType.DONATION).stream().map(m -> {
			DocumentDetail doc = new DocumentDetail();
			doc.setDocId(m.getDocId());
			doc.setDocumentIndexId(donationId);
			doc.setImage(m.isImage());
			doc.setOriginalFileName(m.getOriginalFileName());
			return doc;
		}).toList();
	}

	@Override
	public DonationDetail updateDonation(String id, DonationDetail request) throws Exception {
		/*
		 * Donation amount cannot be changed while updating status as paid
		 */
		if (request.getDonationStatus() == DonationStatus.PAID && request.getAmount() != null) {
			throw new BusinessException("Amount cannot be changed while updating status as PAID.");
		}
		DonationDTO donation = donationInfraService.getDonation(id);
		/**
		 * Do not allow to update amount if resolved
		 */
		if (businessHelper.isResolvedDonation(donation.getStatus())) {
			throw new BusinessException("No updates are allowed on settled donations.");
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

		if (request.getDonationStatus() == DonationStatus.PAID) {
			updatedDetail.setPaidOn(request.getPaidOn());
			updatedDetail.setPaymentMethod(request.getPaymentMethod());
			updatedDetail
					.setUpiName(request.getPaymentMethod() == PaymentMethod.UPI ? request.getPaidUsingUPI() : null);

			UserDTO auth_user = userInfraService
					.getUser(propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
							: SecurityUtils.getAuthUserId(), IdType.AUTH_USER_ID, false);
			updatedDetail.setConfirmedBy(auth_user);
			updatedDetail.setConfirmedOn(CommonUtils.getSystemDate());

			/*
			 * Checking for any existing transactions created against the donation id if one
			 * or more transaction exists then checking if any of the transaction status is
			 * success or not create a transaction with success status if none of then is
			 * success
			 */
			List<TransactionDTO> oldTxns = transactionInfraService.getTransactions(donation.getId(),
					TransactionRefType.DONATION);
			boolean isAnySuccessTxn = false;
			for (TransactionDTO oldTxn : oldTxns) {
				if (oldTxn.getTxnStatus() == TransactionStatus.SUCCESS) {
					isAnySuccessTxn = true;
					break;
				}
			}
			if (!isAnySuccessTxn) {
				/**
				 * check validity of account id
				 */
				AccountDTO accDTO = accountInfraService.getAccountDetails(request.getReceivedAccount().getId());
				if (accDTO.getAccountStatus() == AccountStatus.INACTIVE) {
					throw new BusinessException("Donation cannot be paid to an Inactive account.");
				}
				TransactionDTO newTxn = new TransactionDTO();
				newTxn.setId(businessHelper.generateTransactionId());
				newTxn.setToAccount(accDTO);
				newTxn.setTxnAmount(donation.getAmount());
				newTxn.setTxnDate(request.getPaidOn());
				newTxn.setTxnRefId(donation.getId());
				newTxn.setTxnRefType(TransactionRefType.DONATION);
				newTxn.setTxnStatus(TransactionStatus.SUCCESS);
				newTxn.setTxnType(TransactionType.IN);
				newTxn.setTxnDescription("Donation amount for id " + donation.getId());

				newTxn = transactionInfraService.createTransaction(newTxn);
				updatedDetail.setTransactionRefNumber(newTxn.getId());
				updatedDetail.setPaidToAccount(accDTO);
				updatedDetail.setComment(request.getRemarks());
			}
		} else if (request.getDonationStatus() == DonationStatus.PAYMENT_FAILED) {
			TransactionDTO newTxn = new TransactionDTO();
			newTxn.setId(businessHelper.generateTransactionId());
			newTxn.setTxnAmount(donation.getAmount());
			newTxn.setTxnDate(request.getPaidOn());
			newTxn.setTxnRefId(donation.getId());
			newTxn.setTxnRefType(TransactionRefType.DONATION);
			newTxn.setTxnStatus(TransactionStatus.FAILURE);
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setComment(request.getPaymentFailureDetail());
			newTxn.setTxnDescription("Donation amount for id " + donation.getId());
			newTxn = transactionInfraService.createTransaction(newTxn);
			updatedDetail.setTransactionRefNumber(newTxn.getId());
			updatedDetail.setPaymentFailDetail(request.getPaymentFailureDetail());

		} else if (request.getDonationStatus() == DonationStatus.PAY_LATER) {
			updatedDetail.setPayLaterReason(request.getLaterPaymentReason());
		} else if (request.getDonationStatus() == DonationStatus.CANCELLED) {
			updatedDetail.setCancelReason(request.getCancelletionReason());
		}
		donation = donationInfraService.updateDonation(id, updatedDetail);
		return DTOToBusinessObjectConverter.toDonationDetail(donation, null, null);
	}

	@Override
	public DonationDetail updatePaymentInfo(String id, DonationDetail request) throws Exception {
		DonationDTO donation = donationInfraService.getDonation(id);
		/**
		 * Do not allow to update amount if resolved
		 */
		if (businessHelper.isResolvedDonation(donation.getStatus())) {
			throw new BusinessException("No updates are allowed on settled donations.");
		}
		DonationDTO updatedDetail = new DonationDTO();
		if (request.isPaymentNotified()) {
			updatedDetail.setPaymentNotificationDate(CommonUtils.getSystemDate());
			updatedDetail.setIsPaymentNotified(request.isPaymentNotified());
		}
		donation = donationInfraService.updateDonation(id, updatedDetail);
		return DTOToBusinessObjectConverter.toDonationDetail(donation, null, null);
	}

	@Override
	public DonationSummary getDonationSummary(String id, List<String> fields) throws Exception {
		List<DonationStatus> outStatus = businessHelper.getOutstandingDonationStatus();
		DonationDTOFilter filterDTO = new DonationDTOFilter();
		filterDTO.setDonorId(id);
		filterDTO.setDonationStatus(outStatus);
		List<DonationDTO> outDons = donationInfraService.getDonations(null, null, filterDTO).getContent();
		DonationSummary dsmry = new DonationSummary();

		dsmry.setOutstandingAmount(outDons.stream().mapToDouble(DonationDTO::getAmount).sum());
		dsmry.setHasOutstanding(dsmry.getOutstandingAmount() > 0);

		if (fields.contains(BusinessConstants.INCLUDE_OUTSTANDING_MONTHS)) {
			List<String> months = new ArrayList<>();
			for (DonationDTO outDon : outDons) {
				if (outDon.getType() != DonationType.ONETIME) {
					months.addAll(CommonUtils.getMonthsBetween(outDon.getStartDate(), outDon.getEndDate()));
				}
			}
			dsmry.setOutstandingMonths(months);
		}

		if (fields.contains(BusinessConstants.INCLUDE_PAYABLE_ACCOUNT)) {
			AccountDTOFilter filter = new AccountDTOFilter();
			filter.setAccountStatus(List.of(AccountStatus.ACTIVE));
			filter.setAccountType(List.of(AccountType.DONATION));
			dsmry.setPayableAccounts(
					accountInfraService.getAccounts(null, null, filter).getContent().stream().map(m -> {
						PayableAccDetail pad = new PayableAccDetail();
						pad.setPayableBankDetails(DTOToBusinessObjectConverter.toBankDetail(m.getBankDetail()));
						pad.setPayableUPIDetail(DTOToBusinessObjectConverter.toUPIDetail(m.getUpiDetail()));
						return pad;
					}).collect(Collectors.toList()));
		}

		return dsmry;
	}

}
