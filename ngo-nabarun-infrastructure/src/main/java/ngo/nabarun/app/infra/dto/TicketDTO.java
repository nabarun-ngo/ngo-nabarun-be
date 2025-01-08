package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.CommunicationMethod;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.common.util.CommonUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
	@Id
	private String id;
	private List<String> ticketScope;
	private UserDTO userInfo;
	private List<CommunicationMethod> communicationMethods;
	private String refId;
	private String oneTimePassword;
	private Integer incorrectOTPCount;
	private int otpDigits;
	private String token;
	private String baseTicketUrl;
	//private String ticketUrl;
	private TicketStatus ticketStatus;
	private TicketType ticketType; 
	private Boolean expired;
	private int expireTicketAfterSec;
	private String acceptCode;
	private String declineCode;
	private Date expireOn;

	//private String userId;

	
	public TicketDTO(TicketType ticketType) {
		this.ticketType=ticketType;
	}

	public Map<String, Object> toMap(Map<String, String> domainKeyValues) {
		Map<String, Object> ticket = new HashMap<>();
		ticket.put("otp", oneTimePassword);
		ticket.put("ticketUserName", userInfo.getName());
		ticket.put("ticketUser", userInfo);
		ticket.put("ticketScope", ticketScope);
		ticket.put("expireOn", CommonUtils.formatDateToString(expireOn, "dd MMM yyyy", "IST"));
		return ticket;
	}


}
