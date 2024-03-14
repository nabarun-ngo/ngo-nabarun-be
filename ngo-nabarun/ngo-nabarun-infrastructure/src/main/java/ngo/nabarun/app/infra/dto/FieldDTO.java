package ngo.nabarun.app.infra.dto;

import lombok.Data;

@Data
public class FieldDTO {
	private String fieldId;
	private String fieldKey;
	private String fieldName;
	private String fieldType;
	private String fieldValue;
	private String fieldDescription;
	private String fieldSource;
}
