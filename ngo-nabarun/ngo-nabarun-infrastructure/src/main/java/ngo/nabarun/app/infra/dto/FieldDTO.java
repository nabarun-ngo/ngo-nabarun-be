package ngo.nabarun.app.infra.dto;

import lombok.Data;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.AdditionalFieldSource;

@Data
public class FieldDTO {
	private String fieldId;
	private AdditionalFieldKey fieldKey;
	private String fieldName;
	private String fieldType;
	private String fieldValue;
	private String fieldDescription;
	private String fieldSource;
	private AdditionalFieldSource fieldSourceType;
	private boolean hidden;
	private boolean encrypted;
}
