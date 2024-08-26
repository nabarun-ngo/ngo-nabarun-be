package ngo.nabarun.app.infra.serviceimpl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.common.annotation.NoLogging;
import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.common.exception.NotFoundException;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ObjectFilter;
import ngo.nabarun.app.ext.helpers.ObjectFilter.Operator;
import ngo.nabarun.app.ext.objects.RemoteConfig;
import ngo.nabarun.app.ext.service.ICollectionExtService;
import ngo.nabarun.app.ext.service.IEmailExtService;
import ngo.nabarun.app.ext.service.IFileStorageExtService;
import ngo.nabarun.app.ext.service.IMessageExtService;
import ngo.nabarun.app.ext.service.IRemoteConfigExtService;
import ngo.nabarun.app.infra.core.entity.ApiKeyEntity;
import ngo.nabarun.app.infra.core.entity.CustomFieldEntity;
import ngo.nabarun.app.infra.core.entity.DBSequenceEntity;
import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;
import ngo.nabarun.app.infra.core.entity.LogsEntity;
import ngo.nabarun.app.infra.core.entity.TicketInfoEntity;
import ngo.nabarun.app.infra.core.repo.ApiKeyRepository;
import ngo.nabarun.app.infra.core.repo.CustomFieldRepository;
import ngo.nabarun.app.infra.core.repo.DBSequenceRepository;
import ngo.nabarun.app.infra.core.repo.DocumentRefRepository;
import ngo.nabarun.app.infra.core.repo.LogsRepository;
import ngo.nabarun.app.infra.core.repo.TicketRepository;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.LogsDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO.NotificationDTOFilter;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.misc.ConfigTemplate;
import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.InfraFieldHelper;
import ngo.nabarun.app.infra.service.IApiKeyInfraService;
import ngo.nabarun.app.infra.service.ICorrespondenceInfraService;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IGlobalDataInfraService;
import ngo.nabarun.app.infra.service.IHistoryInfraService;
import ngo.nabarun.app.infra.service.ILogInfraService;
import ngo.nabarun.app.infra.service.ISequenceInfraService;
import ngo.nabarun.app.infra.service.ITicketInfraService;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;

@Service
public class CommonInfraServiceImpl implements ISequenceInfraService, ITicketInfraService, IDocumentInfraService,
		IHistoryInfraService, ICorrespondenceInfraService, IGlobalDataInfraService,ILogInfraService,IApiKeyInfraService {

	@Autowired
	private DBSequenceRepository dbSeqRepository;

	@Autowired
	private TicketRepository ticketRepo;
	
	@Autowired
	private LogsRepository logsRepo;
	
	@Autowired
	private ApiKeyRepository apiKeyRepo;

	@Autowired
	private IFileStorageExtService fileStorageService;

	@Autowired
	private DocumentRefRepository documentRefRepository;

	@Autowired
	private CustomFieldRepository fieldRepository;

	@Autowired
	private IEmailExtService emailExtService;

	@Autowired
	private PropertyHelper propertyHelper;

	@Autowired
	private IRemoteConfigExtService remoteConfigService;
	
	@Autowired
	private IMessageExtService messageExtService;
	
	@Autowired
	private ICollectionExtService collectionExtService;

	/**
	 * Constants THIS SHOULD BE SAME AS REMOTE CONFIG
	 */

	private static final String DOMAIN_GLOBAL_CONFIG = "DOMAIN_GLOBAL_CONFIG";

	private static final String COLLECTION_NOTIFICATION = "notifications";

	private static final String COLLECTION_NOTIFICATION_TOKEN = "notification_tokens";
	

	@Override
	public int getLastSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		return seqUpdate.getSeq();
	}

	@Override
	public int resetSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		seqUpdate.setSeq(1);
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate.setLastSeqResetOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate.getSeq();
	}

	@Override
	public Date getLastResetDate(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase()).orElse(null);
		return seqUpdate == null ? null : seqUpdate.getLastSeqResetOn();
	}

	private DBSequenceEntity createEntity(String seqName) {
		DBSequenceEntity seqUpdate = new DBSequenceEntity();
		seqUpdate.setSeq(1);
		seqUpdate.setName(seqName);
		seqUpdate.setId(seqName.toUpperCase());
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate.setLastSeqResetOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate;
	}

	@Override
	public int incrementSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		seqUpdate.setSeq(seqUpdate.getSeq() + 1);
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate.getSeq();
	}

	@Override
	public int decrementSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		seqUpdate.setSeq(seqUpdate.getSeq() - 1);
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate.getSeq();
	}

	@Override
	public TicketDTO getTicketInfoByToken(String token) {
		TicketInfoEntity tokenEntity = ticketRepo.findByToken(token)
				.orElseThrow(() -> new NotFoundException("ticket", token));
		return InfraDTOHelper.convertToTicketDTO(tokenEntity);
	}

	@Override
	public TicketDTO createTicket(TicketDTO ticket) {
		TicketInfoEntity ticketInfo = new TicketInfoEntity();
		ticketInfo.setId(UUID.randomUUID().toString());
		if (ticket.getCommunicationMethods() != null) {
			ticketInfo.setCommunicationMethod(InfraFieldHelper
					.stringListToString(ticket.getCommunicationMethods().stream().map(m -> m.name()).toList()));
		}
		ticketInfo.setCreatedBy("Company");
		ticketInfo.setCreatedOn(CommonUtils.getSystemDate());
		
		ticketInfo.setForUserId(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getProfileId());
		ticketInfo.setName(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getName());
		ticketInfo.setEmail(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getEmail());
		ticketInfo.setMobileNumber(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getPhoneNumber());

		ticketInfo.setExpireOn(
				CommonUtils.addSecondsToDate(CommonUtils.getSystemDate(), ticket.getExpireTicketAfterSec()));
		ticketInfo.setRefId(ticket.getRefId());
		if (ticket.getTicketScope() != null) {
			ticketInfo.setScope(
					InfraFieldHelper.stringListToString(ticket.getTicketScope()));
		}
		ticketInfo.setToken(ticket.getToken() == null ? PasswordUtils.generateRandomString(128, true):ticket.getToken());
		ticketInfo.setType(ticket.getTicketType().name());// otp- link
		if(ticket.getTicketType() == TicketType.OTP) {
			ticketInfo.setStatus(TicketStatus.OPEN.name());
			ticketInfo.setIncorrectOTPCount(0);
			ticketInfo.setOneTimePassword(PasswordUtils.generateRandomNumber(ticket.getOtpDigits()));
		}else if(ticket.getTicketType() == TicketType.LINK)  {
			ticketInfo.setStatus(TicketStatus.UNUSED.name());
			ticketInfo.setBaseTicketUrl(ticket.getBaseTicketUrl());
		}else if(ticket.getTicketType() == TicketType.DECISION_LINK)  {
			ticketInfo.setStatus(TicketStatus.UNUSED.name());
			ticketInfo.setBaseTicketUrl(ticket.getBaseTicketUrl());
			ticketInfo.setAcceptCode(PasswordUtils.generateRandomNumber(12));
			ticketInfo.setDeclineCode(PasswordUtils.generateRandomNumber(12));
		}
		ticketInfo=ticketRepo.save(ticketInfo);
		return InfraDTOHelper.convertToTicketDTO(ticketInfo);
	}

	@Override
	public TicketDTO updateTicket(String id, TicketDTO updatedTicket) {
		TicketInfoEntity tokenEntity = ticketRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("ticket", id));
		if(updatedTicket.getTicketStatus() != null) {
			tokenEntity.setStatus(updatedTicket.getTicketStatus().name());
		}
		if(updatedTicket.getIncorrectOTPCount() != null) {
			tokenEntity.setIncorrectOTPCount(updatedTicket.getIncorrectOTPCount());
		}
		tokenEntity=ticketRepo.save(tokenEntity);
		return InfraDTOHelper.convertToTicketDTO(tokenEntity);
	}

	@Override
	public DocumentDTO uploadDocument(MultipartFile file, String docIndexId, DocumentIndexType docIndexType)
			throws ThirdPartyException {
		String remotefileName = buildRemoteFileName(file.getOriginalFilename(), docIndexType);
		String fileDownloadableUrl = fileStorageService.uploadFile(remotefileName, file);
		DocumentRefEntity docRef = addDocumentReference(file.getOriginalFilename(), remotefileName, docIndexId,
				docIndexType.name(), fileDownloadableUrl, file.getContentType());
		return InfraDTOHelper.convertToDocumentDTO(docRef);
	}

	@Override
	public DocumentDTO uploadDocument(String originalFileName, String contentType, String docIndexId,
			DocumentIndexType docIndexType, byte[] content) throws ThirdPartyException {
		String remotefileName = buildRemoteFileName(originalFileName, docIndexType);
		String fileDownloadableUrl = fileStorageService.uploadFile(remotefileName, contentType, content);
		DocumentRefEntity docRef = addDocumentReference(originalFileName, remotefileName, docIndexId,
				docIndexType.name(), fileDownloadableUrl, contentType);
		return InfraDTOHelper.convertToDocumentDTO(docRef);
	}

	private DocumentRefEntity addDocumentReference(String originalFileName, String remotefileName, String docIndexId,
			String docIndexType, String fileDownloadableUrl, String contentType) {
		DocumentRefEntity docRef = new DocumentRefEntity();
		docRef.setId(UUID.randomUUID().toString());
		docRef.setRemoteFileName(remotefileName);
		docRef.setCreatedOn(CommonUtils.getSystemDate());
		docRef.setDeleted(false);
		docRef.setDocumentRefId(docIndexId);
		docRef.setDocumentType(docIndexType);
		docRef.setDownloadUrl(fileDownloadableUrl);
		docRef.setFileType(contentType);
		docRef.setOriginalFileName(originalFileName);
		docRef.setAttachementIdentifier(contentType);
		docRef = documentRefRepository.save(docRef);
		return docRef;
	}

	private String buildRemoteFileName(String originalFileName, DocumentIndexType docType) {
		String extension=FilenameUtils.getExtension(originalFileName);
		return docType.getDocFolderName() + "/"
				+ UUID.randomUUID().toString()+"."+(extension == null ? "zip" : extension);
		
	}

	@Override
	public URL getTempDocumentUrl(String docId, long duration, TimeUnit timeunit) throws ThirdPartyException {
		DocumentRefEntity docRef = documentRefRepository.findById(docId).orElseThrow();
		String remoteFileName = docRef.getRemoteFileName() != null ? docRef.getRemoteFileName()
				: CommonUtils.getURLToFileName(docRef.getDownloadUrl());
		return fileStorageService.getTemporaryDownloadUrl(remoteFileName, duration, timeunit);
	}

	@Override
	public boolean hardDeleteDocument(String docId) {
		DocumentRefEntity docRef = documentRefRepository.findById(docId).orElseThrow();
		String remoteFileName = docRef.getRemoteFileName() != null ? docRef.getRemoteFileName()
				: CommonUtils.getURLToFileName(docRef.getDownloadUrl());
		boolean deleted = fileStorageService.removeFileByFilename(remoteFileName);
		if (deleted) {
			documentRefRepository.delete(docRef);
		}
		return deleted;
	}

	@Override
	public List<DocumentDTO> getDocumentList(String docRefId, DocumentIndexType documentType) {
		List<DocumentRefEntity> docList = documentRefRepository.findByDocumentRefIdAndDocumentType(docRefId,
				documentType.name());
		return docList.stream().map(m -> InfraDTOHelper.convertToDocumentDTO(m)).toList();
	}

	@Override
	public void sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplateDTO template) {
		sendEmail(senderName, recipients, template, List.of());
	}

	@Override
	public void sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplateDTO template,
			List<DocumentDTO> attachFrom) {
		List<Map<String, String>> recipientsList = new ArrayList<>();
		for (CorrespondentDTO recipient : recipients) {
			Map<String, String> recipientMap = new HashMap<>();
			recipientMap.put("recipientType", recipient.getEmailRecipientType().name());
			recipientMap.put("recipientEmail", recipient.getEmail());
			recipientMap.put("recipientName", recipient.getName());
			recipientsList.add(recipientMap);
		}

		List<Map<String, String>> attachmentList = new ArrayList<>();
		Base64 x = new Base64();
		for (DocumentDTO attachment : attachFrom) {
			try {
				Map<String, String> attachmentMap = new HashMap<>();
				byte[] fileContent = CommonUtils.toByteArray(new URL(attachment.getDocumentURL()));
				attachmentMap.put("content", x.encodeAsString(fileContent));
				attachmentMap.put("contentId", attachment.getDocId());
				attachmentMap.put("disposition", "attachment");
				attachmentMap.put("fileName", attachment.getOriginalFileName());
				attachmentMap.put("fileType", attachment.getFileType());
				attachmentList.add(attachmentMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		senderName = (senderName == null) ? propertyHelper.getAppName() : senderName;
		emailExtService.sendEmail(template.getSubject(), senderName, recipientsList, null, template.getBody(),
				attachmentList);

	}

	@Override
	public void sendEmail(String senderName, List<CorrespondentDTO> recipients, String templateId,
			EmailTemplateDTO template, List<DocumentDTO> attachFrom) {
		List<Map<String, String>> recipientsList = new ArrayList<>();
		for (CorrespondentDTO recipient : recipients) {
			Map<String, String> recipientMap = new HashMap<>();
			recipientMap.put("recipientType", recipient.getEmailRecipientType().name());
			recipientMap.put("recipientEmail", recipient.getEmail());
			recipientMap.put("recipientName", recipient.getName());
			recipientsList.add(recipientMap);
		}
		
		List<Map<String, String>> attachmentList = new ArrayList<>();

		if(attachFrom != null) {
			Base64 x = new Base64();
			for (DocumentDTO attachment : attachFrom) {
				try {
					Map<String, String> attachmentMap = new HashMap<>();
					byte[] fileContent = CommonUtils.toByteArray(new URL(attachment.getDocumentURL()));
					attachmentMap.put("content", x.encodeAsString(fileContent));
					attachmentMap.put("contentId", attachment.getDocId());
					attachmentMap.put("disposition", "attachment");
					attachmentMap.put("fileName", attachment.getOriginalFileName());
					attachmentMap.put("fileType", attachment.getFileType());
					attachmentList.add(attachmentMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		senderName = (senderName == null) ? propertyHelper.getAppName() : senderName;
		emailExtService.sendEmail(template.getSubject(), senderName, recipientsList, templateId, template.getBody(),
				attachmentList);
	}

	@NoLogging
	@Override
	public Map<String, List<KeyValuePair>> getDomainRefConfigs() throws Exception {
		RemoteConfig config = remoteConfigService.getRemoteConfig(DOMAIN_GLOBAL_CONFIG);
		ConfigTemplate[] configList = CommonUtils.jsonToPojo(config.getValue().toString(), ConfigTemplate[].class);
		Map<String, List<KeyValuePair>> configMap = new HashMap<>();
		for (ConfigTemplate domConfig : configList) {
			configMap.put(domConfig.getConfigName(), domConfig.getConfigValues());
		}
		return configMap;
	}

	 
	public FieldDTO addOrUpdateCustomField(FieldDTO fieldDTO) {
		CustomFieldEntity entity = fieldDTO.getFieldId() == null ? new CustomFieldEntity()
				: fieldRepository.findById(fieldDTO.getFieldId()).orElseThrow();
		entity.setFieldDescription(fieldDTO.getFieldDescription() != null ?fieldDTO.getFieldDescription(): entity.getFieldDescription());
		entity.setFieldKey(fieldDTO.getFieldKey() != null ?fieldDTO.getFieldKey().name(): entity.getFieldKey());
		entity.setFieldName(fieldDTO.getFieldName() != null ?fieldDTO.getFieldName(): entity.getFieldName());
		entity.setFieldType(fieldDTO.getFieldType() != null ?fieldDTO.getFieldType(): entity.getFieldType());
		entity.setFieldValue(fieldDTO.getFieldValue() != null ?fieldDTO.getFieldValue(): entity.getFieldValue());
		if(fieldDTO.getFieldId()== null) {
			entity.setSource(fieldDTO.getFieldSource());
		}
		return InfraDTOHelper.convertToFieldDTO(entity,propertyHelper.getAppSecret());
	}

	@Override
	public Page<NotificationDTO> getNotifications(Integer index, Integer size, NotificationDTOFilter filterDTO) {
		List<ObjectFilter> filter = new ArrayList<>();
		List<NotificationDTO> notifications = new ArrayList<>();

		if(filterDTO.getRead() != null) {
			filter.add(new ObjectFilter("read",Operator.EQUAL,filterDTO.getRead()));
		}
		if(filterDTO.getTargetUserId() != null) {
			filter.add(new ObjectFilter("userId",Operator.EQUAL,filterDTO.getTargetUserId()));
		}
		try {
			List<Map<String, Object>> notificationCollection = collectionExtService.getCollectionData(COLLECTION_NOTIFICATION, index, size, filter);
			for(Map<String, Object> notification:notificationCollection) {
				notifications.add(new NotificationDTO(notification));
			}
		} catch (ThirdPartyException e) {
			e.printStackTrace();
		}
		return new PageImpl<>(notifications);
	}

	@Override
	public NotificationDTO createAndSendNotification(NotificationDTO notificationDTO) throws Exception {
		if(notificationDTO.getTarget() != null && !notificationDTO.getTarget().isEmpty()) {
			List<String> userIds=notificationDTO.getTarget().stream().map(m-> m.getUserId()).collect(Collectors.toList());
			System.err.println(userIds);
			for(String userId:userIds) {
				ObjectFilter filter= new ObjectFilter("userId",Operator.EQUAL,userId);
				List<String> tokens = collectionExtService.getCollectionData(COLLECTION_NOTIFICATION_TOKEN, null, null, List.of(filter))
						.stream().filter(f->f.get("token") != null).map(m->m.get("token").toString()).collect(Collectors.toList());
				System.err.println(tokens);
				if(!tokens.isEmpty()) {
					List<String> messageIds=messageExtService.sendMessage(notificationDTO.getTitle(), notificationDTO.getSummary(), notificationDTO.getImage(), tokens, notificationDTO.toMap());
					notificationDTO.toSourceMap().put("message_ids", messageIds);
				}
			}
		}
		Map<String, Object> data=collectionExtService.storeCollectionData(COLLECTION_NOTIFICATION, notificationDTO.toSourceMap());
		return new NotificationDTO(data);
	}

	@Override
	public NotificationDTO updateNotification(String id, NotificationDTO notificationDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveNotificationToken(String userId, String token) throws Exception {
		ObjectFilter filter= new ObjectFilter("token",Operator.EQUAL,token);
		ObjectFilter filter2= new ObjectFilter("userId",Operator.EQUAL,userId);
		List<Map<String, Object>> collections=collectionExtService.getCollectionData(COLLECTION_NOTIFICATION_TOKEN, null, null, List.of(filter,filter2));
		if(collections.isEmpty()) {
			Map<String,Object> dataMap= new HashMap<>();
			dataMap.put("userId", userId);
			dataMap.put("token", token);
			dataMap.put("registration_date", CommonUtils.getSystemDate());
			collectionExtService.storeCollectionData(COLLECTION_NOTIFICATION_TOKEN, dataMap);
		}
		return true;
	}

	@Override
	public boolean deleteNotificationTargetToken(String userId,String token) throws Exception {
		ObjectFilter filter= new ObjectFilter("token",Operator.EQUAL,token);
		ObjectFilter filter2= new ObjectFilter("userId",Operator.EQUAL,userId);
		List<Map<String, Object>> collections=collectionExtService.getCollectionData(COLLECTION_NOTIFICATION_TOKEN, null, null, List.of(filter,filter2));
		for(Map<String, Object> collection:collections) {
			collectionExtService.removeCollectionData(COLLECTION_NOTIFICATION_TOKEN, String.valueOf(collection.get("id")));
		}
		return true;
	}

	@Override
	public List<LogsDTO> getLogs(String corelationId) {
		List<LogsEntity> logs=logsRepo.findByCorelationId(corelationId);
		return logs.stream().map(InfraDTOHelper::convertToLogsDTO).toList();
	}

	@Override
	public LogsDTO saveLog(LogsDTO logsDTO) {
		LogsEntity logsEntity= new LogsEntity();
		logsEntity.setCorelationId(logsDTO.getCorelationId());
		logsEntity.setEndTime(logsDTO.getEndTime());
		logsEntity.setError(logsDTO.getError());
		logsEntity.setId(UUID.randomUUID().toString());
		logsEntity.setInputs(logsDTO.getInputs());
		logsEntity.setMethodName(logsDTO.getMethodName());
		logsEntity.setOutputs(logsDTO.getOutputs());
		logsEntity.setStartTime(logsDTO.getStartTime());
		logsEntity.setType(logsDTO.getType());
		logsEntity=logsRepo.save(logsEntity);
		return InfraDTOHelper.convertToLogsDTO(logsEntity);
	}

	@Override
	public ApiKeyDTO createApiKey(ApiKeyDTO apiKeyDTO) {
		ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
		apiKeyEntity.setApiKey("N."+UUID.randomUUID().toString()+"."+UUID.randomUUID().toString());
		apiKeyEntity.setCreatedOn(CommonUtils.getSystemDate());
		apiKeyEntity.setExpireable(apiKeyDTO.isExpireable());
		apiKeyEntity.setExpireOn(apiKeyDTO.getExpiryDate());
		apiKeyEntity.setId(UUID.randomUUID().toString());
		apiKeyEntity.setScopes(InfraFieldHelper.stringListToString(apiKeyDTO.getScopes()));
		apiKeyEntity.setStatus(apiKeyDTO.getStatus() == null ? null : apiKeyDTO.getStatus().name());
		apiKeyEntity=apiKeyRepo.save(apiKeyEntity);
		return InfraDTOHelper.convertToApiKeyDTO(apiKeyEntity);
	}

	@Override
	public ApiKeyDTO getApiKeyDetail(String apiKey) {
		ApiKeyEntity apiKeyEntity=apiKeyRepo.findByApiKey(apiKey).orElseThrow();
		return InfraDTOHelper.convertToApiKeyDTO(apiKeyEntity);
	}

	@Override
	public List<ApiKeyDTO> getApiKeys(ApiKeyStatus status) {
		List<ApiKeyEntity> apikeys=apiKeyRepo.findByStatus(status.name());
		return apikeys.stream().map(InfraDTOHelper::convertToApiKeyDTO).collect(Collectors.toList());
	}

	

	

}
