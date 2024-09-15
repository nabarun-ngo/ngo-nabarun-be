package ngo.nabarun.app.businesslogic;

import java.util.List;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationSummary;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;

@Service
public interface IDonationBL {
	DonationDetail raiseDonation(DonationDetail request) throws Exception;

	Paginate<DonationDetail> getUserDonations(String id,Integer index,Integer size) throws Exception;

	Paginate<DonationDetail> getLoggedInUserDonations(Integer index,Integer size) throws Exception;

	void autoRaiseDonation() throws Exception;

	Paginate<DonationDetail> getDonations(Integer index,Integer size, DonationDetailFilter filter);
	
	List<DocumentDetail> getDonationDocument(String donationId);

	DonationDetail updateDonation(String id,DonationDetail request) throws Exception;

	DonationSummary getDonationSummary(String id, List<String> fields) throws Exception;

	DonationDetail updatePaymentInfo(String id, DonationDetail request) throws Exception;
}
