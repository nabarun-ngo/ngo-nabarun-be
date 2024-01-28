package ngo.nabarun.app.infra.misc;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Deprecated
public class DonationConfigTemplate {

	@JsonProperty("REGULAR_DONATION_AMOUNT")
	private int regularDonationAmount;
	
	@JsonProperty("LAST_DONATION_PAYMENT_DATE")
	private int lastDonationPaymentDate;
	
	@JsonProperty("DONATION_TYPES")
	private List<KeyValuePair> donationTypes;
	
	@JsonProperty("DONATION_STATUSES")
	private List<KeyValuePair> donationStatuses;
	
	@JsonProperty("PAYMENT_METHODS")
	private List<KeyValuePair> paymentMethods;
	
	@JsonProperty("UPI_OPTIONS")
	private List<KeyValuePair> UPIOptions;
}
