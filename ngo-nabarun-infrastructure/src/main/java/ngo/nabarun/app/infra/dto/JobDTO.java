package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.JobStatus;

@Data
public class JobDTO<Input,Output> {
	private String id;
	private String name;
	private JobStatus status ;
	private Date start;
	private Date end;
	private Input input;
	private Output output;
	private int queue;
	private Exception error;
	private List<String> log;
	private String triggerId;
	private String memoryAtStart;
	private String memoryAtEnd;
	private long duration;

	public JobDTO(String triggerId,String name){
		this.triggerId=triggerId;
		this.name=name;
	}
}
