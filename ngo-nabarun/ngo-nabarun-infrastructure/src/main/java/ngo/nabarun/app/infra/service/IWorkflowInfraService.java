package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.EventDTO;

@Service
public interface IWorkflowInfraService {
	List<EventDTO> getEventList(Integer index,Integer size,EventDTO filter);
	EventDTO createEvent(EventDTO eventDTO) throws Exception;
	EventDTO getEvent(String id);
	void deleteEvent(String id);
	long getEventsCount();
	EventDTO updateEvent(String id,EventDTO eventDTO) throws Exception;

}
