package ngo.nabarun.app.ext.objects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RazorPayBankList {
	private List<RazorPayBankDetail> data;
	private boolean hasNext;
	private int count;

	@Data
	public static class RazorPayBankDetail {

		@JsonProperty("BANK")
		private String bankName;
		@JsonProperty("IFSC")
		private String ifscCode;
		@JsonProperty("BRANCH")
		private String branchName;
		@JsonProperty("CENTRE")
		private String center;
		@JsonProperty("DISTRICT")
		private String district;
		@JsonProperty("STATE")
		private String state;
		@JsonProperty("ADDRESS")
		private String address;
		@JsonProperty("CONTACT")
		private String contact;
		@JsonProperty("IMPS")
		private boolean impsAvailable;
		@JsonProperty("RTGS")
		private boolean rtgsAvailable;
		@JsonProperty("CITY")
		private String city;
		@JsonProperty("ISO3166")
		private String isoCode;
		@JsonProperty("NEFT")
		private boolean neftAvailable;
		@JsonProperty("MICR")
		private String micr;
		@JsonProperty("UPI")
		private boolean upiAvailable;
		@JsonProperty("SWIFT")
		private String swift;
		@JsonProperty("BANKCODE")
		private String bankcode;

	}
}
