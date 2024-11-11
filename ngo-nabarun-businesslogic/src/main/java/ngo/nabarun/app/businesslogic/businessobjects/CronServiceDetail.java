package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.TriggerEvent;

@Data
public class CronServiceDetail {
	private TriggerEvent triggerName;
	private Map<String,String> parameters;
}
