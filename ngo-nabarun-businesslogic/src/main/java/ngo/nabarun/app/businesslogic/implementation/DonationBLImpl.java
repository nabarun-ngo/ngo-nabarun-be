package ngo.nabarun.app.businesslogic.implementation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary;
import ngo.nabarun.app.businesslogic.businessobjects.HistoryDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary.PayableAccDetail;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.domain.DonationDO;
import ngo.nabarun.app.businesslogic.domain.UserDO;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountType;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.HistoryRefType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.UserDTO;

@Service
@Slf4j
public class DonationBLImpl extends BaseBLImpl implements IDonationBL {

	@Autowired
	private DonationDO donationDO;

	@Autowired
	private UserDO userDO;

	@Autowired
	private PropertyHelper propertyHelper;
	
	@Autowired
	private BusinessDomainHelper domainHelper;

	@Override
	public DonationDetail raiseDonation(DonationDetail donationDetail) throws Exception {
		if (donationDetail.getIsGuest() != Boolean.TRUE) {
			if (donationDetail.getDonationType() == DonationType.REGULAR
					&& donationDetail.getStartDate().after(donationDetail.getEndDate())) {
				throw new BusinessException("StartDate cannot be after end date");
			}
			if (donationDetail.getDonationType() == DonationType.REGULAR
					&& donationDO.checkIfDonationRaised(donationDetail.getDonorDetails().getId(),
							donationDetail.getStartDate(), donationDetail.getEndDate())) {
				throw new BusinessException(BusinessExceptionMessage.DONATION_ALREADY_RAISED.getMessage());
			}

		}
		DonationDTO donation = donationDO.raiseDonation(donationDetail);
		return BusinessObjectConverter.toDonationDetail(donation);
	}

	@Override
	public void autoRaiseDonation() throws Exception {

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date startDate = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date endDate = cal.getTime();

		List<UserDTO> users = userDO.retrieveAllUsers(null, null, null).getContent();
		for (UserDTO user : users) {

			if (!CommonUtils.isCurrentMonth(user.getAdditionalDetails().getCreatedOn())
					&& !donationDO.checkIfDonationRaised(user.getProfileId(), startDate, endDate)) {

				DonationDetail donationDetail = new DonationDetail();
				donationDetail.setDonorDetails(BusinessObjectConverter.toUserDetail(user,domainHelper.getDomainKeyValues()));
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
			}
		}

	}

	@Override
	public Paginate<DonationDetail> getUserDonations(String id, Integer index, Integer size) throws Exception {
		return donationDO.retrieveUserDonations(index, size, id, IdType.ID)
				.map(BusinessObjectConverter::toDonationDetail);
	}

	@Override
	public Paginate<DonationDetail> getLoggedInUserDonations(Integer index, Integer size) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return donationDO.retrieveUserDonations(index, size, userId, IdType.AUTH_USER_ID)
				.map(BusinessObjectConverter::toDonationDetail);
	}

	@Override
	public Paginate<DonationDetail> getDonations(Integer index, Integer size, DonationDetailFilter filter) {
		return donationDO.retrieveDonations(index, size, filter).map(BusinessObjectConverter::toDonationDetail);
	}

	@Override
	public List<DocumentDetail> getDonationDocument(String donationId) {
		return donationDO.getDocuments(donationId,DocumentIndexType.DONATION).stream().map(BusinessObjectConverter::toDocumentDetail)
				.collect(Collectors.toList());
	}

	@Override
	public DonationDetail updateDonation(String id, DonationDetail request) throws Exception {
		/*
		 * Donation amount cannot be changed while updating status as paid
		 */
		if (request.getDonationStatus() == DonationStatus.PAID && request.getAmount() != null) {
			throw new BusinessException("Amount cannot be changed while updating status as PAID.");
		}
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		DonationDTO donation=donationDO.updateDonation(id, request, userId);
		if( donation.getStatus() == DonationStatus.PAID) {
			List<AdditionalField> addnlField= new ArrayList<>();
			addnlField.add(new AdditionalField(AdditionalFieldKey.remarks, "Donation confirmation completed.",true));
			addnlField.add(new AdditionalField(AdditionalFieldKey.decision, "APPROVE",true));
			closeLinkedWorkItem(donation.getId(),RequestType.CHECK_PAYMENT,addnlField);	
		}
		return BusinessObjectConverter.toDonationDetail(donation);
	}
	

	

	@Override
	public DonationDetail updatePaymentInfo(String id, DonationDetail requestDetail) throws Exception {
		DonationDTO donation = donationDO.updateDonation(id, requestDetail,null);
		if(donation.getIsPaymentNotified()) {
			RequestDTO request = new RequestDTO();
			request.setType(RequestType.CHECK_PAYMENT);
			request.setRefId(donation.getId());
			request.setSystemGenerated(true);
			FieldDTO donationNumber= new FieldDTO();
			donationNumber.setFieldKey(AdditionalFieldKey.info);
			donationNumber.setFieldValue("Donation Id : "+donation.getId());
			request.setAdditionalFields(List.of(donationNumber));
			requestDO.createRequest(request, false, null, (t, u) -> {
				return performWorkflowAction(t, u);
			});
		}
		
		return BusinessObjectConverter.toDonationDetail(donation, null, null);
	}

	@Override
	public DonationSummary getDonationSummary(String id, List<String> fields) throws Exception {
		DonationSummary donationSummary = donationDO.retrieveDonationSummary(id);
		if (fields.stream().anyMatch(BusinessConstants.INCLUDE_PAYABLE_ACCOUNT::equalsIgnoreCase)) {
			List<PayableAccDetail> accounts = donationDO.retrievePayableAccounts(AccountType.DONATION).stream()
					.map(m -> {
						PayableAccDetail payAccount = new PayableAccDetail();
						payAccount.setId(m.getId());
						payAccount.setPayableBankDetails(BusinessObjectConverter.toBankDetail(m.getBankDetail()));
						payAccount.setPayableUPIDetail(BusinessObjectConverter.toUPIDetail(m.getUpiDetail()));
						return payAccount;
					}).collect(Collectors.toList());
			donationSummary.setPayableAccounts(accounts);
		}
		return donationSummary;
	}
	
	@Override
	public List<HistoryDetail> getHistories(String id) throws Exception {
		return commonDO.retrieveHistory(id,HistoryRefType.DONATION).stream().map(BusinessObjectConverter::toHistoryDetail)
				.collect(Collectors.toList());
	}

}
