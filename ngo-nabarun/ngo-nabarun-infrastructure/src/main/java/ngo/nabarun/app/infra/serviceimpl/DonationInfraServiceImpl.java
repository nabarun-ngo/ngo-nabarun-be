package ngo.nabarun.app.infra.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
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
	public List<DonationDTO> getUserDonations(String id,Integer page, Integer size) {
		List<DonationEntity> donations = null;
		if (page != null && size != null) {
			Page<DonationEntity> donation2s = donationRepo.findByProfile(id,PageRequest.of(page, size));
		} else {
			donations = donationRepo.findByProfileId(id);
		}
		return donations.stream().map(m -> InfraDTOHelper.convertToDonationDTO(m)).collect(Collectors.toList());
	}

	@Override
	public DonationDTO createDonation(DonationDTO donationDTO) {
		DonationEntity donation = new DonationEntity();
		donation.setStartDate(donationDTO.getStartDate());
		donation.setId(donationDTO.getId());
		donation.setAmount(donationDTO.getAmount());
		donation.setContributionStatus(donationDTO.getStatus().name());
		donation.setContributionType(donationDTO.getType().name());
		donation.setDeleted(false);
		donation.setEndDate(donationDTO.getEndDate());
		donation.setEventId(donationDTO.getForEventId());
		donation.setIsGuest(donationDTO.getGuest());
		donation.setRaisedOn(CommonUtils.getSystemDate());
		if (donationDTO.getGuest() != Boolean.TRUE) {
			donation.setProfile(donationDTO.getDonor().getProfileId());
		} 
		
		donation.setDonorContactNumber(donationDTO.getDonor().getPrimaryPhoneNumber());
		donation.setDonorEmailAddress(donationDTO.getDonor().getEmail());
		donation.setDonorName(donationDTO.getDonor().getName());		
		
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
			//example.setPaymentMethod(filter.getPaymentMethod());

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
	
	@Override
	public long getDonationsCount(String id) {
		return donationRepo.countByProfile(id);
	}
	
	@Override
	public DonationDTO updateDonation(String id,DonationDTO donationDTO) {
		DonationEntity donation=donationRepo.findById(id ).orElseThrow();
		/**
		 * Not allowing id, profile and raised on, delete to be updated at any condition
		 */
		DonationEntity updatedDonation= new DonationEntity();
		updatedDonation.setAccountId(donationDTO.getAccountId());
		updatedDonation.setAmount(donationDTO.getAmount());
		updatedDonation.setContributionStatus(donationDTO.getStatus() == null ? null : donationDTO.getStatus().name());
		updatedDonation.setContributionType(donationDTO.getType() == null ? null : donationDTO.getType().name());
		if(donationDTO.getGuest() != null) {
			updatedDonation.setIsGuest(donationDTO.getGuest());
		}
		
		if(donationDTO.getIsPaymentNotified() != null) {
			updatedDonation.setIsPaymentNotified(donationDTO.getIsPaymentNotified());
		}
		
		/*
		 * Name and email address update is only allowed for guest donation
		 */
		if (donation.getIsGuest() == Boolean.TRUE) {
			updatedDonation.setDonorContactNumber(donationDTO.getDonor() == null ? null : donationDTO.getDonor().getPrimaryPhoneNumber());
			updatedDonation.setDonorEmailAddress(donationDTO.getDonor() == null ? null : donationDTO.getDonor().getEmail());
			updatedDonation.setDonorName(donationDTO.getDonor() == null ? null : donationDTO.getDonor().getName());
		}
		
		updatedDonation.setEndDate(donationDTO.getEndDate());
		updatedDonation.setEventId(donationDTO.getForEventId());
		
		updatedDonation.setPaidOn(donationDTO.getPaidOn());
		
		updatedDonation.setPaymentConfirmedBy(donationDTO.getConfirmedBy() == null ? null : donationDTO.getConfirmedBy().getProfileId());
		updatedDonation.setPaymentConfirmedByName(donationDTO.getConfirmedBy() == null ? null : donationDTO.getConfirmedBy().getName());
		
		updatedDonation.setPaymentConfirmedOn(donationDTO.getConfirmedOn());
		updatedDonation.setPaymentMethod(donationDTO.getPaymentMethod() == null ? null : donationDTO.getPaymentMethod().name());
		updatedDonation.setPaidUPIName(donationDTO.getUpiName()== null ? null : donationDTO.getUpiName().name());
		updatedDonation.setStartDate(donationDTO.getStartDate());
		updatedDonation.setTransactionRefNumber(donationDTO.getTransactionRefNumber());
		updatedDonation.setComment(donationDTO.getComment());
		
		CommonUtils.copyNonNullProperties(updatedDonation, donation);
		return InfraDTOHelper.convertToDonationDTO(donationRepo.save(donation));
	}

}
