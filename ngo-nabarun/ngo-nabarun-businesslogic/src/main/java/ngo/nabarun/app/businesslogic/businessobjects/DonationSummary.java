package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DonationSummary {
	
	@JsonProperty("currentMonthCollection")
	private String currentMonthCollection;
	
	@JsonProperty("totalOutstandingAmount")
	private Double totalOutstandingAmount;
	
	@JsonProperty("hasOutstanding")
	private boolean hasOutstanding;
	
	@JsonProperty("outstandingAmount")
	private Double outstandingAmount;
	
	@JsonProperty("outstandingMonths")
	private List<String> outstandingMonths;
	
	@JsonProperty("payableAccounts")
	private List<PayableAccDetail> payableAccounts;
	
	@Data
	public static class PayableAccDetail{
		@JsonProperty("bankDetail")
		public BankDetail payableBankDetails;
		
		@JsonProperty("upiDetail")
		public UPIDetail payableUPIDetail;
	}
		
}
