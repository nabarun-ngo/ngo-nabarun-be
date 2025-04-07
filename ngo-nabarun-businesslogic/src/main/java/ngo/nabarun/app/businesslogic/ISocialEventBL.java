package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail.EventDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;

@Service
public interface ISocialEventBL {

	Paginate<EventDetail> getSocialEvents(Integer page, Integer size, EventDetailFilter filter);
	EventDetail getSocialEvent(String id);
	EventDetail createSocialEvent(EventDetail eventDetail) throws Exception;
	EventDetail updateSocialEvent(String id,EventDetail updatedEventDetail) throws Exception;
	List<DocumentDetail> getSocialEventDocs(String id);
	void deleteEvent(String id);

}
