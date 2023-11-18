package ngo.nabarun.app.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.EmailRecipientType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondentDTO {
	
	private String name;
	private String email;
	private String mobile;
	private EmailRecipientType emailRecipientType;

	
	
}
