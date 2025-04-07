package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.QSocialEventEntity;
import ngo.nabarun.app.infra.core.entity.SocialEventEntity;
import ngo.nabarun.app.infra.core.repo.SocialEventRepository;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.EventDTO.EventDTOFilter;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.WhereClause;
import ngo.nabarun.app.infra.service.IEventInfraService;

@Service
public class EventInfraServiceImpl implements IEventInfraService {

	@Autowired
	private SocialEventRepository eventRepository;

	@Override
	public Page<EventDTO> getEventList(Integer page ,Integer size,EventDTOFilter filter) {
		Page<SocialEventEntity> pageContent = null;
		Sort sort = Sort.by(Sort.Direction.DESC, "eventDate");
		if (filter != null) {

			/*
			 * Query building and filter logic
			 */
			QSocialEventEntity q = QSocialEventEntity.socialEventEntity;
			BooleanBuilder query = WhereClause.builder()
					.optionalAnd(filter.getId() != null, () -> q.id.eq(filter.getId()))
					.optionalAnd(filter.getTitle() != null, () -> q.title.contains(filter.getTitle()))
					.optionalAnd(filter.getLocation() != null, () -> q.eventLocation.contains(filter.getLocation()))
					.optionalAnd(filter.getToDate() != null && filter.getFromDate() != null,
							() -> q.eventDate.between(filter.getFromDate(), filter.getToDate()))
					.build();

			if (page == null || size == null) {
				List<SocialEventEntity> result = new ArrayList<>();
				eventRepository.findAll(query, sort).iterator().forEachRemaining(result::add);
				pageContent = new PageImpl<>(result);
			} else {
				pageContent = eventRepository.findAll(query, PageRequest.of(page, size, sort));
			}
		} else if (page != null && size != null) {
			pageContent = eventRepository.findAll(PageRequest.of(page, size, sort));
		} else {
			pageContent = new PageImpl<>(eventRepository.findAll(sort));
		}
		return pageContent.map(InfraDTOHelper::convertToEventDTO);
	}

	@Override
	public EventDTO createEvent(EventDTO eventDTO) throws Exception {
		SocialEventEntity event = new SocialEventEntity();
		event.setCreatedById(eventDTO.getCreator().getProfileId());
		event.setCreatedByName(eventDTO.getCreator().getName());
		event.setCreatedOn(CommonUtils.getSystemDate());
		event.setDescription(eventDTO.getDescription());
		event.setDraft(eventDTO.isDraft());
		event.setEventBudget(eventDTO.getBudget());
		event.setEventDate(eventDTO.getEventDate());
		event.setEventLocation(eventDTO.getLocation());
		event.setEventState(eventDTO.getType().name());
		event.setId(eventDTO.getId());
		event.setTitle(eventDTO.getTitle());
		event.setEventExpense(eventDTO.getTotalExpense());
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
		updated_event.setDescription(eventDTO.getDescription());
		updated_event.setDraft(eventDTO.isDraft());
		updated_event.setEventBudget(eventDTO.getBudget());
		updated_event.setEventDate(eventDTO.getEventDate());
		updated_event.setEventLocation(eventDTO.getLocation());
		updated_event.setEventState(eventDTO.getType() == null ? null : eventDTO.getType().name());
		updated_event.setTitle(eventDTO.getTitle());
		updated_event.setCoverPicture(eventDTO.getCoverPic());
		updated_event.setEventExpense(eventDTO.getTotalExpense());

		CommonUtils.copyNonNullProperties(updated_event, event);
		
		event = eventRepository.save(event);
		return InfraDTOHelper.convertToEventDTO(event);
	}

}
