package ngo.nabarun.app.infra.dto;

import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;

@Data
public class FieldDTO {
	private String fieldId;
	private AdditionalFieldKey fieldKey;
	private String fieldName;
	private String fieldType;
	private String fieldValue;
	private String fieldDescription;
	private String fieldSource;
	private String fieldSourceType;
	private boolean hidden;
	private boolean encrypted;
	private boolean mandatory;
	private List<String> fieldOptions;
	private String fieldValueType;



}
