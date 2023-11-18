package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationType;

@Data
public class DonationDetailCreate {
	
	@JsonProperty("isGuest")
	private Boolean isGuest;
	
	@JsonProperty("amount")
	private Double amount;
	
	@JsonProperty("startDate")
	private Date startDate;
	
	@JsonProperty("endDate")
	private Date endDate;
	
	@JsonProperty("donationType")
	private DonationType donationType;
	
	@JsonProperty("donorId")
	private String donorId;
	
	@JsonProperty("donorName")
	private String donorName;
	
	@JsonProperty("donorEmail")
	private String donorEmail;
	
	@JsonProperty("donorMobile")
	private String donorMobile;
	
	@JsonProperty("eventId")
	private String eventId;
}
