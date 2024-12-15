package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.TriggerEvent;

@Data
public class ServiceDetail {
	private TriggerEvent name;
	private Map<String,String> parameters;
}
