package ngo.nabarun.app.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.PhoneType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneDTO {
	private String id;
	private PhoneType phoneType;
	private String phoneCode;
	private String phoneNumber;
	private boolean primary;
	private boolean delete;
}
