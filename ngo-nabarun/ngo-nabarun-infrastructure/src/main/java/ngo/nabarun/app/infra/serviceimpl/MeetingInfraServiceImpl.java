package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.enums.MeetingStatus;
import ngo.nabarun.app.common.enums.MeetingType;
import ngo.nabarun.app.common.exception.NotFoundException;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.CalendarEvent;
import ngo.nabarun.app.ext.service.IGoogleCalendarExtService;
import ngo.nabarun.app.infra.core.entity.MeetingAdditionalInfoEntity;
import ngo.nabarun.app.infra.core.entity.MeetingEntity;
import ngo.nabarun.app.infra.core.repo.MeetingAdditionalInfoRepository;
import ngo.nabarun.app.infra.core.repo.MeetingInfoRepository;
import ngo.nabarun.app.infra.dto.DiscussionDTO;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.InfraFieldHelper;
import ngo.nabarun.app.infra.service.IMeetingInfraService;
import ngo.nabarun.app.infra.service.ITicketInfraService;

@Service
public class MeetingInfraServiceImpl implements IMeetingInfraService {

	@Autowired
	private MeetingInfoRepository meetingInfoRepo;

	@Autowired
	private MeetingAdditionalInfoRepository meetingAdditionalInfoRepo;

	@Autowired
	private IGoogleCalendarExtService googleCalendarExtService;
	
	private static final String EXT_EVENT_STATUS_CREATED="created";

	@Override
	public MeetingDTO getMeeting(String id) {
		MeetingEntity meeting=meetingInfoRepo.findById(id).orElseThrow(()-> new NotFoundException("meeting", id));
		return InfraDTOHelper.convertToMeetingDTO(meeting);
	}

	@Override
	public MeetingDTO createMeeting(MeetingDTO meetingDTO) throws Exception {
		MeetingEntity meeting = new MeetingEntity();
		meeting.setDescription(meetingDTO.getDescription());
		meeting.setLocation(meetingDTO.getLocation());
		meeting.setMeetingStatus(MeetingStatus.CREATED_L.name());
		meeting.setMeetingRefId(meetingDTO.getRefId());
		meeting.setMeetingRefType(meetingDTO.getRefType() == null ? null : meetingDTO.getRefType().name());
		meeting.setRemarks(meetingDTO.getRemarks());
		meeting.setType(meetingDTO.getType() == null ? null : meetingDTO.getType().name());

		meeting.setStartTime(meetingDTO.getStartTime());
		meeting.setEndTime(meetingDTO.getEndTime());
		meeting.setSummary(meetingDTO.getSummary());

		meeting.setDefaultReminder(meetingDTO.isDefaultReminder());
		meeting.setPopupReminderBeforeMin(InfraFieldHelper
				.integerListToString(meetingDTO.getPopupReminderBeforeMin()));
		meeting.setEmailReminderBeforeMin(InfraFieldHelper
				.integerListToString(meetingDTO.getEmailReminderBeforeMin()));
		meeting.setDraft(meetingDTO.isDraft());	
		
		meeting.setCallbackUrl(meetingDTO.getAuthCallbackUrl());//update
		meeting.setAuthUrl(meetingDTO.getAuthUrl());//update
		meeting = meetingInfoRepo.save(meeting);
		List<MeetingAdditionalInfoEntity> additionalInfo = new ArrayList<>();

		if (meetingDTO.getDiscussions() != null) {
			for (DiscussionDTO meet : meetingDTO.getDiscussions()) {
				MeetingAdditionalInfoEntity discussion = new MeetingAdditionalInfoEntity();
				discussion.setDiscussionDetail(true);
				discussion.setAgenda(meet.getAgenda());
				discussion.setMinutes(meet.getMinutes());
				discussion.setMeeting(meeting);
				discussion = meetingAdditionalInfoRepo.save(discussion);
				additionalInfo.add(discussion);
			}
		}


		if (meetingDTO.getAttendees() != null) {
			for (UserDTO attend : meetingDTO.getAttendees()) {
				MeetingAdditionalInfoEntity attendee = new MeetingAdditionalInfoEntity();
				attendee.setAttendeeDetail(true);
				attendee.setAttendeeEmail(attend.getEmail());
				attendee.setAttendeeName(attend.getFirstName() + " " + attend.getLastName());
				attendee.setMeeting(meeting);
				attendee = meetingAdditionalInfoRepo.save(attendee);
				additionalInfo.add(attendee);
			}

		}
		meeting.setAdditionalDetails(additionalInfo);
//		if (meetingDTO.isDraft() && !EXT_EVENT_STATUS_CREATED.equalsIgnoreCase(meeting.getExtEventStatus())) {
//			
//		}
		if (!meetingDTO.isDraft()) {
			createCalenderEventAndUpdateMeeting(meeting, meetingDTO.getAuthCode() , meeting.getCallbackUrl());
		}

		return InfraDTOHelper.convertToMeetingDTO(meeting);

//		String state = Base64.getEncoder().encodeToString(meeting.getId().getBytes());
//		String authLink = googleCalendarExtService.getAuthorizationUrl(meetingDTO.getAuthCallbackUrl(), state);
//		if (meetingDTO.isDraft()) {
//			
//		} else {
//			String id = new String(Base64.getDecoder().decode(meetingDTO.getAuthState()));
//			MeetingEntity meetingDetail = meetingInfoRepo.findById(id).orElseThrow();
//			if () {
//				
//			}
//		}
	}
	
	private MeetingEntity createCalenderEventAndUpdateMeeting(MeetingEntity meeting,String authCode,String callbackUrl) throws Exception {
		CalendarEvent event = new CalendarEvent();
		event.setId(meeting.getExtMeetingId() == null ?  null : meeting.getExtMeetingId());	
		event.setAction(meeting.getExtMeetingId() == null ?  "create" : "update");
		event.setUseDefaultReminder(meeting.isDefaultReminder());
		if (!meeting.isDefaultReminder()) {
			event.setEmailReminderBeforeMinutes(List.of(meeting.getEmailReminderBeforeMin().split(","))
					.stream().map(m -> Integer.parseInt(m)).toList());
			event.setPopupReminderBeforeMinutes(List.of(meeting.getPopupReminderBeforeMin().split(","))
					.stream().map(m -> Integer.parseInt(m)).toList());
		}
		List<MeetingAdditionalInfoEntity> additionalDetails = meeting.getAdditionalDetails();
		event.setAttendees(additionalDetails.stream().filter(f -> f.isAttendeeDetail())
				.map(m -> Map.of("name", m.getAttendeeName(), "email", m.getAttendeeEmail())).toList());
		event.setDescription(meeting.getDescription());
		event.setEndTime(meeting.getEndTime());
		event.setLocation(meeting.getLocation());
		event.setSourceId(meeting.getMeetingRefId());
		event.setStartTime(meeting.getStartTime());
		event.setSummary(meeting.getSummary());
		event.setConferenceType(MeetingType.valueOf(meeting.getType()));
		event.setTimeZone("Asia/Kolkata");
		try {
			event = googleCalendarExtService.createCalendarEvent(authCode,
					callbackUrl, event);
			meeting.setExtMeetingId(event.getId());
			System.out.println("link"+event.getConferenceLink());
			System.out.println("type "+event.getConferenceType());
			meeting.setMeetingLinkA(
					event.getConferenceType() == MeetingType.ONLINE_AUDIO ? event.getConferenceLink() : null);
			meeting.setMeetingLinkV(
					event.getConferenceType() == MeetingType.ONLINE_VIDEO ? event.getConferenceLink() : null);
			meeting.setHtmlLink(event.getHtmlLink());
			meeting.setExtEventStatus(event.getStatus());
			meeting.setMeetingStatus(MeetingStatus.CREATED_G.name());
			meeting.setCreatorEmail(event.getCreatorEmail());
			meeting.setAdditionalDetails(null);
			meeting.setDraft(false);	
			meeting = meetingInfoRepo.save(meeting);
		} catch (Exception e) {
			meeting.setMeetingStatus(MeetingStatus.FAILED_G.name());
			meeting.setErrorDetails(e.toString());
			meeting.setAdditionalDetails(null);
			meeting = meetingInfoRepo.save(meeting);
			throw new Exception(e);
		}
		return meeting;
	}

	@Override
	public MeetingDTO updateMeeting(String id, MeetingDTO meetingDTO) throws Exception {
		MeetingEntity meeting=meetingInfoRepo.findById(id).orElseThrow(()-> new NotFoundException("meeting", id));
		
		MeetingEntity updatedMeeting = new MeetingEntity();
		updatedMeeting.setDescription(meetingDTO.getDescription());
		updatedMeeting.setLocation(meetingDTO.getLocation());
		updatedMeeting.setRemarks(meetingDTO.getRemarks());

		updatedMeeting.setStartTime(meetingDTO.getStartTime());
		updatedMeeting.setEndTime(meetingDTO.getEndTime());
		updatedMeeting.setSummary(meetingDTO.getSummary());
		updatedMeeting.setDraft(meetingDTO.isDraft());	
		
		updatedMeeting.setCallbackUrl(meetingDTO.getAuthCallbackUrl());
		updatedMeeting.setAuthUrl(meetingDTO.getAuthUrl());
		CommonUtils.copyNonNullProperties(updatedMeeting, meeting);
		if(meeting.isDraft() && !meetingDTO.isDraft() && !"created".equalsIgnoreCase(meeting.getExtEventStatus())) {
			meeting=createCalenderEventAndUpdateMeeting(meeting,meetingDTO.getAuthCode(),meeting.getCallbackUrl());
		}else {
			meeting = meetingInfoRepo.save(meeting);
		}
		
		return null;
	}

	@Override
	public Void deleteMeeting(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createAuthorizationLink(String meetingState, String callbackUrl) throws ThirdPartyException {
		return googleCalendarExtService.getAuthorizationUrl(meetingState,callbackUrl);
	}

}
