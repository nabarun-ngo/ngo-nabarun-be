package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.EventDTO.EventDTOFilter;

@Service
public interface IEventInfraService {
	Page<EventDTO> getEventList(Integer index,Integer size,EventDTOFilter filter);
	EventDTO createEvent(EventDTO eventDTO) throws Exception;
	EventDTO getEvent(String id);
	void deleteEvent(String id);
	long getEventsCount();
	EventDTO updateEvent(String id,EventDTO eventDTO) throws Exception;

}
