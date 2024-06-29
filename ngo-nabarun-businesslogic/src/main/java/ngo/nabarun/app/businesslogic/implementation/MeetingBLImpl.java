package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ngo.nabarun.app.businesslogic.IMeetingBL;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetail;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetailCreate;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectToDTOConverter;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.MeetingRefType;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.service.IEventInfraService;
import ngo.nabarun.app.infra.service.IMeetingInfraService;
import ngo.nabarun.app.infra.service.INoticeInfraService;

@Service
public class MeetingBLImpl implements IMeetingBL {

	@Autowired
	private IMeetingInfraService meetingInfraService;
	
	@Autowired
	private INoticeInfraService noticeInfraService;
	
	@Autowired
	private IEventInfraService eventInfraService;

	@Override
	public MeetingDetail createMeeting(MeetingDetailCreate meetingDetail) throws Exception {
		
		if(meetingDetail.getDraft() == null || meetingDetail.getDraft() == Boolean.FALSE) {
			Assert.notNull(meetingDetail.getAuthorization(),"authorization must not be null !");
			Assert.notNull(meetingDetail.getAuthorization().getAuthorizationCode(),"authorizationCode must not be null !");
			//Assert.notNull(meetingDetail.getAuthorization().getAuthorizationScope(),"authorizationScope must not be null !");
		}
		Assert.notNull(meetingDetail.getMeetingAttendees(),"attendees must not be null !");
		Assert.notEmpty(meetingDetail.getMeetingAttendees(), "attendees must not be empty !");
		
		if(meetingDetail.getMeetingRefType() == MeetingRefType.NOTICE) {
			noticeInfraService.getNotice(meetingDetail.getMeetingRefId());
		}else if(meetingDetail.getMeetingRefType() == MeetingRefType.EVENT) {
			eventInfraService.getEvent(meetingDetail.getMeetingRefId());
		}
		
		MeetingDTO meetingDTO=BusinessObjectToDTOConverter.toMeetingDTO(meetingDetail);
		meetingDTO.setDefaultReminder(false);
		meetingDTO.setEmailReminderBeforeMin(List.of(60,24*60));
		meetingDTO.setPopupReminderBeforeMin(List.of(15,60));
		meetingDTO=meetingInfraService.createMeeting(meetingDTO);
		return BusinessObjectConverter.toMeetingDetail(meetingDTO);
	}

	@Override
	public MeetingDetail getMeetingDetail(String id) throws BusinessException, Exception {
		return BusinessObjectConverter.toMeetingDetail(meetingInfraService.getMeeting(id));
	}
}
