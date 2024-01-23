package ngo.nabarun.app.businesslogic.implementation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.helper.BusinessEmailHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessIdGenerator;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.DonationDTO.DonationDTOFilter;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.misc.KeyValuePair;
import ngo.nabarun.app.infra.service.IAccountInfraService;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IDomainRefConfigInfraService;
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
	private BusinessEmailHelper emailHelperService;

	@Autowired
	private GenericPropertyHelper propertyHelper;

	@Autowired
	private IDomainRefConfigInfraService domainInfraService;

	@Autowired
	private BusinessIdGenerator idGenerator;

	@Override
	public DonationDetail raiseDonation(DonationDetailCreate donationDetail) throws Exception {
		DonationDTO donationDTO;
		if (donationDetail.getIsGuest() == Boolean.TRUE) {
			donationDTO = new DonationDTO();
			donationDTO.setId(idGenerator.generateDonationId());
			donationDTO.setAmount(donationDetail.getAmount());
			donationDTO.setType(DonationType.ONETIME);
			donationDTO.setGuest(true);
			donationDTO.setStatus(DonationStatus.RAISED);
			UserDTO donor = new UserDTO();
			donor.setName(donationDetail.getDonorName());
			donor.setEmail(donationDetail.getDonorEmail());
			donor.setPhoneNumber(donationDetail.getDonorMobile());
			donationDTO.setDonor(donor);
			donationDTO.setForEventId(donationDetail.getEventId());
			donationDTO = donationInfraService.createDonation(donationDTO);
		} else {
			UserDTO donor = userInfraService.getUser(donationDetail.getDonorId(), false);
			if (!donor.getAdditionalDetails().isActiveContributor()) {
				throw new BusinessException("");
			}
			if (donationDetail.getDonationType() == DonationType.REGULAR && checkIfDonationRaised(
					donationDetail.getDonorId(), donationDetail.getStartDate(), donationDetail.getEndDate())) {
				throw new BusinessException(BusinessExceptionMessage.DONATION_ALREADY_RAISED.getMessage());
			}
			donationDTO = new DonationDTO();
			donationDTO.setId(idGenerator.generateDonationId());
			donationDTO.setAmount(donationDetail.getAmount());
			donationDTO.setDonor(donor);
			donationDTO.setForEventId(
					(donationDetail.getEventId() != null && donationDetail.getDonationType() == DonationType.ONETIME)
							? donationDetail.getEventId()
							: null);
			donationDTO.setGuest(false);
			donationDTO.setStartDate(donationDetail.getDonationType() == DonationType.REGULAR ? donationDetail.getStartDate() : null);
			donationDTO.setEndDate(donationDetail.getDonationType() == DonationType.REGULAR ? donationDetail.getEndDate() : null);
			donationDTO.setStatus(DonationStatus.RAISED);
			donationDTO.setType(donationDetail.getDonationType());
			donationDTO = donationInfraService.createDonation(donationDTO);
			donationDTO.setDonor(donor);
		}
		emailHelperService.sendEmailOnDonationCreate(donationDTO);
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

		List<UserDTO> users = userInfraService.getUsers(null,null,null).getContent();
		for (UserDTO user : users) {

			if (user.getAdditionalDetails().isActiveContributor()
					&& !CommonUtils.isCurrentMonth(user.getAdditionalDetails().getCreatedOn())
					&& !checkIfDonationRaised(user.getProfileId(), startDate, endDate)) {
				DonationDTO donationDTO = new DonationDTO();
				donationDTO.setId(idGenerator.generateDonationId());
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
		UserDTO userDTO = userInfraService.getUser(id, false);
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
			UserDTO userDTO = userInfraService.getUserByUserId(
					propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
							: SecurityUtils.getAuthUserId(),
					false);
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
	public List<KeyValue> getNextDonationStatus(DonationType type, DonationStatus currentStatus) throws Exception {
		List<KeyValuePair> kvPairs = domainInfraService.getDonationConfig().getDonationStatuses();
		for (KeyValuePair kvPair : kvPairs) {
			Map<String, Object> attributes = kvPair.getAttributes();
			if (kvPair.getKey().equalsIgnoreCase(currentStatus.name()) && attributes.get("APPLICABLE_FOR") != null) {
				List<String> applicableList = CommonUtils.convertToType(attributes.get("APPLICABLE_FOR"),
						new TypeReference<List<String>>() {
						});
				if (applicableList.contains(type.name()) && attributes.get("NEXT_STATUS") != null) {
					Map<String, List<String>> nextStatusList = CommonUtils.convertToType(attributes.get("NEXT_STATUS"),
							new TypeReference<Map<String, List<String>>>() {
							});
					List<String> allowedNextStatus = nextStatusList.get(type.name());
					return DTOToBusinessObjectConverter.toKeyValueList(
							kvPairs.stream().filter(f -> allowedNextStatus.contains(f.getKey())).toList());
				}
				break;
			}
		}
		return List.of();
	}
	
	private boolean isResolved(DonationStatus status) {
		return status == DonationStatus.PAID || status == DonationStatus.CANCELLED;
		
	}

	@Override
	public DonationDetail updateDonation(String id, DonationDetailUpdate request) throws Exception {
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
		if(isResolved(donation.getStatus())) {
			throw new BusinessException("No updates are allowed on settled donations.");
		}
		DonationDTO updatedDetail = new DonationDTO();
		updatedDetail.setAmount(request.getAmount());
		updatedDetail.setStatus(request.getDonationStatus());

		if (donation.getGuest() == Boolean.TRUE) {
			UserDTO donor = new UserDTO();
			donor.setName(request.getDonorName());
			donor.setEmail(request.getDonorEmail());
			donor.setPhoneNumber(request.getDonorMobile());
			updatedDetail.setDonor(donor);
		}

		if (request.getDonationStatus() == DonationStatus.PAID) {
			updatedDetail.setPaidOn(request.getPaidOn());
			updatedDetail.setPaymentMethod(request.getPaymentMethod());
			updatedDetail.setUpiName(request.getPaymentMethod() == PaymentMethod.UPI ? request.getPaidUPIName() : null);

			UserDTO auth_user = userInfraService.getUserByUserId(
					propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
							: SecurityUtils.getAuthUserId(),
					false);
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
				AccountDTO accDTO = accountInfraService.getAccountDetails(request.getAccountId());
				if (accDTO.getAccountStatus() == AccountStatus.INACTIVE) {
					throw new BusinessException("Donation cannot be paid to an Inactive account.");
				}
				TransactionDTO newTxn = new TransactionDTO();
				newTxn.setId(idGenerator.generateTransactionId());
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
				updatedDetail.setComment(request.getComment());
			}
		} else if (request.getDonationStatus() == DonationStatus.PAYMENT_FAILED) {
			TransactionDTO newTxn = new TransactionDTO();
			newTxn.setId(idGenerator.generateTransactionId());
			newTxn.setTxnAmount(donation.getAmount());
			newTxn.setTxnDate(request.getPaidOn());
			newTxn.setTxnRefId(donation.getId());
			newTxn.setTxnRefType(TransactionRefType.DONATION);
			newTxn.setTxnStatus(TransactionStatus.FAILURE);
			newTxn.setTxnType(TransactionType.IN);
			newTxn.setComment(request.getComment());
			newTxn.setTxnDescription("Donation amount for id " + donation.getId());

			newTxn = transactionInfraService.createTransaction(newTxn);
			updatedDetail.setTransactionRefNumber(newTxn.getId());
			updatedDetail.setPaymentFailDetail(request.getComment());

		} else if (request.getDonationStatus() == DonationStatus.PAY_LATER) {
			updatedDetail.setPayLaterReason(request.getComment());
		} else if (request.getDonationStatus() == DonationStatus.CANCELLED) {
			updatedDetail.setCancelReason(request.getComment());
		}
		donation = donationInfraService.updateDonation(id, updatedDetail);
		return DTOToBusinessObjectConverter.toDonationDetail(donation, null, null);
	}

}
