package ngo.nabarun.app.businesslogic;

import java.util.List;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Page;

@Service
public interface IDonationBL {
	DonationDetail raiseDonation(DonationDetailCreate request) throws Exception;

	List<DonationDetail> getUserDonations(String id) throws Exception;

	List<DonationDetail> getLoggedInUserDonations() throws Exception;

	void autoRaiseDonation();

	Page<DonationDetail> getDonations(Integer index,Integer size, DonationDetailFilter filter);
	
	List<DocumentDetail> getDonationDocument(String donationId);

}
