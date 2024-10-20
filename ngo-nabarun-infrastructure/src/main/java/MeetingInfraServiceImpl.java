//package ngo.nabarun.app.infra.serviceimpl;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import ngo.nabarun.app.common.enums.MeetingStatus;
//import ngo.nabarun.app.common.enums.MeetingType;
//import ngo.nabarun.app.common.exception.NotFoundException;
//import ngo.nabarun.app.common.util.CommonUtils;
//import ngo.nabarun.app.ext.exception.ThirdPartyException;
//import ngo.nabarun.app.ext.objects.CalendarEvent;
//import ngo.nabarun.app.ext.service.IGoogleCalendarExtService;
//import ngo.nabarun.app.infra.core.entity.MeetingAdditionalInfoEntity;
//import ngo.nabarun.app.infra.core.entity.LogsEntity;
//import ngo.nabarun.app.infra.core.repo.MeetingAdditionalInfoRepository;
//import ngo.nabarun.app.infra.core.repo.LogsRepository;
//import ngo.nabarun.app.infra.dto.DiscussionDTO;
//import ngo.nabarun.app.infra.dto.LogsDTO;
//import ngo.nabarun.app.infra.dto.UserDTO;
//import ngo.nabarun.app.infra.misc.InfraDTOHelper;
//import ngo.nabarun.app.infra.misc.InfraFieldHelper;
//import ngo.nabarun.app.infra.service.IMeetingInfraService;
//
//@Service
//public class MeetingInfraServiceImpl implements IMeetingInfraService {
//
//	@Autowired
//	private LogsRepository meetingInfoRepo;
//
////	@Autowired
////	private MeetingAdditionalInfoRepository meetingAdditionalInfoRepo;
//
//	@Autowired
//	private IGoogleCalendarExtService googleCalendarExtService;
//
//	@Override
//	public LogsDTO getMeeting(String id) {
//		LogsEntity meeting = meetingInfoRepo.findById(id).orElseThrow(() -> new NotFoundException("meeting", id));
//		return InfraDTOHelper.convertToMeetingDTO(meeting);
//	}
//
//	@Override
//	public LogsDTO createMeeting(LogsDTO meetingDTO) throws Exception {
//		LogsEntity meeting = new LogsEntity();
//		meeting.setId(UUID.randomUUID().toString());
//		meeting.setDescription(meetingDTO.getDescription());
//		meeting.setLocation(meetingDTO.getLocation());
//		meeting.setMeetingStatus(meetingDTO.getStatus().name());
//		meeting.setMeetingRefId(meetingDTO.getRefId());
//		meeting.setMeetingRefType(meetingDTO.getRefType() == null ? null : meetingDTO.getRefType().name());
//		meeting.setRemarks(meetingDTO.getRemarks());
//		meeting.setType(meetingDTO.getType() == null ? null : meetingDTO.getType().name());
//		meeting.setMeetingDate(meetingDTO.getDate());
//		meeting.setStartTime(meetingDTO.getStartTime());
//		meeting.setEndTime(meetingDTO.getEndTime()); 
//		meeting.setSummary(meetingDTO.getSummary());
//
////		meeting.setDefaultReminder(meetingDTO.isDefaultReminder());
////		meeting.setPopupReminderBeforeMin(InfraFieldHelper.integerListToString(meetingDTO.getPopupReminderBeforeMin()));
////		meeting.setEmailReminderBeforeMin(InfraFieldHelper.integerListToString(meetingDTO.getEmailReminderBeforeMin()));
//		meeting.setDraft(meetingDTO.isDraft());
//
////		meeting.setCallbackUrl(meetingDTO.getAuthCallbackUrl());// update
////		meeting.setAuthUrl(meetingDTO.getAuthUrl());// update
//
//		if (meetingDTO.getAttendees() != null && !meetingDTO.getAttendees().isEmpty()) {
//			meeting.setAttendeeEmails(InfraFieldHelper.stringListToString(
//					meetingDTO.getAttendees().stream().map(m -> m.getEmail()).collect(Collectors.toList())));
//			meeting.setAttendeeNames(InfraFieldHelper.stringListToString(
//					meetingDTO.getAttendees().stream().map(m -> m.getName()).collect(Collectors.toList())));
//		}
//		meeting = meetingInfoRepo.save(meeting);
//
//		return InfraDTOHelper.convertToMeetingDTO(meeting);
//	}
//
//	@Override
//	public LogsDTO updateMeeting(String id, LogsDTO meetingDTO) throws Exception {
//		LogsEntity meeting = meetingInfoRepo.findById(id).orElseThrow(() -> new NotFoundException("meeting", id));
//
//		LogsEntity updatedMeeting = new LogsEntity();
//		updatedMeeting.setDescription(meetingDTO.getDescription());
//		updatedMeeting.setLocation(meetingDTO.getLocation());
//		updatedMeeting.setRemarks(meetingDTO.getRemarks());
//		updatedMeeting.setMeetingDate(meetingDTO.getDate());
//
//		updatedMeeting.setStartTime(meetingDTO.getStartTime());
//		updatedMeeting.setEndTime(meetingDTO.getEndTime());
//		updatedMeeting.setSummary(meetingDTO.getSummary());
//		updatedMeeting.setDraft(meetingDTO.isDraft());
//
////		updatedMeeting.setCallbackUrl(meetingDTO.getAuthCallbackUrl());
////		updatedMeeting.setAuthUrl(meetingDTO.getAuthUrl());
//		updatedMeeting.setExtMeetingId(meetingDTO.getExtMeetingId());
//		updatedMeeting.setHtmlLink(meetingDTO.getHtmlLink());
//		
//		if (meetingDTO.getAttendees() != null && !meetingDTO.getAttendees().isEmpty()) {
//			meeting.setAttendeeEmails(InfraFieldHelper.stringListToString(
//					meetingDTO.getAttendees().stream().map(m -> m.getEmail()).collect(Collectors.toList())));
//			meeting.setAttendeeNames(InfraFieldHelper.stringListToString(
//					meetingDTO.getAttendees().stream().map(m -> m.getName()).collect(Collectors.toList())));
//		}
//		updatedMeeting.setCreatorEmail(null);
//		updatedMeeting.setMeetingLinkA(meetingDTO.getAudioMeetingLink());
//		updatedMeeting.setMeetingLinkV(meetingDTO.getVideoMeetingLink());
//		updatedMeeting.setMeetingRefId(meetingDTO.getRefId());
//		updatedMeeting.setMeetingRefType(meetingDTO.getRefType() == null ? null : meetingDTO.getRefType().name());
//		updatedMeeting.setMeetingStatus(meetingDTO.getStatus() == null ? null : meetingDTO.getStatus().name());
//		updatedMeeting.setExtEventStatus(meetingDTO.getExternalStatus());
//		CommonUtils.copyNonNullProperties(updatedMeeting, meeting);
//		meeting = meetingInfoRepo.save(meeting);
//
//		return InfraDTOHelper.convertToMeetingDTO(meeting);
//	}
//	
//	@Override
//	public void deleteMeeting(String id) {
//		meetingInfoRepo.deleteById(id);;
//	}
//
//	@Deprecated
//	private LogsEntity createCalenderEventAndUpdateMeeting(LogsEntity meeting, String authCode,
//			String callbackUrl) throws Exception {
////		CalendarEvent event = new CalendarEvent();
////		event.setId(meeting.getExtMeetingId() == null ? null : meeting.getExtMeetingId());
////		event.setAction(meeting.getExtMeetingId() == null ? "create" : "update");
////		event.setUseDefaultReminder(meeting.isDefaultReminder());
////		if (!meeting.isDefaultReminder()) {
////			event.setEmailReminderBeforeMinutes(List.of(meeting.getEmailReminderBeforeMin().split(",")).stream()
////					.map(m -> Integer.parseInt(m)).toList());
////			event.setPopupReminderBeforeMinutes(List.of(meeting.getPopupReminderBeforeMin().split(",")).stream()
////					.map(m -> Integer.parseInt(m)).toList());
////		}
////		List<MeetingAdditionalInfoEntity> additionalDetails = meeting.getAdditionalDetails();
////		event.setAttendees(additionalDetails.stream().filter(f -> f.isAttendeeDetail())
////				.map(m -> Map.of("name", m.getAttendeeName(), "email", m.getAttendeeEmail())).toList());
////		event.setDescription(meeting.getDescription());
////		// event.setEndTime(meeting.getEndTime());
////		event.setLocation(meeting.getLocation());
////		event.setSourceId(meeting.getMeetingRefId());
////		// event.setStartTime(meeting.getStartTime());
////		event.setSummary(meeting.getSummary());
////		event.setConferenceType(MeetingType.valueOf(meeting.getType()));
////		event.setTimeZone("Asia/Kolkata");
////		try {
////			event = googleCalendarExtService.createCalendarEvent(authCode, callbackUrl, event);
////			meeting.setExtMeetingId(event.getId());
////			System.out.println("link" + event.getConferenceLink());
////			System.out.println("type " + event.getConferenceType());
////			meeting.setMeetingLinkA(
////					event.getConferenceType() == MeetingType.ONLINE_AUDIO ? event.getConferenceLink() : null);
////			meeting.setMeetingLinkV(
////					event.getConferenceType() == MeetingType.ONLINE_VIDEO ? event.getConferenceLink() : null);
////			meeting.setHtmlLink(event.getHtmlLink());
////			meeting.setExtEventStatus(event.getStatus());
////			meeting.setMeetingStatus(MeetingStatus.CREATED_G.name());
////			meeting.setCreatorEmail(event.getCreatorEmail());
////			meeting.setAdditionalDetails(null);
////			meeting.setDraft(false);
////			meeting = meetingInfoRepo.save(meeting);
////		} catch (Exception e) {
////			meeting.setMeetingStatus(MeetingStatus.FAILED_G.name());
////			meeting.setErrorDetails(e.toString());
////			meeting.setAdditionalDetails(null);
////			meeting = meetingInfoRepo.save(meeting);
////			throw new Exception(e);
////		}
//		return meeting;
//	}
//
//	@Override
//	@Deprecated
//	public String createAuthorizationLink(String meetingState, String callbackUrl) throws ThirdPartyException {
//		return googleCalendarExtService.getAuthorizationUrl(meetingState, callbackUrl);
//	}
//
//	
//
//}

//
//	public static LogsDTO convertToMeetingDTO(LogsEntity meetEntity) {
//		LogsDTO meetingDTO = new LogsDTO();
//		meetingDTO.setAudioMeetingLink(meetEntity.getMeetingLinkA());
//		meetingDTO.setDescription(meetEntity.getDescription());
//
//		
//		if(meetEntity.getAttendeeEmails() != null) {
//			List<UserDTO> attendees= new ArrayList<>();
//			List<String> attendeesEmail=InfraFieldHelper.stringToStringList(meetEntity.getAttendeeEmails());
//			List<String> attendeesNames=InfraFieldHelper.stringToStringList(meetEntity.getAttendeeNames());
//			
//			for(int i=0;i<attendeesEmail.size();i++) {
//				UserDTO attendee = new UserDTO();
//				attendee.setEmail(attendeesEmail.get(i));	
//				attendee.setName(attendeesNames.get(i));
//				attendees.add(attendee);
//			}
//			meetingDTO.setAttendees(attendees);
//		}
//		
//		
//
//		meetingDTO.setEndTime(meetEntity.getEndTime());
//		meetingDTO.setExtMeetingId(meetEntity.getExtMeetingId());
//		meetingDTO.setLocation(meetEntity.getLocation());
//		meetingDTO.setRefId(null);
//		meetingDTO.setRefType(null);
//		meetingDTO.setRemarks(meetEntity.getRemarks());
//		meetingDTO.setStartTime(meetEntity.getStartTime());
//		meetingDTO.setStatus(MeetingStatus.valueOf(meetEntity.getMeetingStatus()));
//		meetingDTO.setSummary(meetEntity.getSummary());
//		meetingDTO.setType(MeetingType.valueOf(meetEntity.getType()));
//		meetingDTO.setVideoMeetingLink(meetEntity.getMeetingLinkV());
//		meetingDTO.setHtmlLink(meetEntity.getHtmlLink());
//		meetingDTO.setExternalStatus(meetEntity.getExtEventStatus());
//		meetingDTO.setDate(meetEntity.getMeetingDate()); 

//		if (meetEntity.getEmailReminderBeforeMin() != null) {
//			meetingDTO.setEmailReminderBeforeMin(
//					InfraFieldHelper.stringToIntegerList(meetEntity.getEmailReminderBeforeMin()));
//		}
//		if (meetEntity.getPopupReminderBeforeMin() != null) {
//			meetingDTO.setPopupReminderBeforeMin(
//					InfraFieldHelper.stringToIntegerList(meetEntity.getPopupReminderBeforeMin()));
//		}
//
//import java.util.ArrayList;
//import java.util.List;
//
//import ngo.nabarun.app.common.enums.MeetingStatus;
//import ngo.nabarun.app.common.enums.MeetingType;
//import ngo.nabarun.app.infra.core.entity.LogsEntity;
//import ngo.nabarun.app.infra.dto.LogsDTO;
//import ngo.nabarun.app.infra.dto.UserDTO;
//import ngo.nabarun.app.infra.misc.InfraFieldHelper;
//
//return meetingDTO;
//	}
