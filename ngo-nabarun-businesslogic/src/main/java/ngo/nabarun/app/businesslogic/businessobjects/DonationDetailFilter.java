package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DonationDetailFilter {
	
	@JsonProperty("isGuest")
	private Boolean isGuest;
	
	@JsonProperty("fromDate")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date fromDate;
	
	@JsonProperty("toDate")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date toDate;
	
	@JsonProperty("donationId")
	private String donationId;
	
	@JsonProperty("donationType")
	private List<DonationType> donationType;
	
	@JsonProperty("donationStatus")
	private List<DonationStatus> donationStatus;
	
	@JsonProperty("donorName")
	private String donorName;
	
	@JsonProperty("paidToAccountId")
	private String paidToAccountId;
	
	@JsonProperty("donorId")
	private String donorId;
	
}
