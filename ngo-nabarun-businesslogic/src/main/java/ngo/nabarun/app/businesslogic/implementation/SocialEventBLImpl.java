package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.ISocialEventBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail.EventDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.domain.SocialEventDO;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.EventDTO.EventDTOFilter;

@Service
public class SocialEventBLImpl implements ISocialEventBL {
	
	@Autowired
	private SocialEventDO eventDO;

	@Override
	public Paginate<EventDetail> getSocialEvents(Integer page, Integer size, EventDetailFilter filter) {
		EventDTOFilter eventDTOFilter = null;
		if(filter != null) {
			eventDTOFilter = new EventDTOFilter();
			eventDTOFilter.setTitle(filter.getEventTitle());
			eventDTOFilter.setId(filter.getId());
			eventDTOFilter.setFromDate(filter.getFromDate());
			eventDTOFilter.setToDate(filter.getToDate());
			eventDTOFilter.setCompleted(filter.getCompleted());
		}
		return eventDO.retrieveSocialEvents(page, size, eventDTOFilter).map(BusinessObjectConverter::toEventDetail);
	}

	@Override
	public EventDetail getSocialEvent(String id) {		
		EventDTOFilter eventDTOFilter= new EventDTOFilter();
		eventDTOFilter.setId(id);
		EventDTO event = eventDO.retrieveSocialEvents(null, null, eventDTOFilter).getContent().stream().findFirst().orElseThrow();
		return BusinessObjectConverter.toEventDetail(event);
	}

	@Override
	public EventDetail createSocialEvent(EventDetail eventDetail) throws Exception {
		EventDTO eventDTO = eventDO.createSocialEvent(eventDetail);
		return BusinessObjectConverter.toEventDetail(eventDTO);
	}

	@Override
	public EventDetail updateSocialEvent(String id, EventDetail updatedEventDetail) throws Exception {
		EventDTO event =eventDO.updateSocialEvent(id,updatedEventDetail);
		return BusinessObjectConverter.toEventDetail(event);
	}

	@Override
	public List<DocumentDetail> getSocialEventDocs(String id) {
		return eventDO.getDocuments(id, DocumentIndexType.EVENT).stream().map(m -> {
			DocumentDetail doc = new DocumentDetail();
			doc.setDocId(m.getDocId());
			doc.setDocumentIndexId(id);
			doc.setImage(m.isImage());
			doc.setOriginalFileName(m.getOriginalFileName());
			return doc;
		}).toList();
	}

	@Override
	public void deleteEvent(String id) {
		eventDO.deleteEvent(id);
	}



}
