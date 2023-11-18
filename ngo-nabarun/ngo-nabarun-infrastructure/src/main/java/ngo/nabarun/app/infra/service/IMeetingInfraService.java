package ngo.nabarun.app.infra.service;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.infra.dto.MeetingDTO;

@Service
public interface IMeetingInfraService {
	MeetingDTO getMeeting(String id);
	MeetingDTO createMeeting(MeetingDTO meetingDTO) throws Exception;
	MeetingDTO updateMeeting(String id, MeetingDTO meetingDTO) throws Exception;
	Void deleteMeeting(String id);
	String createAuthorizationLink(String meetingState, String callbackUrl) throws ThirdPartyException;


}
