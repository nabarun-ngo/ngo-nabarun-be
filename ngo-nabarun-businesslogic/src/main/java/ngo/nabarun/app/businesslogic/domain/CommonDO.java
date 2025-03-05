package ngo.nabarun.app.businesslogic.domain;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
 
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.businessobjects.ApiKeyDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail.JobDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.exception.BusinessException.ExceptionEvent;
import ngo.nabarun.app.businesslogic.helper.BusinessConstants;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.common.enums.AdditionalConfigKey;
import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.common.enums.CommunicationMethod;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.enums.HistoryRefType;
import ngo.nabarun.app.common.enums.JobStatus;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.HistoryDTO;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.JobDTO.JobDTOFilter;
import ngo.nabarun.app.infra.dto.NotificationDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO.NotificationDTOFilter;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.service.IApiKeyInfraService;
import ngo.nabarun.app.infra.service.ICorrespondenceInfraService;
import ngo.nabarun.app.infra.service.ICountsInfraService;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IHistoryInfraService;
import ngo.nabarun.app.infra.service.IJobsInfraService;
import ngo.nabarun.app.infra.service.ISystemInfraService;
import ngo.nabarun.app.infra.service.ITicketInfraService;

@Slf4j
@Component
public class CommonDO {

	@Autowired
	private ICorrespondenceInfraService correspondenceInfraService;

	@Autowired
	private ICountsInfraService sequenceInfraService;

	@Autowired
	private ITicketInfraService ticketInfraService;

	@Autowired
	protected BusinessDomainHelper businessDomainHelper;

	@Autowired
	private IApiKeyInfraService apiKeyInfraService;

	@Autowired
	private IDocumentInfraService documentInfraService;

	@Autowired
	protected IHistoryInfraService historyInfraService;

	@Autowired
	protected PropertyHelper propertyHelper;

	@Autowired
	protected ISystemInfraService systemInfraService;

	@Autowired
	private IJobsInfraService jobInfraService;

	/**
	 * Generate sequential human readable number for notice
	 * 
	 * @return Notice id
	 */
	public String generateNoticeId() {
		String seqName = "NOTICE_SEQUENCE";
		String pattern = "NNOT%s%sN%s";
		String year = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yy");
		String month = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "MMM").toUpperCase();
		String ran = PasswordUtils.generateRandomNumber(3);

		Date lastResetDate = sequenceInfraService.getEntiryLastResetDate(seqName);
		if (lastResetDate == null || CommonUtils.isCurrentMonth(lastResetDate)) {
			int seq = sequenceInfraService.incrementEntirySequence(seqName);
			return String.format(pattern, year + month, ran, seq);
		} else {
			int seq = sequenceInfraService.resetEntirySequence(seqName);
			return String.format(pattern, year + month, ran, seq);
		}
	}

	/**
	 * Generate sequential human readable number for account
	 * 
	 * @return Account id
	 */
	public String generateAccountId() {
		String seqName = "ACCOUNT_SEQUENCE";
		String pattern = "NACC%s%sA%s";
		String ran = PasswordUtils.generateRandomNumber(6);
		String yearmonth = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yyyyMM").toUpperCase();
		int seq = sequenceInfraService.incrementEntirySequence(seqName);
		return String.format(pattern, yearmonth, ran, seq);
	}

	/**
	 * Generate sequential human readable number for donation
	 * 
	 * @return Donation id
	 */
	public String generateDonationId() {
		String seqName = "DONATION_SEQUENCE";
		String pattern = "NDON%s%sD%s";
		String ran = PasswordUtils.generateRandomNumber(4);
		String yearmonth = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yyyyMMM").toUpperCase();
		int seq = sequenceInfraService.incrementEntirySequence(seqName);
		return String.format(pattern, yearmonth, ran, seq);
	}

	/**
	 * Generate sequential human readable number for transaction
	 * 
	 * @return Transaction id
	 */
	public String generateTransactionId() {
		String seqName = "TRANSACTION_SEQUENCE";
		String pattern = "NTXN%s%sT%s";
		String date = CommonUtils.getFormattedDateString(CommonUtils.getSystemDate(), "yyyyMMddHHmmss");
		String ran = PasswordUtils.generateRandomNumber(4);
		int seq = sequenceInfraService.incrementEntirySequence(seqName);
		return String.format(pattern, date, ran, seq);
	}

	/**
	 * Generate sequential human readable number for account
	 * 
	 * @return Account id
	 */
	public String generateWorkflowId() {
		String seqName = "WORKFLOW_SEQUENCE";
		String pattern = "NWF%sR%s";
		String ran = PasswordUtils.generateRandomNumber(2);
		int seq = sequenceInfraService.incrementEntirySequence(seqName);
		return String.format(pattern, ran, seq);
	}

	public String generateWorkId() {
		String seqName = "WORK_SEQUENCE";
		String pattern = "NWO%sR%s";
		String ran = PasswordUtils.generateRandomNumber(2);
		int seq = sequenceInfraService.incrementEntirySequence(seqName);
		return String.format(pattern, ran, seq);
	}

	public String generateExpenseId() {
		String seqName = "EXPENSE_SEQUENCE";
		String pattern = "NEX%sR%s";
		String ran = PasswordUtils.generateRandomNumber(3);
		int seq = sequenceInfraService.incrementEntirySequence(seqName);
		return String.format(pattern, ran, seq);
	}

	/**
	 * Generate and send OTP to user
	 * 
	 * @param name
	 * @param email
	 * @param mobileNo
	 * @param scope
	 * @param refId
	 * @return unique token for identification
	 * @throws Exception
	 */
	public String sendOTP(String name, String email, String mobileNo, String scope, String refId) throws Exception {
		String ticketValidity = businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.OTP_TICKET_VALIDITY);
		String otpDigit = businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.OTP_DIGITS);

		TicketDTO ticket = new TicketDTO(TicketType.OTP);
		ticket.setCommunicationMethods(List.of(CommunicationMethod.EMAIL));
		UserDTO userDTO = new UserDTO();
		userDTO.setName(name);
		userDTO.setEmail(email);
		userDTO.setPhoneNumber(mobileNo);
		ticket.setUserInfo(userDTO);
		ticket.setRefId(refId);
		ticket.setOtpDigits(Integer.parseInt(otpDigit));
		ticket.setExpireTicketAfterSec(Integer.parseInt(ticketValidity));// configure it
		ticket.setTicketScope(List.of(scope));
		ticket = ticketInfraService.createTicket(ticket);
		CorrespondentDTO recipient = CorrespondentDTO.builder().name(name).emailRecipientType(EmailRecipientType.TO)
				.email(email).mobile(mobileNo).build();
		Map<String, Object> ticket_vars = ticket.toMap(businessDomainHelper.getDomainKeyValues());
		sendEmail(BusinessConstants.EMAILTEMPLATE__SEND_OTP, List.of(recipient), Map.of("ticket", ticket_vars));
		return ticket.getToken();
	}

	/**
	 * Re-send OTP to user
	 * 
	 * @param token
	 * @throws Exception
	 */
	public void reSendOTP(String token) throws Exception {
		TicketDTO ticket = ticketInfraService.getTicketInfoByToken(token);
		businessDomainHelper.throwBusinessExceptionIf(() -> ticket.getExpired(), ExceptionEvent.OTP_EXPIRED);
		CorrespondentDTO recipient = CorrespondentDTO.builder()
				.name(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getName())
				.emailRecipientType(EmailRecipientType.TO)
				.email(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getEmail())
				.mobile(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getPhoneNumber()).build();

		Map<String, Object> ticket_vars = ticket.toMap(businessDomainHelper.getDomainKeyValues());
		sendEmail(BusinessConstants.EMAILTEMPLATE__SEND_OTP, List.of(recipient), Map.of("ticket", ticket_vars));
	}

	/**
	 * Validate one time password against token
	 * 
	 * @param token
	 * @param otp
	 * @param scope
	 * @throws Exception
	 */
	public void validateOTP(String token, String otp, String scope) throws Exception {
		TicketDTO ticket = ticketInfraService.getTicketInfoByToken(token);
		businessDomainHelper.throwBusinessExceptionIf(() -> ticket.getExpired(), ExceptionEvent.OTP_EXPIRED);
		businessDomainHelper.throwBusinessExceptionIf(() -> !ticket.getTicketScope().contains(scope),
				ExceptionEvent.GENERIC_ERROR);
		if (ticket.getOneTimePassword().equals(otp)) {
			TicketDTO tDTO = new TicketDTO();
			tDTO.setTicketStatus(TicketStatus.USED);
			ticketInfraService.updateTicket(ticket.getId(), tDTO);
		} else {
			TicketDTO tDTO = new TicketDTO();
			int count = tDTO.getIncorrectOTPCount() == null ? 1 : tDTO.getIncorrectOTPCount() + 1;
			tDTO.setIncorrectOTPCount(count);
			ticketInfraService.updateTicket(ticket.getId(), tDTO);
			businessDomainHelper.throwBusinessExceptionIf(() -> true, ExceptionEvent.INVALID_OTP);
		}
	}

	public int sendEmail(String templateName, List<CorrespondentDTO> recipients, Map<String, Object> objectMap)
			throws Exception {
		return sendEmail(null, templateName, recipients, objectMap);
	}

	public int sendEmail(String senderName, String templateName, List<CorrespondentDTO> recipients,
			Map<String, Object> objectMap) throws Exception {
		EmailTemplateDTO template = businessDomainHelper.findInterpolateAndConvertToEmailTemplateDTO(templateName,
				objectMap);
		return correspondenceInfraService.sendEmail(senderName, recipients, template.getTemplateId(), template, null);
	}

	@Async
	public void sendEmailAsync(String templateName, List<CorrespondentDTO> recipients, Map<String, Object> objectMap,
			String triggerId, String senderName) throws Exception {
		JobDTO emailJob = new JobDTO(triggerId, templateName);
		startJob(emailJob, Map.of("recipients", recipients, "objectMap", objectMap, "senderName",
				senderName == null ? "" : senderName));
		int status = sendEmail(senderName, templateName, recipients, objectMap);
		endJob(emailJob, status);
	}

	public Paginate<Map<String, String>> getNotifications(Integer index, Integer size) {
		NotificationDTOFilter filter = new NotificationDTOFilter();
		filter.setTargetUserId(SecurityUtils.getAuthUserId());
		filter.setRead(false);
		Page<Map<String, String>> page = correspondenceInfraService.getNotifications(index, size, filter)
				.map(m -> m.toMap());
		return new Paginate<>(page);
	}

	public void sendNotification(NotificationDTO template, List<UserDTO> recipients) throws Exception {
		List<UserDTO> target = recipients.stream().filter(f -> f.getUserId() != null).collect(Collectors.toList());
		template.setItemClosed(false);
		template.setNotificationDate(CommonUtils.getSystemDate());
		template.setTarget(target);
		correspondenceInfraService.createAndSendNotification(template);
	}

	public void sendNotification(String templateName, Map<String, Object> objectMap, List<UserDTO> recipients)
			throws Exception {
		NotificationDTO template = businessDomainHelper.findInterpolateAndConvertToNotificationDTO(templateName,
				objectMap);
		sendNotification(template, recipients);
	}

	public void saveNotificationToken(String userId, String token) throws Exception {
		correspondenceInfraService.saveNotificationToken(userId, token);
	}

	public void removeNotificationToken(String userId, String token) throws Exception {
		correspondenceInfraService.deleteNotificationTargetToken(userId, token);
	}

	public void updateNotification(String id, Map<String, Object> objectMap) throws Exception {
		correspondenceInfraService.updateNotification(id, new NotificationDTO(objectMap));
	}

	@Async
	public void uploadDocument(DocumentDetailUpload file, String docIndexId, DocumentIndexType docIndexType)
			throws Exception {
		byte[] content = file.getContent() == null ? Base64.decodeBase64(file.getBase64Content()) : file.getContent();
		documentInfraService.uploadDocument(file.getOriginalFileName(), file.getContentType(), docIndexId, docIndexType,
				content);
	}

	@Async
	public void uploadDocument(MultipartFile file, String docIndexId, DocumentIndexType docIndexType) throws Exception {
		documentInfraService.uploadDocument(file, docIndexId, docIndexType);
	}

	public URL getDocumentUrl(String docId) throws Exception {
		String docLinkValidity = businessDomainHelper.getAdditionalConfig(AdditionalConfigKey.DOCUMENT_LINK_VALIDITY);
		return documentInfraService.getTempDocumentUrl(docId, Integer.parseInt(docLinkValidity), TimeUnit.SECONDS);
	}

	public boolean deleteDocument(String docId) throws Exception {
		return documentInfraService.hardDeleteDocument(docId);
	}

	@Async
	public void sendDashboardCounts(String userId) throws Exception {
		Map<String, String> countMap = sequenceInfraService.getDashboardCounts(userId);
		Map<String, String> dataMap = prepareDataMap(countMap);
		correspondenceInfraService.sendNotificationMessage(userId, "Hey! There are some updates for you from NABARUN.",
				"", "", dataMap);
	}

	private Map<String, String> prepareDataMap(Map<String, String> countMap) {
		Map<String, String> dataMap = new HashMap<>();
		if (countMap.containsKey(BusinessConstants.attr_DB_pendingDonationAmount)) {
			String donation = countMap.get(BusinessConstants.attr_DB_pendingDonationAmount);
			if (donation != null) {
				dataMap.put("donationAmount", "₹ " + donation);
				dataMap.put("needActionDonation", Double.valueOf(donation) > 0 ? "Y" : "N");
			}
		}
		if (countMap.containsKey(BusinessConstants.attr_DB_accountBalance)) {
			String account = countMap.get(BusinessConstants.attr_DB_accountBalance);
			if (account != null) {
				dataMap.put("needActionAccount", "N");
				dataMap.put("accountAmount", "₹ " + account);
			}
		}

		if (countMap.containsKey(BusinessConstants.attr_DB_memberCount)) {
			String member = countMap.get(BusinessConstants.attr_DB_memberCount);
			if (member != null) {
				dataMap.put("needActionMember", "N");
				dataMap.put("memberCount", member);
			}
		}
		if (countMap.containsKey(BusinessConstants.attr_DB_noticeCount)) {
			String notice = countMap.get(BusinessConstants.attr_DB_noticeCount);
			if (notice != null) {
				dataMap.put("needActionNotice", "N");
				dataMap.put("noticeCount", notice);
			}
		}
		if (countMap.containsKey(BusinessConstants.attr_DB_requestCount)) {
			String request = countMap.get(BusinessConstants.attr_DB_requestCount);
			if (request != null) {
				dataMap.put("needActionRequest", Integer.parseInt(request) > 0 ? "Y" : "N");
				dataMap.put("requestCount", request);
			}
		}
		if (countMap.containsKey(BusinessConstants.attr_DB_workCount)) {
			String work = countMap.get(BusinessConstants.attr_DB_workCount);
			if (work != null) {
				dataMap.put("needActionWork", Integer.parseInt(work) > 0 ? "Y" : "N");
				dataMap.put("workCount", work + "");
			}
		}

		if (countMap.containsKey(BusinessConstants.attr_DB_notificationCount)) {
			String notification = countMap.get(BusinessConstants.attr_DB_notificationCount);
			if (notification != null) {
				dataMap.put("notificationCount", notification);
			}
		}
		return dataMap;
	}

	@Async
	public void updateAndSendDashboardCounts(String userId, Function<Object, Map<String, String>> action) {
		try {
			Map<String, String> countMap = action.apply("");
			Map<String, String> userCountMap = new HashMap<>();
			for (Entry<String, String> countM : countMap.entrySet()) {
				userCountMap.put(countM.getKey(), countM.getValue());
			}

			if (!userCountMap.isEmpty()) {
				Map<String, String> updateCountMap = sequenceInfraService.addOrUpdateDashboardCounts(userId,
						userCountMap);
				Map<String, String> dataMap = prepareDataMap(updateCountMap);
				correspondenceInfraService.sendNotificationMessage(userId,
						"Hey! There are some updates for you from NABARUN.", "", "", dataMap);
			}
		} catch (Exception e) {
			log.error("Error sending notification", e);
		}
	}

	public List<DocumentDTO> getDocuments(String id, DocumentIndexType type) {
		return documentInfraService.getDocumentList(id, type);
	}

	public void cloneDocuments(String sourceId, DocumentIndexType sourceIndexType, String destId,
			DocumentIndexType destIndexType) {
		List<DocumentDTO> docDTos = documentInfraService.getDocumentList(sourceId, sourceIndexType);
		for (DocumentDTO docDTO : docDTos) {
			docDTO.setDocumentRefId(destId);
			docDTO.setDocumentType(destIndexType);
			documentInfraService.createDocumentIndex(docDTO);
		}
	}

	public List<HistoryDTO> retrieveHistory(String refId, HistoryRefType historyType) throws Exception {
		return historyInfraService.getHistory(historyType, refId).stream().sorted((c1, c2) -> {
			return Long.valueOf(c2.getCreatedOn()).compareTo(Long.valueOf(c1.getCreatedOn()));
		}).collect(Collectors.toList());
	}

	public void syncSystems(JobDTO job) throws Exception {
		job.log("[INFO] -----Starting System SYNC -----");
		String apikey_sg = propertyHelper.getSendGridAPIKey();
		String sender = propertyHelper.getDefaultEmailSender();
		job.log("[INFO] -----Syncing Auth0 Email provider -----");
		job.log("[INFO] ==> Sender : " + sender + " ==> Apikey : <apiKey>");
		int code = systemInfraService.configureAuthEmailProvider(sender, apikey_sg);
		job.log("[INFO] ----- Auth0 Email provider Synced. Status Code : " + code + " -----");
		job.log("[INFO] -----Ending System SYNC -----");
	}

	public ApiKeyDTO generateAPIKey(ApiKeyDetail detail) {
		ApiKeyDTO apikeyDTO = new ApiKeyDTO();
		apikeyDTO.setName(detail.getName());
		apikeyDTO.setExpireable(detail.isExpireable());
		apikeyDTO.setExpiryDate(detail.getExpiryDate());
		apikeyDTO.setScopes(detail.getScopes());
		apikeyDTO.setStatus(ApiKeyStatus.ACTIVE);
		apikeyDTO.setCreatedOn(CommonUtils.getSystemDate());
		apikeyDTO = apiKeyInfraService.createOrUpdateApiKey(apikeyDTO);
		return apikeyDTO;
	}

	public ApiKeyDTO updateAPIKey(String id, ApiKeyDetail detail, boolean revoke) {
		ApiKeyDTO apikeyDTO = new ApiKeyDTO();
		apikeyDTO.setId(id);
		apikeyDTO.setName(detail.getName());
		apikeyDTO.setScopes(detail.getScopes());
		if (revoke) {
			apikeyDTO.setStatus(ApiKeyStatus.REVOKED);
		}
		apikeyDTO = apiKeyInfraService.createOrUpdateApiKey(apikeyDTO);
		return apikeyDTO;
	}

	public List<ApiKeyDTO> getAPIKeys(ApiKeyStatus status) {
		return apiKeyInfraService.getApiKeys(status);
	}

	public void startJob(JobDTO job, Object ip) throws Exception {
		job.setStart(CommonUtils.getSystemDate());
		job.setStatus(JobStatus.IN_PROGRESS);
		job.setInput(ip);
		// Run garbage collection to reclaim unused memory
		// System.gc();
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
		long freeMemory = runtime.freeMemory() / 1024;
		long totalMemory = runtime.totalMemory() / 1024;
		long maxMemory = runtime.maxMemory() / 1024;
		job.setMemoryAtStart("Total memory (kB): " + totalMemory + " Free memory (kB): " + freeMemory
				+ " Used memory (kB): " + usedMemory + " Max memory (kB): " + maxMemory);
		String id = jobInfraService.createOrUpdateJob(job).getId();
		job.setId(id);
	}

	public void endJob(JobDTO job, Object op) throws Exception {
		job.setEnd(CommonUtils.getSystemDate());
		job.setOutput(op);
		job.setStatus(JobStatus.COMPLETED);
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
		long freeMemory = runtime.freeMemory() / 1024;
		long totalMemory = runtime.totalMemory() / 1024;
		long maxMemory = runtime.maxMemory() / 1024;
		job.setMemoryAtEnd("Total memory (kB): " + totalMemory + " Free memory (kB): " + freeMemory
				+ " Used memory (kB): " + usedMemory + " Max memory (kB): " + maxMemory);
		job = jobInfraService.createOrUpdateJob(job);
		// Add failure log count
		// TODO Send Email if failure log count is non 0 with log details
	}

	public List<Map<String, String>> getApiScopes() throws Exception {
		return apiKeyInfraService.getAPIScopes();
	}

	public Paginate<JobDTO> retrieveJobs(Integer pageIndex, Integer pageSize, JobDetailFilter filter) {
		JobDTOFilter filterDTO = null;
		if (filter != null) {
			filterDTO = new JobDTOFilter();
			filterDTO.setEnd(filter.getEnd());
			filterDTO.setId(filter.getId());
			filterDTO.setName(filter.getName());
			filterDTO.setStart(filter.getStart());
			filterDTO.setStatus(filter.getStatus());
			filterDTO.setTriggerId(filter.getTriggerId());
		}
		Page<JobDTO> page = jobInfraService.getJobList(pageIndex, pageSize, filterDTO);
		return new Paginate<JobDTO>(page);
	}
}
