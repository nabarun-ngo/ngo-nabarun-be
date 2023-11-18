package ngo.nabarun.app.infra.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.SocialEventEntity;
import ngo.nabarun.app.infra.core.repo.SocialEventRepository;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.service.IEventInfraService;

@Service
public class EventInfraServiceImpl implements IEventInfraService {

	@Autowired
	private SocialEventRepository eventRepository;

	@Override
	public List<EventDTO> getEventList(Integer page, Integer size, EventDTO filter) {
		List<SocialEventEntity> events = null;
		if (filter != null) {
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase()
					.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
			SocialEventEntity example = new SocialEventEntity();
			example.setTitle(filter.getTitle());
			example.setEventState(filter.getType() == null ? null : filter.getType().name());
			example.setCreatedBy(filter.getCreatorId());
			example.setDraft(filter.isDraft());
			events = (page == null || size == null) ? eventRepository.findAll(Example.of(example, matcher))
					: eventRepository.findAll(Example.of(example, matcher), PageRequest.of(page, size)).getContent();
		} else if (page != null && size != null) {
			events = eventRepository.findAll(PageRequest.of(page, size)).getContent();
		} else {
			events = eventRepository.findAll();
		}
		return events.stream().map(m -> InfraDTOHelper.convertToEventDTO(m)).collect(Collectors.toList());
	}

	@Override
	public EventDTO createEvent(EventDTO eventDTO) throws Exception {
		SocialEventEntity event = new SocialEventEntity();
		event.setCreatedBy(eventDTO.getCreatorId());
		event.setCreatedOn(CommonUtils.getSystemDate());
		event.setDescription(eventDTO.getDescription());
		event.setDraft(eventDTO.isDraft());
		event.setEventBudget(eventDTO.getBudget());
		event.setEventDate(eventDTO.getEventDate());
		event.setEventLocation(eventDTO.getLocation());
		event.setEventState(eventDTO.getType().name());
		event.setId(eventDTO.getId());
		event.setTitle(eventDTO.getTitle());
		event = eventRepository.save(event);
		return InfraDTOHelper.convertToEventDTO(event);
	}

	@Override
	public EventDTO getEvent(String id) {
		SocialEventEntity event = eventRepository.findById(id).orElseThrow();
		return InfraDTOHelper.convertToEventDTO(event);
	}

	@Override
	public void deleteEvent(String id) {
		SocialEventEntity event = eventRepository.findById(id).orElseThrow();
		eventRepository.delete(event);
	}

	@Override
	public long getEventsCount() {
		return eventRepository.count();
	}

	@Override
	public EventDTO updateEvent(String id, EventDTO eventDTO) throws Exception {
		SocialEventEntity event = eventRepository.findById(id).orElseThrow();
		SocialEventEntity updated_event = new SocialEventEntity();
		updated_event.setCreatedBy(eventDTO.getCreatorId());
		updated_event.setDescription(eventDTO.getDescription());
		updated_event.setDraft(eventDTO.isDraft());
		updated_event.setEventBudget(eventDTO.getBudget());
		updated_event.setEventDate(eventDTO.getEventDate());
		updated_event.setEventLocation(eventDTO.getLocation());
		updated_event.setEventState(eventDTO.getType() == null ? null : eventDTO.getType().name());
		updated_event.setTitle(eventDTO.getTitle());
		updated_event.setCoverPicture(eventDTO.getCoverPic());
		CommonUtils.copyNonNullProperties(updated_event, event);
		
		event = eventRepository.save(event);
		return InfraDTOHelper.convertToEventDTO(event);
	}

}
