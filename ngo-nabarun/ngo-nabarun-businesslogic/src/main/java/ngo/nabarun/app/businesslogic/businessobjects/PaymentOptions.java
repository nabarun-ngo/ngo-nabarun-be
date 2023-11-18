package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaymentOptions {
	@JsonProperty("donationId")
	private String donationId;
	
	@JsonProperty("payeeAccountId")
	private String payeeAccountId;
	
	@JsonProperty("paymentMenthod")
	private String paymentMenthod;
	
}
