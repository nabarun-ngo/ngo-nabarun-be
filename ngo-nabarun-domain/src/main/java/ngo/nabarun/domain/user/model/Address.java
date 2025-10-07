package ngo.nabarun.domain.user.model;

import lombok.Data;

@Data
public class Address {
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String hometown;
	private String zipCode;
	private String state;
	private String district;
	private String country;
}
