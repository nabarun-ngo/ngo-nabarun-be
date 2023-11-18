package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Page;

@Service
public interface ISocialEventBL {

	Page<EventDetail> getSocialEvents(Integer page, Integer size, EventDetailFilter filter);
	EventDetail getSocialEvent(String id);
	EventDetail createSocialEvent(EventDetailCreate eventDetail) throws Exception;
	EventDetail updateSocialEvent(String id,EventDetailUpdate updatedEventDetail) throws Exception;
	List<DocumentDetail> getSocialEventDocs(String id);
	EventDetail getDraftedEvent();
	void deleteEvent(String id);

}
