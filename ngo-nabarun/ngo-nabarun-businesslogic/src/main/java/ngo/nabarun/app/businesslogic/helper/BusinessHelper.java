package ngo.nabarun.app.businesslogic.helper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.common.enums.CommunicationMethod;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.service.ICorrespondenceInfraService;
import ngo.nabarun.app.infra.service.ISequenceInfraService;
import ngo.nabarun.app.infra.service.ITicketInfraService;

@Service
public class BusinessHelper extends BusinessDomainHelper{

	@Autowired
	private ISequenceInfraService sequenceInfraService;
	
	@Autowired
	private ITicketInfraService ticketInfraService;
	
	@Autowired
	private ICorrespondenceInfraService correspondenceInfraService;
	
	/**
	 * Generate sequential human readable number for notice
	 * @return Notice id
	 */
	public String generateNoticeId() {
		String seqName="NOTICE_SEQUENCE";
		String pattern="NNOT%s%sN%s";
		String year = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yy");
		String month = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "MMM").toUpperCase();
		String ran = PasswordUtils.generateRandomNumber(3);

		Date lastResetDate=sequenceInfraService.getLastResetDate(seqName);
		if(lastResetDate == null || CommonUtils.isCurrentMonth(lastResetDate)) {
			int seq=sequenceInfraService.incrementSequence(seqName);
			return String.format(pattern, year+month,ran,seq);
		}else {
			int seq=sequenceInfraService.resetSequence(seqName);
			return String.format(pattern, year+month,ran,seq);
		}
	}
	
	/**
	 * Generate sequential human readable number for account
	 * @return Account id
	 */
	public String generateAccountId() {
		String seqName="ACCOUNT_SEQUENCE";
		String pattern="NACC%s%sA%s";
		String ran = PasswordUtils.generateRandomNumber(6);
		String yearmonth = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yyyyMM").toUpperCase();
		int seq=sequenceInfraService.incrementSequence(seqName);
		return String.format(pattern,yearmonth,ran,seq);
	}
	
	/**
	 * Generate sequential human readable number for donation
	 * @return Donation id
	 */
	public String generateDonationId() {
		String seqName="DONATION_SEQUENCE";
		String pattern="NDON%s%sD%s";
		String ran = PasswordUtils.generateRandomNumber(4);
		String yearmonth = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yyyyMMM").toUpperCase();
		int seq=sequenceInfraService.incrementSequence(seqName);
		return String.format(pattern, yearmonth, ran,seq);
	}

	/**
	 * Generate sequential human readable number for transaction
	 * @return Transaction id
	 */
	public String generateTransactionId() {
		String seqName="TRANSACTION_SEQUENCE";
		String pattern="NTXN%s%sT%s";
		String date = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yyyyMMddHHmmss");
		String ran = PasswordUtils.generateRandomNumber(4);
		int seq=sequenceInfraService.incrementSequence(seqName);
		return String.format(pattern, date,ran,seq);
	}
	
	/**
	 * Generate sequential human readable number for account
	 * @return Account id
	 */
	public String generateWorkflowId() {
		String seqName="WORKFLOW_SEQUENCE";
		String pattern="NWF%sR%s";
		String ran = PasswordUtils.generateRandomNumber(2);
		int seq=sequenceInfraService.incrementSequence(seqName);
		return String.format(pattern,ran,seq);
	}
	
	/**
	 * Generate and send OTP to user
	 * @param name
	 * @param email
	 * @param mobileNo
	 * @param scope
	 * @param refId
	 * @return unique token for identification
	 * @throws Exception 
	 */
	public String sendOTP(String name,String email,String mobileNo,String scope,String refId) throws Exception {
		TicketDTO ticket= new TicketDTO(TicketType.OTP);
		ticket.setCommunicationMethods(List.of(CommunicationMethod.EMAIL));
		UserDTO userDTO = new UserDTO();
		userDTO.setName(name);
		userDTO.setEmail(email);
		userDTO.setPhoneNumber(mobileNo);
		ticket.setUserInfo(userDTO);
		ticket.setRefId(refId);
		ticket.setOtpDigits(6);
		ticket.setExpireTicketAfterSec(84000);//configure it
		ticket.setTicketScope(List.of(scope));
		ticket=ticketInfraService.createTicket(ticket);
		CorrespondentDTO recipient= CorrespondentDTO.builder()
				.name(name)
				.emailRecipientType(EmailRecipientType.TO)
				.email(email)
				.mobile(mobileNo).build();
		
		sendEmail(BusinessConstants.EMAILTEMPLATE__SEND_OTP, List.of(recipient), Map.of("ticket",ticket));
		return ticket.getToken();
	}


	/**
	 * Re-send OTP to user
	 * @param token
	 * @throws Exception
	 */
	public void reSendOTP(String token) throws Exception {
		TicketDTO ticket=ticketInfraService.getTicketInfoByToken(token);
		throwBusinessExceptionIf(()->ticket.getExpired(), ExceptionEvent.OTP_EXPIRED);
		//sendEmail("OTP_",)
	}
	
	/**
	 * Validate one time password against token
	 * @param token
	 * @param otp
	 * @param scope
	 * @throws Exception
	 */
	public void validateOTP(String token,String otp,String scope) throws Exception {
		TicketDTO ticket=ticketInfraService.getTicketInfoByToken(token);
		throwBusinessExceptionIf(()->ticket.getExpired(), ExceptionEvent.OTP_EXPIRED);
		throwBusinessExceptionIf(()->!ticket.getTicketScope().contains(scope), ExceptionEvent.GENERIC_ERROR,"Err2468");
		if(ticket.getOneTimePassword().equals(otp)) {
			TicketDTO tDTO= new TicketDTO();
			tDTO.setTicketStatus(TicketStatus.USED);
			ticketInfraService.updateTicket(ticket.getId(), tDTO);
		}else {
			TicketDTO tDTO= new TicketDTO();
			int count = tDTO.getIncorrectOTPCount() == null ? 1 : tDTO.getIncorrectOTPCount()+1;
			tDTO.setIncorrectOTPCount(count);
			ticketInfraService.updateTicket(ticket.getId(), tDTO);
			throwBusinessExceptionIf(()->true, ExceptionEvent.INVALID_OTP);
		}
	}
	
	@Async
	public void sendEmail(String templateName,List<CorrespondentDTO> recipients, Map<String,Object> objectMap) throws Exception {
		EmailTemplateDTO template=findInterpolateAndConvertToEmailTemplateDTO(templateName, objectMap);
		correspondenceInfraService.sendEmail(null, recipients, template.getTemplateId(), template,null);
	}
	
	@Async
	public void sendSMS(String templateName,List<CorrespondentDTO> recipients, Map<String,Object> objectMap) throws Exception {
	
	}
	
}
