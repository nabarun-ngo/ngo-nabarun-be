package ngo.nabarun.app.businesslogic;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.InterviewDetail;

@Service
public interface IPublicBL {
	
	InterviewDetail signUp(InterviewDetail interview) throws Exception;

	InterviewDetail initDonation(InterviewDetail interview) throws Exception;

	InterviewDetail contact(InterviewDetail interview) throws Exception;

	Map<String, Object> getPageData(List<String> dataFilter);
	//Map<String, Object> getPageData();

}
