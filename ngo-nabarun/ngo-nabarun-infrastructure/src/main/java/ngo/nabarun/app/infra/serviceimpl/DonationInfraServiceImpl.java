package ngo.nabarun.app.infra.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.DonationEntity;
import ngo.nabarun.app.infra.core.repo.DonationRepository;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.service.IDonationInfraService;

@Service
public class DonationInfraServiceImpl implements IDonationInfraService {

	@Autowired
	private DonationRepository donationRepo;

	@Override
	public List<DonationDTO> getUserDonations(String id) {
		List<DonationEntity> donations = donationRepo.findByProfileId(id);
		return donations.stream().map(m -> InfraDTOHelper.convertToDonationDTO(m)).collect(Collectors.toList());
	}

	@Override
	public DonationDTO createDonation(DonationDTO donationDTO) {
		DonationEntity donation = new DonationEntity();

		donation.setAmount(donationDTO.getAmount());
		donation.setContributionStatus(donationDTO.getStatus().name());
		donation.setContributionType(donationDTO.getType().name());
		donation.setDeleted(false);
		donation.setEndDate(donationDTO.getEndDate());
		donation.setEventId(donationDTO.getForEventId());
		donation.setIsGuest(donationDTO.getGuest());
		donation.setRaisedOn(CommonUtils.getSystemDate());
		if (donationDTO.getGuest() == Boolean.TRUE) {
			donation.setGuestContactNumber(donationDTO.getDonor().getPrimaryPhoneNumber());
			donation.setGuestEmailAddress(donationDTO.getDonor().getEmail());
			donation.setGuestFullNameOrOrgName(donationDTO.getDonor().getFirstName());
		} else {
			donation.setProfile(donationDTO.getDonor().getProfileId());
			donation.setGuestEmailAddress(donationDTO.getDonor().getEmail());
			donation.setGuestFullNameOrOrgName(
					donationDTO.getDonor().getFirstName() + " " + donationDTO.getDonor().getLastName());
		}
		donation.setStartDate(donationDTO.getStartDate());
		donation = donationRepo.save(donation);
		// create log at firebase
		return InfraDTOHelper.convertToDonationDTO(donation);
	}

	@Override
	public List<DonationDTO> getDonations(Integer page, Integer size, DonationDTO filter) {
		List<DonationEntity> donations = null;
		if (filter != null) {
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase()
					.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
			DonationEntity example = new DonationEntity();
			example.setAccountId(filter.getAccountId());
			example.setIsGuest(filter.getGuest() == Boolean.TRUE);
			example.setStartDate(filter.getStartDate());
			example.setEndDate(filter.getEndDate());
			example.setContributionType(filter.getType() == null ? null : filter.getType().name());
			example.setContributionStatus(filter.getStatus() == null ? null : filter.getStatus().name());
			example.setPaymentMethod(filter.getPaymentMethod());

			donations = (page == null || size == null) ? donationRepo.findAll(Example.of(example, matcher))
					: donationRepo.findAll(Example.of(example, matcher), PageRequest.of(page, size)).getContent();
		} else if (page != null && size != null) {
			donations = donationRepo.findAll(PageRequest.of(page, size)).getContent();
		} else {
			donations = donationRepo.findAll();
		}

		return donations.stream().map(m -> InfraDTOHelper.convertToDonationDTO(m)).collect(Collectors.toList());
	}

	@Override
	public DonationDTO getDonation(String donationId) {
		return InfraDTOHelper.convertToDonationDTO(donationRepo.findById(donationId).orElseThrow());
	}

	@Override
	public long getDonationsCount() {
		return donationRepo.count();
	}

}
