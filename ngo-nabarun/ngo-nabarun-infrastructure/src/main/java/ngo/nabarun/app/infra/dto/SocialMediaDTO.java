package ngo.nabarun.app.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.SocialMediaType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocialMediaDTO {
	private String id;
	private SocialMediaType socialMediaType;
	private String socialMediaName;
	private String socialMediaURL;
	private boolean delete;
}
