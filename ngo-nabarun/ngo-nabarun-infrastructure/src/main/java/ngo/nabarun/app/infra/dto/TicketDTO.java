package ngo.nabarun.app.infra.dto;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ngo.nabarun.app.common.enums.CommunicationMethod;
import ngo.nabarun.app.common.enums.TicketScope;
import ngo.nabarun.app.common.enums.TicketStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
	@Id
	private String id;
	private List<TicketScope> ticketScope;
	private UserDTO userInfo;
	private List<CommunicationMethod> communicationMethods;
	private String refId;
	private String oneTimePassword;
	private int incorrectOTPCount;
	private int otpDigits;
	private String token;
	private String baseTicketUrl;
	private String ticketUrl;
	private TicketStatus ticketStatus;
	private Boolean expired;
	private int expireTicketAfterSec;


}
