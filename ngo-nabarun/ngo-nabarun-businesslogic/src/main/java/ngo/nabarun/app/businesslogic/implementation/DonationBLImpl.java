package ngo.nabarun.app.businesslogic.implementation;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.IDonationBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Page;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.exception.BusinessExceptionMessage;
import ngo.nabarun.app.businesslogic.helper.BusinessEmailHelper;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.helper.GenericMockDataHelper;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IDonationInfraService;
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
	private BusinessEmailHelper emailHelperService;
	
	@Autowired
	private GenericMockDataHelper mockHelper;
	
	@Autowired 
	private GenericPropertyHelper propertyHelper;

	@Override
	public DonationDetail raiseDonation(DonationDetailCreate donationDetail) throws Exception {
		DonationDTO donationDTO;
		if (donationDetail.getIsGuest() == Boolean.TRUE) {
			donationDTO = new DonationDTO();
			donationDTO.setAmount(donationDetail.getAmount());
			donationDTO.setType(DonationType.ONETIME);
			donationDTO.setGuest(true);
			donationDTO.setStatus(DonationStatus.RAISED);
			UserDTO donor = new UserDTO(); 
			donor.setFirstName(donationDetail.getDonorName());
			donor.setEmail(donationDetail.getDonorEmail());
			donor.setPrimaryPhoneNumber(donationDetail.getDonorMobile());
			donationDTO.setDonor(donor);
			donationDTO.setForEventId(donationDetail.getEventId());
			donationDTO = donationInfraService.createDonation(donationDTO);
		} else {
			UserDTO donor = userInfraService.getUserByProfileId(donationDetail.getDonorId(), false);
			if (!donor.getAdditionalDetails().isActiveContributor()) {
				throw new BusinessException(BusinessExceptionMessage.INACTIVE_DONOR.getMessage());
			}
			if (donationDetail.getDonationType() == DonationType.REGULAR && checkIfDonationRaised(donationDetail.getDonorId(),
					donationDetail.getStartDate(), donationDetail.getEndDate())) {
				throw new BusinessException(BusinessExceptionMessage.DONATION_ALREADY_RAISED.getMessage());
			}
			donationDTO = new DonationDTO();
			donationDTO.setAmount(donationDetail.getAmount());
			donationDTO.setDonor(donor);
			donationDTO.setEndDate(donationDetail.getEndDate());
			donationDTO.setForEventId(
					(donationDetail.getEventId() != null && donationDetail.getDonationType() == DonationType.ONETIME)
							? donationDetail.getEventId()
							: null);
			donationDTO.setGuest(false);
			donationDTO.setStartDate(donationDetail.getStartDate());
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

		List<UserDTO> users = userInfraService.getUsers();
		for (UserDTO user : users) {

			if (user.getAdditionalDetails().isActiveContributor()
					&& !CommonUtils.isCurrentMonth(user.getAdditionalDetails().getCreatedOn())
					&& !checkIfDonationRaised(user.getProfileId(), startDate, endDate)) {
				DonationDTO donationDTO = new DonationDTO();
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
		List<DonationDTO> donations = donationInfraService.getUserDonations(userId);
		return donations.stream().filter(c -> c.getStatus() != DonationStatus.CANCELLED)
				.anyMatch(contribution -> contribution.getType() == DonationType.REGULAR
						&& (startDate.compareTo(contribution.getEndDate()) < 0
								|| startDate.compareTo(contribution.getEndDate()) == 0)
						&& (endDate.compareTo(contribution.getStartDate()) > 0
								|| endDate.compareTo(contribution.getStartDate()) == 0));
	}

	@Override
	public List<DonationDetail> getUserDonations(String id) throws Exception {
		UserDTO userDTO = userInfraService.getUserByProfileId(id, false);
		return donationInfraService.getUserDonations(id).stream().map(m -> {
			m.setDonor(userDTO);
			return DTOToBusinessObjectConverter.toDonationDetail(m, null, null);
		}).collect(Collectors.toList());
	}

	@Override
	public List<DonationDetail> getLoggedInUserDonations() throws Exception {
		if (SecurityUtils.isAuthenticated()) {
			UserDTO userDTO = userInfraService.getUserByUserId(propertyHelper.isTokenMockingEnabledForTest() ? mockHelper.getAuthUserId() : SecurityUtils.getAuthUserId() , false);
			return donationInfraService.getUserDonations(userDTO.getProfileId()).stream().map(m -> {
				m.setDonor(userDTO);
				return DTOToBusinessObjectConverter.toDonationDetail(m, null, null);
			}).collect(Collectors.toList());
		} else {
			throw new BusinessException(BusinessExceptionMessage.USER_AUTH_NEEDED.getMessage());
		}

	}

	@Override
	public Page<DonationDetail> getDonations(Integer index,Integer size, DonationDetailFilter filter) {
		DonationDTO filterDTO= null;
		if(filter != null) {
			filterDTO = new DonationDTO();
			filterDTO.setAccountId(filter.getAccountId());
			filterDTO.setGuest(filter.getIsGuest());
			filterDTO.setEndDate(filter.getEndDate());
			filterDTO.setStartDate(filter.getStartDate());
			filterDTO.setStatus(filter.getDonationStatus());
			filterDTO.setType(filter.getDonationType());
			filterDTO.setPaymentMethod(filter.getPaymentMethod());
		}
		List<DonationDetail> content =donationInfraService.getDonations(index, size, filterDTO).stream()
				.map(m -> DTOToBusinessObjectConverter.toDonationDetail(m, null, null))
				.collect(Collectors.toList());
		long total;
		if(index != null && size != null){
			total=donationInfraService.getDonationsCount();
		}else {
			total = content.size();
		}
		return new Page<DonationDetail>(index, size, total, content);
	}

	@Override
	public List<DocumentDetail> getDonationDocument(String donationId) {
		return documentInfraService.getDocumentList(donationId, DocumentIndexType.DONATION).stream().map(m -> {
			DocumentDetail doc = new DocumentDetail();
			doc.setDocId(m.getDocId());
			doc.setDocumentRefId(donationId);
			doc.setImage(m.isImage());
			doc.setOriginalFileName(m.getOriginalFileName());
			return doc;
		}).toList();
	}

}
