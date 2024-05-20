package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.ISocialEventBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectToDTOConverter;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.EventType;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IEventInfraService;

@Service
public class SocialEventBLImpl implements ISocialEventBL {
	
	@Autowired
	private IEventInfraService eventInfraService;
	
//	@Autowired
//	private IUserInfraService userInfraService;
	
	@Autowired
	private IDocumentInfraService documentInfraService;

	@Override
	public Paginate<EventDetail> getSocialEvents(Integer page, Integer size, EventDetailFilter filter) {
		EventDTO eventDTOFilter = null;
		if(filter != null) {
			eventDTOFilter= new EventDTO();
			eventDTOFilter.setTitle(filter.getTitle());
			eventDTOFilter.setType(filter.getEventType());
		}
		List<EventDetail> content =eventInfraService.getEventList(page, size, eventDTOFilter).stream()
				.filter(f->!f.isDraft())
				.map(m -> DTOToBusinessObjectConverter.toEventDetail(m)).collect(Collectors.toList());
		long total;
		if(page != null && size != null){
			total=eventInfraService.getEventsCount();
		}else {
			total = content.size();
		}
		return new Paginate<EventDetail>(page, size, total, content);
	}

	@Override
	public EventDetail getSocialEvent(String id) {
		EventDTO event=eventInfraService.getEvent(id);
		return DTOToBusinessObjectConverter.toEventDetail(event);
	}

	@Override
	public EventDetail createSocialEvent(EventDetailCreate eventDetail) throws Exception {
		EventDTO eventDTO = new EventDTO();
		eventDTO.setBudget(eventDetail.getEventBudget());
		eventDTO.setDescription(eventDetail.getEventDescription());
		eventDTO.setDraft(eventDetail.isDraft());
		eventDTO.setEventDate(eventDetail.getEventDate());
		eventDTO.setLocation(eventDetail.getEventLocation());
		eventDTO.setTitle(eventDetail.getTitle());
		eventDTO.setType(EventType.INTERNAL);
		
		//userInfraService.getUserByUserId(SecurityUtils.getAuthUserId(), false);
		/*
		 * Add profile id instead of user id
		 */
		eventDTO.setCreatorId(SecurityUtils.getAuthUserId());
		eventDTO=eventInfraService.createEvent(eventDTO);
		if (eventDetail.getBase64Image() != null) {
			List<DocumentDTO> profilePics = documentInfraService.getDocumentList(eventDTO.getId(),
					DocumentIndexType.EVENT_COVER);
			for (DocumentDTO doc : profilePics) {
				documentInfraService.hardDeleteDocument(doc.getDocId());
			}
			byte[] content = Base64.decodeBase64(eventDetail.getBase64Image());
			DocumentDTO doc = documentInfraService.uploadDocument("demo.png", "image/png", eventDTO.getId(),
					DocumentIndexType.EVENT_COVER, content);
			EventDTO updatedEvent= new EventDTO();
			updatedEvent.setCoverPic(doc.getDocumentURL());
			eventDTO=eventInfraService.updateEvent(eventDTO.getId(), updatedEvent);
		}
		return DTOToBusinessObjectConverter.toEventDetail(eventDTO);
	}

	@Override
	public EventDetail updateSocialEvent(String id, EventDetailUpdate updatedEventDetail) throws Exception {
		EventDTO event =eventInfraService.getEvent(id);
		/*
		 * Setting attributes
		 */
		EventDTO updatedDTO = new EventDTO();
		updatedDTO.setBudget(updatedEventDetail.getEventBudget());
		updatedDTO.setDescription(updatedEventDetail.getEventDescription());
		if(updatedEventDetail.getPublish() != null) {
			updatedDTO.setDraft(!updatedEventDetail.getPublish());
		}
		updatedDTO.setEventDate(updatedEventDetail.getEventDate());
		updatedDTO.setLocation(updatedEventDetail.getEventLocation());
		updatedDTO.setTitle(updatedEventDetail.getTitle());
		updatedDTO.setType(updatedEventDetail.getEventType());
		/*
		 * Uploading cover picture
		 */
		if (updatedEventDetail.getBase64Image() != null) {
			List<DocumentDTO> profilePics = documentInfraService.getDocumentList(event.getId(),
					DocumentIndexType.EVENT_COVER);
			for (DocumentDTO doc : profilePics) {
				documentInfraService.hardDeleteDocument(doc.getDocId());
			}
			byte[] content = Base64.decodeBase64(updatedEventDetail.getBase64Image());
			DocumentDTO doc = documentInfraService.uploadDocument("ecp.png", "image/png", event.getId(),
					DocumentIndexType.EVENT_COVER, content);
			updatedDTO.setCoverPic(doc.getDocumentURL());
		}
		event=eventInfraService.updateEvent(id,updatedDTO);
		return DTOToBusinessObjectConverter.toEventDetail(event);
	}

	@Override
	public List<DocumentDetail> getSocialEventDocs(String id) {
		return documentInfraService.getDocumentList(id, DocumentIndexType.DONATION).stream().map(m -> {
			DocumentDetail doc = new DocumentDetail();
			doc.setDocId(m.getDocId());
			doc.setDocumentIndexId(id);
			doc.setImage(m.isImage());
			doc.setOriginalFileName(m.getOriginalFileName());
			return doc;
		}).toList();
	}

	@Override
	public EventDetail getDraftedEvent() {
		EventDTO filter =  new EventDTO();
		filter.setCreatorId(SecurityUtils.getAuthUserId());
		filter.setDraft(true);
		List<EventDTO> draftedEvants=eventInfraService.getEventList(null, null, filter);
		if(draftedEvants.size()==0) {
			return null;
		}
		return DTOToBusinessObjectConverter.toEventDetail(draftedEvants.get(0));
	}

	@Override
	public void deleteEvent(String id) {
		eventInfraService.deleteEvent(id);
	}



}
