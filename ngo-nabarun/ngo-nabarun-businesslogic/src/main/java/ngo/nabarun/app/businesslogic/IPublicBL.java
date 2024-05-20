package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.SignUpDetail;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;

@Service
public interface IPublicBL {
	
	SignUpDetail signUp(SignUpDetail interview) throws Exception;

	List<KeyValue> getOrganizationInfo() throws Exception;


}
