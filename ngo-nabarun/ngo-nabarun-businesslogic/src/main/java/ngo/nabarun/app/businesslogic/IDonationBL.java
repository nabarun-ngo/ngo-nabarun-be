package ngo.nabarun.app.businesslogic;

import java.util.List;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;

@Service
public interface IDonationBL {
	DonationDetail raiseDonation(DonationDetailCreate request) throws Exception;

	Paginate<DonationDetail> getUserDonations(String id,Integer index,Integer size) throws Exception;

	Paginate<DonationDetail> getLoggedInUserDonations(Integer index,Integer size) throws Exception;

	void autoRaiseDonation();

	Paginate<DonationDetail> getDonations(Integer index,Integer size, DonationDetailFilter filter);
	
	List<DocumentDetail> getDonationDocument(String donationId);

	List<KeyValue> getNextDonationStatus(DonationType type,DonationStatus currentStatus) throws Exception;

	DonationDetail updateDonation(String id,DonationDetailUpdate request) throws Exception;
}
