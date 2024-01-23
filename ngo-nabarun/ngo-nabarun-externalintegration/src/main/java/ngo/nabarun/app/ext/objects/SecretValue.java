package ngo.nabarun.app.ext.objects;

import lombok.Data;

@Data
public class SecretValue {
	private String raw;
	private String computed;
	private String note;
	private String rawVisibility;
	private String computedVisibility;

}
