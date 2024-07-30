package ngo.nabarun.app.businesslogic.domain;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.common.enums.IdType;
import ngo.nabarun.app.common.enums.MeetingRefType;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO.NoticeDTOFilter;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.service.INoticeInfraService;
import ngo.nabarun.app.infra.service.IUserInfraService;

@Service
public class NoticeDO extends CommonDO{
	
	@Autowired
	private INoticeInfraService noticeInfraService;
	
	@Autowired
	private IUserInfraService userInfraService;
	
	public Paginate<NoticeDTO> retrieveNotices(Integer page, Integer size, NoticeDetailFilter filter) {
		NoticeDTOFilter noticeDTOFilter = null;
		if(filter != null) {
			noticeDTOFilter= new NoticeDTOFilter();
			noticeDTOFilter.setTitle(filter.getTitle());
			noticeDTOFilter.setId(filter.getId());
			noticeDTOFilter.setFromDate(filter.getStartDate());
			noticeDTOFilter.setToDate(filter.getEndDate());
			noticeDTOFilter.setStatus(filter.getStatus());
		}
		Page<NoticeDTO> content =noticeInfraService.getNoticeList(page, size, noticeDTOFilter);
		return new Paginate<NoticeDTO>(content);
	}

	public NoticeDTO retrieveNotice(String id) {
		return noticeInfraService.getNotice(id);
	}

	public NoticeDTO createNotice(NoticeDetail noticeDetail,boolean isTemplate) throws Exception {
		NoticeDTO noticeDTO = new NoticeDTO();
		noticeDTO.setTitle(noticeDetail.getTitle());
		noticeDTO.setDescription(noticeDetail.getDescription());
		noticeDTO.setNoticeDate(noticeDetail.getNoticeDate());
		noticeDTO.setStatus(noticeDetail.getNoticeStatus());
		UserDTO auth_user=userInfraService.getUser(SecurityUtils.getAuthUserId(), IdType.AUTH_USER_ID, false);
		noticeDTO.setCreatedBy(auth_user);
		//noticeDTO.setCreatorRole(auth_user.getRoles().);

		noticeDTO.setTemplateNotice(isTemplate);
		
		
		MeetingDTO meetingDTO = null;
		noticeDTO.setNeedMeeting(noticeDetail.isHasMeeting());
		if(noticeDetail.isHasMeeting()) {
			MeetingDetail meetingDetail= noticeDetail.getMeeting();
			meetingDTO = new MeetingDTO();
			meetingDTO.setDescription(meetingDetail.getMeetingDescription());
			meetingDTO.setDate(meetingDetail.getMeetingDate());
			meetingDTO.setEndTime(meetingDetail.getMeetingEndTime());
			List<UserDTO> attendees = new ArrayList<>();
			if (meetingDetail.getMeetingAttendees() != null) {
				meetingDetail.getMeetingAttendees().forEach(user -> {
					UserDTO attendee= new UserDTO();
					attendee.setProfileId(user.getId());
					attendee.setName(user.getFullName());
					attendee.setEmail(user.getEmail());
					attendees.add(attendee);
				});
			}
			meetingDTO.setAttendees(attendees);
			//meetingDTO.setDefaultReminder(true);
			meetingDTO.setLocation(meetingDetail.getMeetingLocation());
			meetingDTO.setStartTime(meetingDetail.getMeetingStartTime());
			meetingDTO.setSummary(meetingDetail.getMeetingSummary());
			meetingDTO.setType(meetingDetail.getMeetingType());
			meetingDTO.setRefId(meetingDetail.getMeetingRefId());
			meetingDTO.setRefType(MeetingRefType.NOTICE);
			meetingDTO.setRemarks(meetingDetail.getMeetingRemarks());
			meetingDTO.setStatus(meetingDetail.getMeetingStatus());
			meetingDTO.setExtMeetingId(meetingDetail.getExtMeetingId());
			meetingDTO.setVideoMeetingLink(meetingDetail.getExtVideoConferenceLink());
			meetingDTO.setAudioMeetingLink(meetingDetail.getExtAudioConferenceLink());
			meetingDTO.setHtmlLink(meetingDetail.getExtHtmlLink());
			meetingDTO.setExternalStatus(meetingDetail.getExtConferenceStatus());

			noticeDTO.setMeeting(meetingDTO);
			
		}
		noticeDTO.setId(generateNoticeId());
		noticeDTO=noticeInfraService.createNotice(noticeDTO);
		return noticeDTO;
	}
	
	public NoticeDTO updateNotice(String id,NoticeDetail noticeDetail) throws Exception {
		NoticeDTO noticeDTO = new NoticeDTO();
		noticeDTO.setTitle(noticeDetail.getTitle());
		noticeDTO.setDescription(noticeDetail.getDescription());
		noticeDTO.setNoticeDate(noticeDetail.getNoticeDate());
		noticeDTO.setStatus(noticeDetail.getNoticeStatus());
		
		

		MeetingDTO meetingDTO = null;
		if(noticeDetail.isHasMeeting()) {
			MeetingDetail meetingDetail= noticeDetail.getMeeting();
			meetingDTO = new MeetingDTO();
			meetingDTO.setDescription(meetingDetail.getMeetingDescription());
			meetingDTO.setDate(meetingDetail.getMeetingDate());
			meetingDTO.setEndTime(meetingDetail.getMeetingEndTime());
			List<UserDTO> attendees = new ArrayList<>();
			if (meetingDetail.getMeetingAttendees() != null) {
				meetingDetail.getMeetingAttendees().forEach(user -> {
					UserDTO attendee= new UserDTO();
					attendee.setProfileId(user.getId());
					attendee.setName(user.getFullName());
					attendee.setEmail(user.getEmail());
					attendees.add(attendee);
				});
			}
			meetingDTO.setAttendees(attendees);
			//meetingDTO.setDefaultReminder(true);
			meetingDTO.setLocation(meetingDetail.getMeetingLocation());
			meetingDTO.setStartTime(meetingDetail.getMeetingStartTime());
			meetingDTO.setSummary(meetingDetail.getMeetingSummary());
			meetingDTO.setRemarks(meetingDetail.getMeetingRemarks());
			meetingDTO.setStatus(meetingDetail.getMeetingStatus());
			meetingDTO.setRefId(meetingDetail.getMeetingRefId());
			meetingDTO.setExtMeetingId(meetingDetail.getExtMeetingId());
			meetingDTO.setVideoMeetingLink(meetingDetail.getExtVideoConferenceLink());
			meetingDTO.setAudioMeetingLink(meetingDetail.getExtAudioConferenceLink());
			meetingDTO.setHtmlLink(meetingDetail.getExtHtmlLink());
			meetingDTO.setExternalStatus(meetingDetail.getExtConferenceStatus());
			meetingDTO.setCreatorEmail(meetingDetail.getCreatorEmail());;
			noticeDTO.setMeeting(meetingDTO);
		}
		noticeDTO=noticeInfraService.updateNotice(id,noticeDTO);
		return noticeDTO;
	}

}
