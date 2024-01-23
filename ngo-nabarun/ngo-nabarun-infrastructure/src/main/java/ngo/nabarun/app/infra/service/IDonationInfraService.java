package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.DonationDTO.DonationDTOFilter;

@Service
public interface IDonationInfraService {

	DonationDTO createDonation(DonationDTO donationDTO);

	Page<DonationDTO> getDonations(Integer page,Integer size, DonationDTOFilter filter);
	
	DonationDTO getDonation(String donationId);

	long getDonationsCount();
	long getDonationsCount(String id);

	DonationDTO updateDonation(String id, DonationDTO updatedDonationDTO);

}
