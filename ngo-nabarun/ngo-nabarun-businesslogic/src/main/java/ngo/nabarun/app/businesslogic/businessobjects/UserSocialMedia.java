package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.SocialMediaType;

@Data
public class UserSocialMedia {
	@JsonProperty("ref")
	private String id;
	
	@JsonProperty("mediaType")
	private SocialMediaType mediaType;
	
	@JsonProperty("mediaName")
	private String mediaName;
	
	@JsonProperty("mediaIcon")
	private String mediaIcon;
	
	@JsonProperty("mediaLink")
	private String mediaLink;
	
	@JsonProperty("delete")
	private boolean delete;
}
