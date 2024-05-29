package ngo.nabarun.app.businesslogic.implementation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.domain.DonationDO;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
@Slf4j
public class DonationBLImpl implements IDonationBL {

	@Autowired
	private IUserInfraService userInfraService;

	@Autowired
	private DonationDO donationDO;
	
	@Autowired
	private GenericPropertyHelper propertyHelper;
	
	@Autowired
	protected BusinessHelper businessHelper;
	
	@Override
	public DonationDetail raiseDonation(DonationDetail donationDetail) throws Exception {
		UserDTO donor;
		if (donationDetail.getIsGuest() != Boolean.TRUE) {
			if (donationDetail.getDonationType() == DonationType.REGULAR
					&& donationDetail.getStartDate().after(donationDetail.getEndDate())) {
				throw new BusinessException("StartDate cannot be after end date");
			}
			if (donationDetail.getDonationType() == DonationType.REGULAR
					&& donationDO.checkIfDonationRaised(donationDetail.getDonorDetails().getId(), donationDetail.getStartDate(),
							donationDetail.getEndDate())) {
				throw new BusinessException(BusinessExceptionMessage.DONATION_ALREADY_RAISED.getMessage());
			}
			donor = userInfraService.getUser(donationDetail.getDonorDetails().getId(), IdType.ID, false);
			if (!donor.getAdditionalDetails().isActiveContributor()) {
				throw new BusinessException("Donor is not active");
			}
		}

		return donationDO.raiseDonation(donationDetail);
	}

	@Override
	public void autoRaiseDonation() {

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date startDate = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		Date endDate = cal.getTime();

		List<UserDTO> users = userInfraService.getUsers(null, null, null).getContent();
		for (UserDTO user : users) {

			if (user.getAdditionalDetails().isActiveContributor()
					&& !CommonUtils.isCurrentMonth(user.getAdditionalDetails().getCreatedOn())
					&& !donationDO.checkIfDonationRaised(user.getProfileId(), startDate, endDate)) {
				
				DonationDetail donationDetail = new DonationDetail();
				donationDetail.setDonorDetails(BusinessObjectConverter.toUserDetail(user));
				donationDetail.setEndDate(endDate);
				donationDetail.setIsGuest(false);
				donationDetail.setStartDate(startDate);
				donationDetail.setDonationType(DonationType.REGULAR);
				try {
					donationDetail =donationDO.raiseDonation(donationDetail);
				} catch (Exception e) {
					log.error("Exception occured during automatic donation creation ",e);
				}
				log.info("Automatically raised donation id : " + donationDetail.getId());
			}
		}

	}

	

	@Override
	public Paginate<DonationDetail> getUserDonations(String id, Integer index, Integer size) throws Exception {
		return donationDO.retrieveUserDonations(index, size, id, IdType.ID);
	}

	@Override
	public Paginate<DonationDetail> getLoggedInUserDonations(Integer index, Integer size) throws Exception {
		String userId=propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return donationDO.retrieveUserDonations(index, size, userId, IdType.AUTH_USER_ID);
	}

	@Override
	public Paginate<DonationDetail> getDonations(Integer index, Integer size, DonationDetailFilter filter) {
		return donationDO.retrieveDonations(index, size, filter);
	}

	@Override
	public List<DocumentDetail> getDonationDocument(String donationId) {
		return donationDO.retrieveDonationDocument(donationId);
	}

	@Override
	public DonationDetail updateDonation(String id, DonationDetail request) throws Exception {
		/*
		 * Donation amount cannot be changed while updating status as paid
		 */
		if (request.getDonationStatus() == DonationStatus.PAID && request.getAmount() != null) {
			throw new BusinessException("Amount cannot be changed while updating status as PAID.");
		}
		DonationDetail donation = donationDO.retrieveDonation(id);
		/**
		 * Do not allow to update amount if resolved
		 */
		if (businessHelper.isResolvedDonation(donation.getDonationStatus())) {
			throw new BusinessException("No updates are allowed on settled donations.");
		}
		
		AccountDetail accDetail= donationDO.retrieveAccount(request.getReceivedAccount().getId());
		if (accDetail.getAccountStatus() == AccountStatus.INACTIVE) {
			throw new BusinessException("Donation cannot be paid to an Inactive account.");
		}
		return donationDO.updateDonation(donation, request, id, accDetail);
	}

	@Override
	public DonationDetail updatePaymentInfo(String id, DonationDetail request) throws Exception {
//		DonationDTO donation = donationInfraService.getDonation(id);
//		/**
//		 * Do not allow to update amount if resolved
//		 */
//		if (businessHelper.isResolvedDonation(donation.getStatus())) {
//			throw new BusinessException("No updates are allowed on settled donations.");
//		}
//		DonationDTO updatedDetail = new DonationDTO();
//		if (request.isPaymentNotified()) {
//			updatedDetail.setPaymentNotificationDate(CommonUtils.getSystemDate());
//			updatedDetail.setIsPaymentNotified(request.isPaymentNotified());
//		}
//		donation = donationInfraService.updateDonation(id, updatedDetail);
		return BusinessObjectConverter.toDonationDetail(null, null, null);
	}

	@Override
	public DonationSummary getDonationSummary(String id, List<String> fields) throws Exception {
		
		//TODO
		return donationDO.retrieveDonationSummary(id, false);
	}

}
