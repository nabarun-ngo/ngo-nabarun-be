package ngo.nabarun.app.businesslogic.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.EventType;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.EventDTO.EventDTOFilter;
import ngo.nabarun.app.infra.service.IEventInfraService;

@Component
public class SocialEventDO extends CommonDO {

	@Autowired
	private IEventInfraService eventInfraService;
	
	public Paginate<EventDTO> retrieveSocialEvents(Integer page, Integer size, EventDTOFilter eventDTOFilter) {
		Page<EventDTO> content =eventInfraService.getEventList(page, size, eventDTOFilter);
		return new Paginate<EventDTO>(content);
	}

	
	public EventDTO createSocialEvent(EventDetail eventDetail) throws Exception {
		EventDTO eventDTO = new EventDTO();
		eventDTO.setId(generateEventId());
		eventDTO.setBudget(eventDetail.getEventBudget());
		eventDTO.setDescription(eventDetail.getEventDescription());
		eventDTO.setDraft(eventDetail.isDraft());
		eventDTO.setEventDate(eventDetail.getEventDate());
		eventDTO.setLocation(eventDetail.getEventLocation());
		eventDTO.setTitle(eventDetail.getTitle());
		eventDTO.setType(EventType.INTERNAL);
		eventDTO.setCreator(BusinessObjectConverter.toUserDTO(SecurityUtils.getAuthUser()));
		return eventInfraService.createEvent(eventDTO);
    }

	
	public EventDTO updateSocialEvent(String id, EventDetail updatedEventDetail) throws Exception {
		/*
		 * Setting attributes
		 */
		EventDTO updatedDTO = new EventDTO();
		updatedDTO.setBudget(updatedEventDetail.getEventBudget());
		updatedDTO.setDescription(updatedEventDetail.getEventDescription());
		updatedDTO.setTotalExpense(updatedDTO.getTotalExpense());
		updatedDTO.setEventDate(updatedEventDetail.getEventDate());
		updatedDTO.setLocation(updatedEventDetail.getEventLocation());
		updatedDTO.setTitle(updatedEventDetail.getTitle());
		updatedDTO.setType(updatedEventDetail.getEventType());
		return eventInfraService.updateEvent(id,updatedDTO);
	}


	public void deleteEvent(String id) {
		eventInfraService.deleteEvent(id);
	}

}
