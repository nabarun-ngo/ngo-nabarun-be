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
	
	@JsonProperty("memberOutstandingAmount")
	private Double memberOutstandingAmount;
	
	@JsonProperty("memberOutstandingAmount")
	private List<String> memberOutstandingMonths;
		
}
