package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.TaskName;

@Data
public class ServiceDetail {
	private TaskName name;
	private Map<String,String> parameters;
}
