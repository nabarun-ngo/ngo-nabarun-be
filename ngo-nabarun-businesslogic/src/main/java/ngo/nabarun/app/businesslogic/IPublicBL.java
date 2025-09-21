package ngo.nabarun.app.businesslogic;

import java.util.Map;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.InterviewDetail;
import ngo.nabarun.app.common.enums.PublicPage;

@Service
public interface IPublicBL {
	
	InterviewDetail signUp(InterviewDetail interview) throws Exception;

	InterviewDetail initDonation(InterviewDetail interview) throws Exception;

	InterviewDetail contact(InterviewDetail interview) throws Exception;

	Map<String, Object> getPageData(PublicPage pageName) throws Exception;
	
}