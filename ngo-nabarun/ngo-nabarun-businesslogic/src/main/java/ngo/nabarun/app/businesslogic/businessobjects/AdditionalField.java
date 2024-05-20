package ngo.nabarun.app.businesslogic.businessobjects;

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
	
	@JsonIgnore
	private boolean encrypted;
	
	@JsonIgnore
	private boolean hidden;
	
	private boolean updateField;

	
	public AdditionalField() {
	}
	
	public AdditionalField(AdditionalFieldKey key, String value, boolean isPublic) {
		super();
		this.key = key;
		this.value = value;
		this.hidden = !isPublic;
		this.encrypted=false;
	}
	
	public AdditionalField(AdditionalFieldKey key, String value, boolean isPublic, boolean encrypted) {
		super();
		this.key = key;
		this.value = value;
		this.hidden = !isPublic;
		this.encrypted=encrypted;
	}
}
