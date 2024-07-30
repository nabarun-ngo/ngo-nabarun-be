package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.DonationEntity;
import ngo.nabarun.app.infra.core.entity.QDonationEntity;
import ngo.nabarun.app.infra.core.repo.DonationRepository;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.DonationDTO.DonationDTOFilter;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.WhereClause;
import ngo.nabarun.app.infra.service.IDonationInfraService;

@Service
public class DonationInfraServiceImpl extends BaseServiceImpl implements IDonationInfraService {

	@Autowired
	private DonationRepository donationRepo;

	
	@Override
	public Page<DonationDTO> getDonations(Integer page, Integer size, DonationDTOFilter filter) {
		Page<DonationEntity> donationPage = null;
		Sort sort = Sort.by(Sort.Direction.DESC, "raisedOn");
		if (filter != null) {

			/*
			 * Query building and filter logic
			 */
			QDonationEntity qDonation = QDonationEntity.donationEntity;
			BooleanBuilder query = WhereClause.builder()
					.optionalAnd(filter.getDonationId() != null, () -> qDonation.id.eq(filter.getDonationId()))
					.optionalAnd(filter.getDonorId() != null, () -> qDonation.profile.eq(filter.getDonorId()))
					.optionalAnd(filter.getDonationStatus() != null,
							() -> qDonation.status.in(filter.getDonationStatus().stream().map(m -> m.name()).toList()))
					.optionalAnd(filter.getDonationType() != null,
							() -> qDonation.type.in(filter.getDonationType().stream().map(m -> m.name()).toList()))
					.optionalAnd(filter.getIsGuestDonation() != null,
							() -> qDonation.isGuest.eq(filter.getIsGuestDonation()))
					.optionalAnd(filter.getPaidAccountId() != null,
							() -> qDonation.accountId.eq(filter.getPaidAccountId()))
					.optionalAnd(filter.getDonorName() != null,
							() -> qDonation.donorName.containsIgnoreCase(filter.getDonorName()))
					.optionalAnd(filter.getFromDate() != null && filter.getToDate() != null,
							() -> qDonation.raisedOn.between(filter.getFromDate(), filter.getToDate()))
					.build();

			if (page == null || size == null) {
				List<DonationEntity> result = new ArrayList<>();
				donationRepo.findAll(query, sort).iterator().forEachRemaining(result::add);
				donationPage = new PageImpl<>(result);
			} else {
				donationPage = donationRepo.findAll(query, PageRequest.of(page, size, sort));
			}
		} else if (page != null && size != null) {
			donationPage = donationRepo.findAll(PageRequest.of(page, size, sort));
		} else {
			donationPage = new PageImpl<>(donationRepo.findAll(sort));
		}
		return donationPage.map(InfraDTOHelper::convertToDonationDTO);
	}

	@Override
	public DonationDTO createDonation(DonationDTO donationDTO) {
		DonationEntity donation = new DonationEntity();
		donation.setStartDate(donationDTO.getStartDate());
		donation.setId(donationDTO.getId());
		donation.setAmount(donationDTO.getAmount());
		donation.setStatus(donationDTO.getStatus().name());
		donation.setType(donationDTO.getType().name());
		donation.setDeleted(false);
		donation.setEndDate(donationDTO.getEndDate());
		donation.setEventId(donationDTO.getForEventId());
		donation.setIsGuest(donationDTO.getGuest() == Boolean.TRUE);
		donation.setRaisedOn(CommonUtils.getSystemDate());
		if (donationDTO.getGuest() != Boolean.TRUE) {
			donation.setProfile(donationDTO.getDonor().getProfileId());
		}

		donation.setDonorContactNumber(donationDTO.getDonor().getPhoneNumber());
		donation.setDonorEmailAddress(donationDTO.getDonor().getEmail());
		donation.setDonorName(donationDTO.getDonor().getName());

		donation = donationRepo.save(donation);
		if (donationDTO.getAdditionalFields() != null) {
			for (FieldDTO addfield : donationDTO.getAdditionalFields()) {
				addfield.setFieldSource(donation.getId());
				addOrUpdateCustomField(addfield);
			}
		}

		// create log at firebase
		return InfraDTOHelper.convertToDonationDTO(donation);
	}

	

	@Override
	public DonationDTO getDonation(String donationId) {
		return InfraDTOHelper.convertToDonationDTO(donationRepo.findById(donationId).orElseThrow());
	}

	@Override
	public long getDonationsCount() {
		return donationRepo.count();
	}

	@Override
	public long getDonationsCount(String id) {
		return donationRepo.countByProfile(id);
	}

	@Override
	public DonationDTO updateDonation(String id, DonationDTO donationDTO) {
		DonationEntity donation = donationRepo.findById(id).orElseThrow();
		/**
		 * Not allowing id, profile and raised on, delete to be updated at any condition
		 */
		DonationEntity updatedDonation = new DonationEntity();
		updatedDonation
				.setAccountId(donationDTO.getPaidToAccount() == null ? null : donationDTO.getPaidToAccount().getId());
		updatedDonation.setAccountName(
				donationDTO.getPaidToAccount() == null ? null : donationDTO.getPaidToAccount().getAccountName());

		updatedDonation.setAmount(donationDTO.getAmount());
		updatedDonation.setStatus(donationDTO.getStatus() == null ? null : donationDTO.getStatus().name());
		updatedDonation.setType(donationDTO.getType() == null ? null : donationDTO.getType().name());
		if (donationDTO.getGuest() != null) {
			updatedDonation.setIsGuest(donationDTO.getGuest());
		}

		if (donationDTO.getIsPaymentNotified() != null) {
			updatedDonation.setIsPaymentNotified(donationDTO.getIsPaymentNotified());
			updatedDonation.setNotifiedOn(donationDTO.getPaymentNotificationDate());
		}

		/*
		 * Name and email address update is only allowed for guest donation
		 */
		if (donation.getIsGuest() == Boolean.TRUE) {
			updatedDonation.setDonorContactNumber(
					donationDTO.getDonor() == null ? null : donationDTO.getDonor().getPhoneNumber());
			updatedDonation
					.setDonorEmailAddress(donationDTO.getDonor() == null ? null : donationDTO.getDonor().getEmail());
			updatedDonation.setDonorName(donationDTO.getDonor() == null ? null : donationDTO.getDonor().getName());
		}

		updatedDonation.setEndDate(donationDTO.getEndDate());
		updatedDonation.setEventId(donationDTO.getForEventId());

		updatedDonation.setPaidOn(donationDTO.getPaidOn());

		updatedDonation.setPaymentConfirmedBy(
				donationDTO.getConfirmedBy() == null ? null : donationDTO.getConfirmedBy().getProfileId());
		updatedDonation.setPaymentConfirmedByName(
				donationDTO.getConfirmedBy() == null ? null : donationDTO.getConfirmedBy().getName());

		updatedDonation.setPaymentConfirmedOn(donationDTO.getConfirmedOn());
		updatedDonation.setPaymentMethod(
				donationDTO.getPaymentMethod() == null ? null : donationDTO.getPaymentMethod().name());
		updatedDonation.setPaidUPIName(donationDTO.getUpiName() == null ? null : donationDTO.getUpiName().name());

		updatedDonation.setStartDate(donationDTO.getStartDate());
		updatedDonation.setTransactionRefNumber(donationDTO.getTransactionRefNumber());
		updatedDonation.setComment(donationDTO.getComment());
		updatedDonation.setCancelReason(donationDTO.getCancelReason());
		updatedDonation.setPayLaterReason(donationDTO.getPayLaterReason());
		updatedDonation.setPaymentFailDetail(donationDTO.getPaymentFailDetail());

		CommonUtils.copyNonNullProperties(updatedDonation, donation);
		donation =donationRepo.save(donation);
		if (donationDTO.getAdditionalFields() != null) {
			for (FieldDTO addfield : donationDTO.getAdditionalFields()) {
				addfield.setFieldSource(donation.getId());
				addOrUpdateCustomField(addfield);
			}
		}
		return InfraDTOHelper.convertToDonationDTO(donation);
	}

}
