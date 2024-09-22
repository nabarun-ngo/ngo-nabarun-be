package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;

@Data
public class AdditionalField {
	private String id;
	
	private AdditionalFieldKey key;
	private String name;
	private String type;
	private String value;
	private List<String> options;

	private boolean mandatory;
	private String valueType;

	
	@JsonIgnore
	private boolean encrypted;
	
	@JsonIgnore
	private boolean hidden;
	
	private boolean updateField;

	
	public AdditionalField() {
	}
	
	public AdditionalField(AdditionalFieldKey key, String value) {
		super();
		this.key = key;
		this.value = value;
//		this.hidden = !isPublic;
//		this.encrypted=false;
	}
	
//	public AdditionalField(AdditionalFieldKey key, String value, boolean isPublic, boolean encrypted) {
//		super();
//		this.key = key;
//		this.value = value;
//		this.hidden = !isPublic;
//		this.encrypted=encrypted;
//	}
}
