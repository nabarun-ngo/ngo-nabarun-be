package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.NoticeEntity;
import ngo.nabarun.app.infra.core.entity.QNoticeEntity;
import ngo.nabarun.app.infra.core.repo.NoticeRepository;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO.NoticeDTOFilter;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.InfraFieldHelper;
import ngo.nabarun.app.infra.misc.WhereClause;
import ngo.nabarun.app.infra.service.INoticeInfraService;

@Service
public class NoticeInfraServiceImpl implements INoticeInfraService {

	@Autowired
	private NoticeRepository noticeRepository;

	@Override
	public Page<NoticeDTO> getNoticeList(Integer page, Integer size, NoticeDTOFilter filter) {
		Page<NoticeEntity> noticePage = null;
		Sort sort = Sort.by(Sort.Direction.DESC, "noticeDate");
		if (filter != null) {

			/*
			 * Query building and filter logic
			 */
			QNoticeEntity qNotice = QNoticeEntity.noticeEntity;
			BooleanBuilder query = WhereClause.builder()
					.optionalAnd(filter.getId() != null, () -> qNotice.id.eq(filter.getId()))
					.optionalAnd(filter.getTitle() != null, () -> qNotice.title.contains(filter.getTitle()))
					.optionalAnd(filter.getStatus() != null,
							() -> qNotice.status.in(filter.getStatus().stream().map(m -> m.name()).toList()))
					.optionalAnd(true, () -> qNotice.template.eq(filter.isTemplate()))
					.optionalAnd(filter.getToDate() != null && filter.getFromDate() != null,
							() -> qNotice.noticeDate.between(filter.getFromDate(), filter.getToDate()))
					.build();

			if (page == null || size == null) {
				List<NoticeEntity> result = new ArrayList<>();
				noticeRepository.findAll(query, sort).iterator().forEachRemaining(result::add);
				noticePage = new PageImpl<>(result);
			} else {
				noticePage = noticeRepository.findAll(query, PageRequest.of(page, size, sort));
			}
		} else if (page != null && size != null) {
			noticePage = noticeRepository.findAll(PageRequest.of(page, size, sort));
		} else {
			noticePage = new PageImpl<>(noticeRepository.findAll(sort));
		}
		return noticePage.map(InfraDTOHelper::convertToNoticeDTO);
	}

	@Override
	public NoticeDTO createNotice(NoticeDTO noticeDTO) throws Exception {
		NoticeEntity notice = new NoticeEntity();
		notice.setId(noticeDTO.getId());
		notice.setCreatedById(noticeDTO.getCreatedBy().getProfileId());
		notice.setCreatedBy(noticeDTO.getCreatedBy().getName());
		notice.setCreatedOn(CommonUtils.getSystemDate());
		notice.setCreatorRole(InfraFieldHelper.stringListToString(noticeDTO.getCreatedBy().getRoles().stream().map(m->m.getName()).collect(Collectors.toList())));
		
		notice.setDescription(noticeDTO.getDescription());
		notice.setTemplate(noticeDTO.isTemplateNotice());
		 notice.setDraft(false);
		notice.setNeedMeeting(noticeDTO.getNeedMeeting());
		notice.setNoticeDate(noticeDTO.getNoticeDate());
		notice.setPublishedOn(notice.getCreatedOn());
		notice.setTitle(noticeDTO.getTitle());
		notice.setVisibility(null);
		notice.setStatus(noticeDTO.getStatus() == null ? null : noticeDTO.getStatus().name());
		
		/**
		 * Saving Meeting details
		 */ 
		if(noticeDTO.getMeeting() != null) {
			MeetingDTO meetingDTO=noticeDTO.getMeeting();
			notice.setMeetingDescription(meetingDTO.getDescription());
			notice.setMeetingLocation(meetingDTO.getLocation());
			notice.setMeetingStatus(meetingDTO.getStatus().name());
			notice.setMeetingRemarks(meetingDTO.getRemarks());
			notice.setMeetingType(meetingDTO.getType() == null ? null : meetingDTO.getType().name());
			notice.setMeetingDate(meetingDTO.getDate());
			notice.setMeetingStartTime(meetingDTO.getStartTime());
			notice.setMeetingEndTime(meetingDTO.getEndTime()); 
			notice.setMeetingSummary(meetingDTO.getSummary());

			if (meetingDTO.getAttendees() != null && !meetingDTO.getAttendees().isEmpty()) {
				notice.setAttendeeEmails(InfraFieldHelper.stringListToString(
						meetingDTO.getAttendees().stream().map(m -> m.getEmail()).collect(Collectors.toList())));
				notice.setAttendeeNames(InfraFieldHelper.stringListToString(
						meetingDTO.getAttendees().stream().map(m -> m.getName()).collect(Collectors.toList())));
			}
			
			notice.setExtMeetingId(meetingDTO.getExtMeetingId());
			notice.setMeetingLinkV(meetingDTO.getVideoMeetingLink());
			notice.setMeetingLinkA(meetingDTO.getAudioMeetingLink());
			notice.setHtmlLink(meetingDTO.getHtmlLink());
			notice.setExtEventStatus(meetingDTO.getExternalStatus());
			notice.setCreatorEmail(meetingDTO.getCreatorEmail());
		}
		
		notice = noticeRepository.save(notice);
		return InfraDTOHelper.convertToNoticeDTO(notice);
	}

	@Override
	public NoticeDTO getNotice(String id) {
		NoticeEntity notice = noticeRepository.findById(id).orElseThrow();
		return InfraDTOHelper.convertToNoticeDTO(notice);
	}

	@Override
	public void deleteNotice(String id) {
		NoticeEntity notice = noticeRepository.findById(id).orElseThrow();
		noticeRepository.delete(notice);
	}

	@Override
	public long getNoticeCount() {
		return noticeRepository.count();
	}

	@Override
	public NoticeDTO updateNotice(String id, NoticeDTO noticeDTO) throws Exception {
		NoticeEntity notice = noticeRepository.findById(id).orElseThrow();
		NoticeEntity updated_notice = new NoticeEntity();
		updated_notice.setCreatorRole(noticeDTO.getCreatorRole());
		updated_notice.setDescription(noticeDTO.getDescription());
		updated_notice.setDraft(false);
		updated_notice.setNeedMeeting(noticeDTO.getNeedMeeting());
		updated_notice.setNoticeDate(noticeDTO.getNoticeDate());
		updated_notice.setNoticeNumber(null);
		updated_notice.setTitle(noticeDTO.getTitle());
		updated_notice.setStatus(noticeDTO.getStatus() == null ? null : noticeDTO.getStatus().name());

		/**
		 * Meeting details
		 */
		/**
		 * Saving Meeting details
		 */ 
		if(noticeDTO.getMeeting() != null) {
			MeetingDTO meetingDTO=noticeDTO.getMeeting();
			updated_notice.setMeetingDescription(meetingDTO.getDescription());
			updated_notice.setMeetingLocation(meetingDTO.getLocation());
			updated_notice.setMeetingStatus(meetingDTO.getStatus().name());
			updated_notice.setMeetingRemarks(meetingDTO.getRemarks());
			updated_notice.setMeetingType(meetingDTO.getType() == null ? null : meetingDTO.getType().name());
			updated_notice.setMeetingDate(meetingDTO.getDate());
			updated_notice.setMeetingStartTime(meetingDTO.getStartTime());
			updated_notice.setMeetingEndTime(meetingDTO.getEndTime()); 
			updated_notice.setMeetingSummary(meetingDTO.getSummary());

			if (meetingDTO.getAttendees() != null && !meetingDTO.getAttendees().isEmpty()) {
				updated_notice.setAttendeeEmails(InfraFieldHelper.stringListToString(
						meetingDTO.getAttendees().stream().map(m -> m.getEmail()).collect(Collectors.toList())));
				updated_notice.setAttendeeNames(InfraFieldHelper.stringListToString(
						meetingDTO.getAttendees().stream().map(m -> m.getName()).collect(Collectors.toList())));
			}
			updated_notice.setExtMeetingId(meetingDTO.getExtMeetingId());
			updated_notice.setMeetingLinkV(meetingDTO.getVideoMeetingLink());
			updated_notice.setMeetingLinkA(meetingDTO.getAudioMeetingLink());
			updated_notice.setHtmlLink(meetingDTO.getHtmlLink());
			updated_notice.setExtEventStatus(meetingDTO.getExternalStatus());
			updated_notice.setCreatorEmail(meetingDTO.getCreatorEmail());
		}
		CommonUtils.copyNonNullProperties(updated_notice, notice);
		notice = noticeRepository.save(notice);
		return InfraDTOHelper.convertToNoticeDTO(notice);

	}

}
