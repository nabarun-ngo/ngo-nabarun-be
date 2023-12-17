package ngo.nabarun.app.infra.service;

import java.util.List;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.DonationDTO;

@Service
public interface IDonationInfraService {
	List<DonationDTO> getUserDonations(String id,Integer page,Integer size);

	DonationDTO createDonation(DonationDTO donationDTO);

	List<DonationDTO> getDonations(Integer page,Integer size, DonationDTO filter);
	
	DonationDTO getDonation(String donationId);

	long getDonationsCount();
	long getDonationsCount(String id);

	DonationDTO updateDonation(String id, DonationDTO updatedDonationDTO);

}
