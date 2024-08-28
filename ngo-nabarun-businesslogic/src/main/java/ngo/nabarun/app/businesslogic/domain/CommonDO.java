package ngo.nabarun.app.businesslogic.domain;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.common.enums.AdditionalConfigKey;
import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.common.enums.CommunicationMethod;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO.NotificationDTOFilter;
import ngo.nabarun.app.infra.service.IApiKeyInfraService;
import ngo.nabarun.app.infra.service.ICorrespondenceInfraService;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.ISequenceInfraService;
import ngo.nabarun.app.infra.service.ITicketInfraService;

@Component
public class CommonDO {
	
	@Autowired
	private ICorrespondenceInfraService correspondenceInfraService;
	
	@Autowired
	private ISequenceInfraService sequenceInfraService;
	
	@Autowired
	private ITicketInfraService ticketInfraService;
	
	@Autowired
	protected BusinessDomainHelper businessDomainHelper;
	
	@Autowired
	private IDocumentInfraService docInfraService;
	
	@Autowired
	private IApiKeyInfraService apiKeyInfraService;
	
	/**
	 * Generate sequential human readable number for notice
	 * @return Notice id
	 */
	public String generateNoticeId() {
		String seqName="NOTICE_SEQUENCE";
		String pattern="NNOT%s%sN%s";
		String year = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yy");
		String month = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "MMM").toUpperCase();
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
		String yearmonth = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yyyyMM").toUpperCase();
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
		String yearmonth = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yyyyMMM").toUpperCase();
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
		String date = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yyyyMMddHHmmss");
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
	
	public String generateWorkId() {
		String seqName="WORK_SEQUENCE";
		String pattern="NWO%sR%s";
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
		String ticketValidity=businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.OTP_TICKET_VALIDITY);
		String otpDigit=businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.OTP_DIGITS);

		TicketDTO ticket= new TicketDTO(TicketType.OTP);
		ticket.setCommunicationMethods(List.of(CommunicationMethod.EMAIL));
		UserDTO userDTO = new UserDTO();
		userDTO.setName(name);
		userDTO.setEmail(email);
		userDTO.setPhoneNumber(mobileNo);
		ticket.setUserInfo(userDTO);
		ticket.setRefId(refId);
		ticket.setOtpDigits(Integer.parseInt(otpDigit));
		ticket.setExpireTicketAfterSec(Integer.parseInt(ticketValidity));//configure it
		ticket.setTicketScope(List.of(scope));
		ticket=ticketInfraService.createTicket(ticket);
		CorrespondentDTO recipient= CorrespondentDTO.builder()
				.name(name)
				.emailRecipientType(EmailRecipientType.TO)
				.email(email)
				.mobile(mobileNo).build();
		Map<String, Object> ticket_vars=ticket.toMap(businessDomainHelper.getDomainKeyValues());
		sendEmail(BusinessConstants.EMAILTEMPLATE__SEND_OTP, List.of(recipient), Map.of("ticket",ticket_vars));
		return ticket.getToken();
	}


	/**
	 * Re-send OTP to user
	 * @param token
	 * @throws Exception
	 */
	public void reSendOTP(String token) throws Exception {
		TicketDTO ticket=ticketInfraService.getTicketInfoByToken(token);
		businessDomainHelper.throwBusinessExceptionIf(()->ticket.getExpired(), ExceptionEvent.OTP_EXPIRED);
		CorrespondentDTO recipient= CorrespondentDTO.builder()
				.name(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getName())
				.emailRecipientType(EmailRecipientType.TO)
				.email(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getEmail())
				.mobile(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getPhoneNumber()).build();
		
		Map<String, Object> ticket_vars=ticket.toMap(businessDomainHelper.getDomainKeyValues());
		sendEmail(BusinessConstants.EMAILTEMPLATE__SEND_OTP, List.of(recipient), Map.of("ticket",ticket_vars));
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
		businessDomainHelper.throwBusinessExceptionIf(()->ticket.getExpired(), ExceptionEvent.OTP_EXPIRED);
		businessDomainHelper.throwBusinessExceptionIf(()->!ticket.getTicketScope().contains(scope), ExceptionEvent.GENERIC_ERROR);
		if(ticket.getOneTimePassword().equals(otp)) {
			TicketDTO tDTO= new TicketDTO();
			tDTO.setTicketStatus(TicketStatus.USED);
			ticketInfraService.updateTicket(ticket.getId(), tDTO);
		}else {
			TicketDTO tDTO= new TicketDTO();
			int count = tDTO.getIncorrectOTPCount() == null ? 1 : tDTO.getIncorrectOTPCount()+1;
			tDTO.setIncorrectOTPCount(count);
			ticketInfraService.updateTicket(ticket.getId(), tDTO);
			businessDomainHelper.throwBusinessExceptionIf(()->true, ExceptionEvent.INVALID_OTP);
		}
	}
	
	
	public void sendEmail(String templateName,List<CorrespondentDTO> recipients, Map<String,Object> objectMap) throws Exception {
		sendEmail(null,templateName,recipients, objectMap);
	}
	
	
	public void sendEmail(String senderName,String templateName,List<CorrespondentDTO> recipients, Map<String,Object> objectMap) throws Exception {
		EmailTemplateDTO template=businessDomainHelper.findInterpolateAndConvertToEmailTemplateDTO(templateName, objectMap);
		correspondenceInfraService.sendEmail(senderName, recipients, template.getTemplateId(), template,null);
	}
	
	public Paginate<Map<String, String>> getNotifications(Integer index, Integer size) {
		NotificationDTOFilter filter=new NotificationDTOFilter();
		filter.setTargetUserId(SecurityUtils.getAuthUserId());
		filter.setRead(false);
		Page<Map<String, String>> page = correspondenceInfraService.getNotifications(index, size, filter)
				.map(m->m.toMap());
		return new Paginate<>(page);
	}
	
	
	public void sendNotification(NotificationDTO template,List<UserDTO> recipients) throws Exception {
		List<UserDTO> target = recipients.stream().filter(f->f.getUserId() != null).collect(Collectors.toList());
		template.setItemClosed(false);
		template.setNotificationDate(CommonUtils.getSystemDate());
		template.setTarget(target);	
		correspondenceInfraService.createAndSendNotification(template);
	}
	
	
	public void sendNotification(String templateName,Map<String,Object> objectMap,List<UserDTO> recipients) throws Exception {
		NotificationDTO template=businessDomainHelper.findInterpolateAndConvertToNotificationDTO(templateName, objectMap);
		sendNotification(template,recipients);
	}
	
	public void saveNotificationToken(String userId,String token) throws Exception {
		correspondenceInfraService.saveNotificationToken(userId, token);
	}

	public void removeNotificationToken(String userId, String token) throws Exception {
		correspondenceInfraService.deleteNotificationTargetToken(userId,token);
	}
	public void updateNotification(String id, Map<String,Object> objectMap) throws Exception {
		correspondenceInfraService.updateNotification(id,new NotificationDTO(objectMap));
	}
	
	public void uploadDocument(DocumentDetailUpload file,String docIndexId, DocumentIndexType docIndexType) throws Exception {
		byte[] content = file.getContent() == null ?  Base64.decodeBase64(file.getBase64Content()) : file.getContent();
		docInfraService.uploadDocument(file.getOriginalFileName(),file.getContentType(), docIndexId, docIndexType,content);	
	}
	
	
	public void uploadDocument(MultipartFile file,String docIndexId, DocumentIndexType docIndexType) throws Exception {
		docInfraService.uploadDocument(file, docIndexId, docIndexType);	
	}

	public URL getDocumentUrl(String docId) throws Exception {
		String docLinkValidity=businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.DOCUMENT_LINK_VALIDITY);
		return docInfraService.getTempDocumentUrl(docId,Integer.parseInt(docLinkValidity),TimeUnit.SECONDS);
	}

	
	public boolean deleteDocument(String docId) throws Exception {
		return docInfraService.hardDeleteDocument(docId);
	}
	
	public Map<String,String> generateAPIKey(List<String> scopes){
		ApiKeyDTO apikeyDTO= new ApiKeyDTO();
		apikeyDTO.setExpireable(false);
		apikeyDTO.setScopes(scopes);
		apikeyDTO.setStatus(ApiKeyStatus.ACTIVE);
		apikeyDTO=apiKeyInfraService.createApiKey(apikeyDTO);
		return Map.of("id",apikeyDTO.getId(),"apiKey",apikeyDTO.getApiKey());
	}

}
