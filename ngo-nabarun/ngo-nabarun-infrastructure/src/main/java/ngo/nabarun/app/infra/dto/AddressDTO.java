package ngo.nabarun.app.infra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.AddressType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
	private String id;
	private AddressType addressType;
	private String addressLine;
	private String hometown;
	private String state;
	private String district;
	private String country;
	private boolean delete;
}
