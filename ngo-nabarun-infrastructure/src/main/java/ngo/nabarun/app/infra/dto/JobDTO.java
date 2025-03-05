package ngo.nabarun.app.infra.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.common.enums.JobStatus;
import ngo.nabarun.app.common.util.CommonUtils;

@Slf4j
@Data
public class JobDTO {
	private String id;
	private String name;
	private JobStatus status ;
	private Date start;
	private Date end;
	private Object input;
	private Object output;
	private int queue;
	private Exception error;
	private List<String> logs;
	private String triggerId;
	private String memoryAtStart;
	private String memoryAtEnd;
	private long duration;

	public JobDTO(String triggerId,String name){
		this.triggerId=triggerId;
		this.name=name;
	}
	
	public JobDTO() {
	}
	
	public void log(String msg) {
		if(logs == null) {
			logs= new ArrayList<>();
		}
		String timestamp=CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yyyy-MM-dd HH:mm:ss");
		logs.add(timestamp+" "+msg);
		log.info(msg);
	}
	
	@Data
	public static class JobDTOFilter{
		private String id;
		private String name;
		private List<JobStatus> status ;
		private Date start;
		private Date end;
		private String triggerId;
	}
}
