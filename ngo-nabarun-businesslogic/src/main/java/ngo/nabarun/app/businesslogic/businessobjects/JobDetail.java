package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.JobStatus;

@Data
public class JobDetail {
	private String id;
	private String name;
	private JobStatus status ;
	private Date submitAt;
	private Date startAt;
	private Date endAt;
	private Object input;
	private Object output;
	private long queue;
	private Exception error;
	private List<String> logs;
	private String triggerId;
	private String memoryAtStart;
	private String memoryAtEnd;
	private long duration;
	
	@Data
	public static class JobDetailFilter {
		private String id;
		private String name;
		private List<JobStatus> status ;
		private Date start;
		private Date end;
		private String triggerId;
	}
}
