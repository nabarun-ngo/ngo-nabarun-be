package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.MeetingRefType;
import ngo.nabarun.app.common.enums.MeetingType;

@Data
public class MeetingDetailCreate {
	
	private String meetingSummary;
	private String meetingDescription;
	private String meetingLocation;
	private Date meetingStartTime;
	private Date meetingEndTime;
	private String meetingRefId;
	private MeetingType meetingType;
	private List<UserDetail> meetingAttendees;
	private List<MeetingDiscussion> meetingDiscussions;
	private String meetingRemarks;
	private MeetingRefType meetingRefType;

	private Boolean draft;
	private AuthorizationDetail authorization;

}
