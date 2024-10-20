package ngo.nabarun.app.infra.dto;

import java.util.Date;
import lombok.Data;

@Data
public class LogsDTO {

	private String id;
	private String type;
	private String methodName;
	private String inputs;
	private String outputs;
	private Date startTime;
	private Date endTime;
	private String error;
	private long duration;
	private String corelationId;
}
