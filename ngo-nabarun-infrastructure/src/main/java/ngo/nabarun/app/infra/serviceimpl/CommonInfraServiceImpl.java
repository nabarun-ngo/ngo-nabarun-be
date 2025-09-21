package ngo.nabarun.app.infra.serviceimpl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.common.annotation.NoLogging;
import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.HistoryRefType;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.common.exception.NotFoundException;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.PasswordUtils;
import ngo.nabarun.app.common.util.SecurityUtils.AuthenticatedUser;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ObjectFilter;
import ngo.nabarun.app.ext.helpers.ObjectFilter.Operator;
import ngo.nabarun.app.ext.objects.AuthAPIInfo.AuthAPIScope;
import ngo.nabarun.app.ext.objects.RemoteConfig;
import ngo.nabarun.app.ext.service.IAuthManagementExtService;
import ngo.nabarun.app.ext.service.IEmailExtService;
import ngo.nabarun.app.ext.service.IFileStorageExtService;
import ngo.nabarun.app.ext.service.IGitHubExtService;
import ngo.nabarun.app.ext.service.IRemoteConfigExtService;
import ngo.nabarun.app.infra.core.entity.ApiKeyEntity;
import ngo.nabarun.app.infra.core.entity.CustomFieldEntity;
import ngo.nabarun.app.infra.core.entity.DBSequenceEntity;
import ngo.nabarun.app.infra.core.entity.DashboardCountEntity;
import ngo.nabarun.app.infra.core.entity.DocumentMappingEntity;
import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;
import ngo.nabarun.app.infra.core.entity.HistoryEntity;
import ngo.nabarun.app.infra.core.entity.LogsEntity;
import ngo.nabarun.app.infra.core.entity.TicketInfoEntity;
import ngo.nabarun.app.infra.core.repo.ApiKeyRepository;
import ngo.nabarun.app.infra.core.repo.CustomFieldRepository;
import ngo.nabarun.app.infra.core.repo.DBSequenceRepository;
import ngo.nabarun.app.infra.core.repo.DashboardCountRepository;
import ngo.nabarun.app.infra.core.repo.DocumentMappingRepository;
import ngo.nabarun.app.infra.core.repo.DocumentRefRepository;
import ngo.nabarun.app.infra.core.repo.HistoryRepository;
import ngo.nabarun.app.infra.core.repo.LogsRepository;
import ngo.nabarun.app.infra.core.repo.TicketRepository;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO.DocumentMappingDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO.DocumentUploadDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.HistoryDTO;
import ngo.nabarun.app.infra.dto.HistoryDTO.ChangeDTO;
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
import ngo.nabarun.app.infra.service.ISystemInfraService;
import ngo.nabarun.app.infra.service.ICountsInfraService;
import ngo.nabarun.app.infra.service.ITicketInfraService;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;

@Slf4j
@Service
public class CommonInfraServiceImpl implements ICountsInfraService, ITicketInfraService, IDocumentInfraService,
		IHistoryInfraService, ICorrespondenceInfraService, IGlobalDataInfraService, ILogInfraService,
		IApiKeyInfraService, ISystemInfraService {

	@Autowired
	private DBSequenceRepository dbSeqRepository;

	@Autowired
	private DashboardCountRepository dbCountRepository;

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
	private DocumentMappingRepository documentMappingRepository;

	@Autowired
	private CustomFieldRepository fieldRepository;

	@Autowired
	private IEmailExtService emailExtService;

	@Autowired
	private PropertyHelper propertyHelper;

	@Autowired
	private IRemoteConfigExtService remoteConfigService;

//	@Autowired
//	private IMessageExtService messageExtService;

	// @Autowired
	// private ICollectionExtService collectionExtService; 

	@Autowired
	private IAuthManagementExtService authManagementService;

	@Autowired
	private IGitHubExtService gitHubExtService;

	@Autowired
	private HistoryRepository historyRepository;

	/**
	 * Constants THIS SHOULD BE SAME AS REMOTE CONFIG
	 */

	private static final String DOMAIN_GLOBAL_CONFIG = "DOMAIN_GLOBAL_CONFIG";

	private static final String DOMAIN_LOCATION_DATA = "DOMAIN_LOCATION_DATA";

	private static final String DOMAIN_CONTENT_PUBLIC = "DOMAIN_CONTENT_PUBLIC";

	@Override
	public int getEntiryLastSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		return seqUpdate.getSeq();
	}

	@Override
	public int resetEntirySequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		seqUpdate.setSeq(1);
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate.setLastSeqResetOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate.getSeq();
	}

	@Override
	public Date getEntiryLastResetDate(String seqName) {
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
	public int incrementEntirySequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findById(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		seqUpdate.setSeq(seqUpdate.getSeq() + 1);
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate.getSeq();
	}

	@Override
	public int decrementEntitySequence(String seqName) {
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
			ticketInfo.setScope(InfraFieldHelper.stringListToString(ticket.getTicketScope()));
		}
		ticketInfo.setToken(
				ticket.getToken() == null ? PasswordUtils.generateRandomString(128, true) : ticket.getToken());
		ticketInfo.setType(ticket.getTicketType().name());// otp- link
		if (ticket.getTicketType() == TicketType.OTP) {
			ticketInfo.setStatus(TicketStatus.OPEN.name());
			ticketInfo.setIncorrectOTPCount(0);
			ticketInfo.setOneTimePassword(PasswordUtils.generateRandomNumber(ticket.getOtpDigits()));
		} else if (ticket.getTicketType() == TicketType.LINK) {
			ticketInfo.setStatus(TicketStatus.UNUSED.name());
			ticketInfo.setBaseTicketUrl(ticket.getBaseTicketUrl());
		} else if (ticket.getTicketType() == TicketType.DECISION_LINK) {
			ticketInfo.setStatus(TicketStatus.UNUSED.name());
			ticketInfo.setBaseTicketUrl(ticket.getBaseTicketUrl());
			ticketInfo.setAcceptCode(PasswordUtils.generateRandomNumber(12));
			ticketInfo.setDeclineCode(PasswordUtils.generateRandomNumber(12));
		}
		ticketInfo = ticketRepo.save(ticketInfo);
		return InfraDTOHelper.convertToTicketDTO(ticketInfo);
	}

	@Override
	public TicketDTO updateTicket(String id, TicketDTO updatedTicket) {
		TicketInfoEntity tokenEntity = ticketRepo.findById(id).orElseThrow(() -> new NotFoundException("ticket", id));
		if (updatedTicket.getTicketStatus() != null) {
			tokenEntity.setStatus(updatedTicket.getTicketStatus().name());
		}
		if (updatedTicket.getIncorrectOTPCount() != null) {
			tokenEntity.setIncorrectOTPCount(updatedTicket.getIncorrectOTPCount());
		}
		tokenEntity = ticketRepo.save(tokenEntity);
		return InfraDTOHelper.convertToTicketDTO(tokenEntity);
	}

	@Override
	public DocumentDTO uploadDocument(MultipartFile file, List<DocumentMappingDTO> documentMapping)
			throws ThirdPartyException {
		String remotefileName = buildRemoteFileName(file.getOriginalFilename(), null);
		String fileDownloadableUrl = fileStorageService.uploadFile(remotefileName, file);
		DocumentRefEntity docRef = addDocumentReference(file.getOriginalFilename(), remotefileName, fileDownloadableUrl,
				file.getContentType());
		createDocumentIndex(docRef.getId(), documentMapping);
		return InfraDTOHelper.convertToDocumentDTO(docRef);
	}

	@Override
	public DocumentDTO uploadDocument(DocumentUploadDTO documentUploadDTO) throws ThirdPartyException {
		String remotefileName = buildRemoteFileName(documentUploadDTO.getOriginalFileName(), null);
		String fileDownloadableUrl = fileStorageService.uploadFile(remotefileName, documentUploadDTO.getContentType(),
				documentUploadDTO.getContent());
		DocumentRefEntity docRef = addDocumentReference(documentUploadDTO.getOriginalFileName(), remotefileName,
				fileDownloadableUrl, documentUploadDTO.getContentType());
		createDocumentIndex(docRef.getId(), documentUploadDTO.getDocumentMapping());
		return InfraDTOHelper.convertToDocumentDTO(docRef);
	}

	private DocumentRefEntity addDocumentReference(String originalFileName, String remotefileName,
			String fileDownloadableUrl, String contentType) {
		DocumentRefEntity docRef = new DocumentRefEntity();
		docRef.setId(UUID.randomUUID().toString());
		docRef.setRemoteFileName(remotefileName);
		docRef.setCreatedOn(CommonUtils.getSystemDate());
		docRef.setDeleted(false);
		docRef.setDownloadUrl(fileDownloadableUrl);
		docRef.setFileType(contentType);
		docRef.setOriginalFileName(originalFileName);
		docRef.setAttachementIdentifier(contentType);
		docRef = documentRefRepository.save(docRef);
		return docRef;
	}

	private String buildRemoteFileName(String originalFileName, DocumentIndexType docType) {
		String extension = FilenameUtils.getExtension(originalFileName);
		if (docType == null) {
			return UUID.randomUUID().toString() + "." + extension;
		}
		return docType.getDocFolderName() + "/" + UUID.randomUUID().toString() + "."
				+ (extension == null ? "zip" : extension);
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
			List<DocumentMappingEntity> mappedDoc = documentMappingRepository.findByDocumentId(docId);
			documentMappingRepository.deleteAll(mappedDoc);
		}

		return deleted;
	}

	@Override
	public List<DocumentDTO> getDocumentList(String docRefId, DocumentIndexType documentType) {
		List<DocumentMappingEntity> docList = documentMappingRepository.findByDocumentRefIdAndDocumentType(docRefId,
				documentType.name());
		return docList.stream().map(m -> InfraDTOHelper.convertToDocumentDTO(m.getDocumentRef())).toList();
	}

	@Override
	public void createDocumentIndex(String documentId, List<DocumentMappingDTO> documentMapping) {
		for (DocumentMappingDTO docMap : documentMapping) {
			DocumentMappingEntity docMapEntity = new DocumentMappingEntity();
			docMapEntity.setId(UUID.randomUUID().toString());
			docMapEntity.setCreatedOn(CommonUtils.getSystemDate());
			docMapEntity.setDocumentId(documentId);
			docMapEntity.setDocumentRefId(docMap.getDocIndexId());
			docMapEntity.setDocumentType(docMap.getDocIndexType() == null ? null : docMap.getDocIndexType().name());
			documentMappingRepository.save(docMapEntity);
		}
	}

	@Override
	public int sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplateDTO template) {
		return sendEmail(senderName, recipients, template, List.of());
	}

	@Override
	public int sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplateDTO template,
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
				byte[] fileContent = CommonUtils.toByteArray(URI.create(attachment.getDocumentURL()).toURL());
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
		return emailExtService.sendEmail(template.getSubject(), senderName, recipientsList, null, template.getBody(),
				attachmentList);

	}

	@Override
	public int sendEmail(String senderName, List<CorrespondentDTO> recipients, String templateId,
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

		if (attachFrom != null) {
			Base64 x = new Base64();
			for (DocumentDTO attachment : attachFrom) {
				try {
					Map<String, String> attachmentMap = new HashMap<>();
					byte[] fileContent = CommonUtils.toByteArray(URI.create(attachment.getDocumentURL()).toURL());
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
		return emailExtService.sendEmail(template.getSubject(), senderName, recipientsList, templateId,
				template.getBody(), attachmentList);
	}

	@Override
	public Map<String, List<KeyValuePair>> getDomainRefConfigs() throws Exception {
		RemoteConfig config = remoteConfigService.getRemoteConfig(DOMAIN_GLOBAL_CONFIG);
		ConfigTemplate[] configList = CommonUtils.jsonToPojo(config.getValue().toString(), ConfigTemplate[].class);
		Map<String, List<KeyValuePair>> configMap = new HashMap<>();
		for (ConfigTemplate domConfig : configList) {
			configMap.put(domConfig.getConfigName(), domConfig.getConfigValues().stream().filter(f->f.isActive()).toList());
		}
		return configMap;
	}

	@Override
	public Map<String, List<KeyValuePair>> getDomainLocationData() throws Exception {
		RemoteConfig config = remoteConfigService.getRemoteConfig(DOMAIN_LOCATION_DATA);
		ConfigTemplate[] configList = CommonUtils.jsonToPojo(config.getValue().toString(), ConfigTemplate[].class);
		Map<String, List<KeyValuePair>> configMap = new HashMap<>();
		for (ConfigTemplate domConfig : configList) {
			configMap.put(domConfig.getConfigName(), domConfig.getConfigValues());
		}
		return configMap;
	}
	
	@Override
	public String getDomainJsonContent(boolean isPublic) throws Exception {
		RemoteConfig config = remoteConfigService.getRemoteConfig(DOMAIN_CONTENT_PUBLIC);
		return config.getValue().toString();
	}


	public FieldDTO addOrUpdateCustomField(FieldDTO fieldDTO) {
		CustomFieldEntity entity = fieldDTO.getFieldId() == null ? new CustomFieldEntity()
				: fieldRepository.findById(fieldDTO.getFieldId()).orElseThrow();
		entity.setFieldDescription(
				fieldDTO.getFieldDescription() != null ? fieldDTO.getFieldDescription() : entity.getFieldDescription());
		entity.setFieldKey(fieldDTO.getFieldKey() != null ? fieldDTO.getFieldKey().name() : entity.getFieldKey());
		entity.setFieldName(fieldDTO.getFieldName() != null ? fieldDTO.getFieldName() : entity.getFieldName());
		entity.setFieldType(fieldDTO.getFieldType() != null ? fieldDTO.getFieldType() : entity.getFieldType());
		entity.setFieldValue(fieldDTO.getFieldValue() != null ? fieldDTO.getFieldValue() : entity.getFieldValue());
		if (fieldDTO.getFieldId() == null) {
			entity.setSource(fieldDTO.getFieldSource());
		}
		return InfraDTOHelper.convertToFieldDTO(entity, propertyHelper.getAppSecret());
	}

	@Override
	public Page<NotificationDTO> getNotifications(Integer index, Integer size, NotificationDTOFilter filterDTO) {
		List<ObjectFilter> filter = new ArrayList<>();
		List<NotificationDTO> notifications = null;

		if (filterDTO.getRead() != null) {
			filter.add(new ObjectFilter("read", Operator.EQUAL, filterDTO.getRead()));
		}
		if (filterDTO.getTargetUserId() != null) {
			filter.add(new ObjectFilter("targetUserIds", Operator.ARRAY_CONTAIN, filterDTO.getTargetUserId()));
		}
		// try {
		// notifications =
		// collectionExtService.getCollectionData(COLLECTION_NOTIFICATION, index, size,
		// filter,
		// NotificationDTO.class);
//			for (Map<String, Object> notification : notificationCollection) {
//				notifications.add(new NotificationDTO(notification));
//			}
//			notifications.sort((n1, n2) -> {
//				return Long.valueOf(n2.getNotificationDate().getTime())
//						.compareTo(Long.valueOf(n1.getNotificationDate().getTime()));
//			});
		// Collections.sort(notifications, Collections.reverseOrder());
//		} catch (ThirdPartyException e) {
//			e.printStackTrace();
//		}
		return new PageImpl<>(notifications);
	}

	@Override
	public NotificationDTO createAndSendNotification(NotificationDTO notificationDTO) throws Exception {
//		if (notificationDTO.getTarget() != null && !notificationDTO.getTarget().isEmpty()) {
//			for (UserDTO targetUser : notificationDTO.getTarget()) {
//				String id = messageExtService.saveItemInRealtimeDB("notifications/" + targetUser.getUserId(),
//						notificationDTO.toMap());
//				notificationDTO.setId(id);
//			}
//		}
		return notificationDTO;
	}

	@Override
	public List<String> sendNotificationMessage(String userId, String title, String summary, String image,
			Map<String, String> data) throws Exception {
//		ObjectFilter filter = new ObjectFilter("userId", Operator.EQUAL, userId);
//		List<String> tokens = collectionExtService
//				.getCollectionData(COLLECTION_NOTIFICATION_TOKEN, null, null, List.of(filter), NTokenDTO.class).stream()
//				.filter(f -> f.getToken() != null).map(m -> m.getToken()).collect(Collectors.toList());
		List<String> messageIds = new ArrayList<>();
//		if (!tokens.isEmpty()) {
//			messageIds = messageExtService.sendMessage(title, summary, image, tokens, data);
//		}
		return messageIds;
	}

	@Override
	public NotificationDTO updateNotification(String id, NotificationDTO notificationDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveNotificationToken(String userId, String token) throws Exception {
//		ObjectFilter filter = new ObjectFilter("token", Operator.EQUAL, token);
//		ObjectFilter filter2 = new ObjectFilter("userId", Operator.EQUAL, userId);
//		List<NTokenDTO> collections = collectionExtService.getCollectionData(COLLECTION_NOTIFICATION_TOKEN, null, null,
//				List.of(filter, filter2), NTokenDTO.class);
//		if (collections.isEmpty()) {
//			NTokenDTO dataMap = new NTokenDTO();
//			dataMap.setId(UUID.randomUUID().toString());
//			dataMap.setUserId(userId);
//			dataMap.setToken(token);
//			dataMap.setCreatedOn(CommonUtils.getSystemDate());
//			collectionExtService.storeCollectionData(COLLECTION_NOTIFICATION_TOKEN, dataMap.getId(), dataMap);
//		}
		return true;
	}

	@Override
	public boolean deleteNotificationTargetToken(String userId, String token) throws Exception {
//		ObjectFilter filter = new ObjectFilter("token", Operator.EQUAL, token);
//		ObjectFilter filter2 = new ObjectFilter("userId", Operator.EQUAL, userId);
//		List<NTokenDTO> collections = collectionExtService.getCollectionData(COLLECTION_NOTIFICATION_TOKEN, null, null,
//				List.of(filter, filter2), NTokenDTO.class);
//		for (NTokenDTO collection : collections) {
//			collectionExtService.removeCollectionData(COLLECTION_NOTIFICATION_TOKEN, collection.getId());
//		}
		return true;
	}

	@Override
	public List<LogsDTO> getLogs(String corelationId) {
		List<LogsEntity> logs = logsRepo.findByCorelationId(corelationId);
		return logs.stream().map(InfraDTOHelper::convertToLogsDTO).toList();
	}

	@Override
	public LogsDTO saveLog(LogsDTO logsDTO) {
		LogsEntity logsEntity = new LogsEntity();
		logsEntity.setCorelationId(logsDTO.getCorelationId());
		logsEntity.setEndTime(logsDTO.getEndTime());
		logsEntity.setError(logsDTO.getError());
		logsEntity.setId(UUID.randomUUID().toString());
		logsEntity.setInputs(logsDTO.getInputs());
		logsEntity.setMethodName(logsDTO.getMethodName());
		logsEntity.setOutputs(logsDTO.getOutputs());
		logsEntity.setStartTime(logsDTO.getStartTime());
		logsEntity.setType(logsDTO.getType());
		logsEntity = logsRepo.save(logsEntity);
		return InfraDTOHelper.convertToLogsDTO(logsEntity);
	}

	@Override
	public ApiKeyDTO createOrUpdateApiKey(ApiKeyDTO apiKeyDTO) {
		ApiKeyEntity apiKeyEntity;
		if (apiKeyDTO.getId() != null) {
			apiKeyEntity = apiKeyRepo.findById(apiKeyDTO.getId()).orElseThrow();
		} else {
			apiKeyEntity = new ApiKeyEntity();
			apiKeyEntity.setId(UUID.randomUUID().toString());
			apiKeyEntity.setApiKey("N." + UUID.randomUUID().toString() + "." + UUID.randomUUID().toString());
			apiKeyEntity.setCreatedOn(CommonUtils.getSystemDate());
			apiKeyEntity.setExpireable(apiKeyDTO.isExpireable());
		}
		apiKeyEntity.setName(apiKeyDTO.getName() == null ? apiKeyEntity.getName() : apiKeyDTO.getName());
		apiKeyEntity.setExpireOn(
				apiKeyDTO.getExpiryDate() == null ? apiKeyEntity.getExpireOn() : apiKeyDTO.getExpiryDate());
		apiKeyEntity.setScopes(InfraFieldHelper.stringListToString(apiKeyDTO.getScopes()));
		apiKeyEntity.setStatus(apiKeyDTO.getStatus() == null ? apiKeyEntity.getStatus() : apiKeyDTO.getStatus().name());
		apiKeyEntity = apiKeyRepo.save(apiKeyEntity);
		return InfraDTOHelper.convertToApiKeyDTO(apiKeyEntity);
	}

	@Override
	public ApiKeyDTO getApiKeyDetail(String apiKey) {
		ApiKeyEntity apiKeyEntity = apiKeyRepo.findByApiKey(apiKey).orElseThrow();
		return InfraDTOHelper.convertToApiKeyDTO(apiKeyEntity);
	}

	@Override
	public List<ApiKeyDTO> getApiKeys(ApiKeyStatus status) {
		List<ApiKeyEntity> apikeys = apiKeyRepo.findByStatus(status.name());
		return apikeys.stream().map(InfraDTOHelper::convertToApiKeyDTO).collect(Collectors.toList());
	}

	@Override
	public Map<String, String> getDashboardCounts(String userId) {
		List<DashboardCountEntity> counts = dbCountRepository.findByUserIdIn(List.of(userId));
		return counts.stream().collect(Collectors.toMap(m1 -> m1.getDbFieldKey(), m2 -> m2.getDbFieldValue()));
	}

	@Override
	public Map<String, String> addOrUpdateDashboardCounts(String userId, Map<String, String> map) {
		List<DashboardCountEntity> countsEntities = new ArrayList<>();
		for (Entry<String, String> entity : map.entrySet()) {
			DashboardCountEntity dbCEntity = new DashboardCountEntity();
			dbCEntity.setId(userId + "_" + entity.getKey());
			dbCEntity.setDbFieldKey(entity.getKey());
			dbCEntity.setDbFieldValue(entity.getValue());
			dbCEntity.setLastUpdatedOn(CommonUtils.getSystemDate());
			dbCEntity.setUserId(userId);
			countsEntities.add(dbCEntity);
		}
		return dbCountRepository.saveAll(countsEntities).stream()
				.collect(Collectors.toMap(m1 -> m1.getDbFieldKey(), m2 -> m2.getDbFieldValue()));
	}

	private static List<ChangeDTO> compareMapsAndGetChanges(Map<String, Object> oldMap, Map<String, Object> newMap) {
		List<ChangeDTO> changes = new ArrayList<>();

		if (newMap != null && oldMap != null) {
			for (String key : oldMap.keySet()) {
				Object oldValue = oldMap.get(key);
				Object newValue = newMap.get(key);
				if (!CommonUtils.areEqual(oldValue, newValue)) {
					changes.add(new ChangeDTO(key, oldValue, newValue, "changed"));
				}
			}
		} else if (newMap != null) {
			for (String key : newMap.keySet()) {
				if (oldMap == null || !oldMap.containsKey(key)) {
					if (!ObjectUtils.isEmpty(newMap.get(key))) {
						changes.add(new ChangeDTO(key, null, newMap.get(key), "added"));
					}
				}
			}
		} else if (oldMap != null) {
			for (String key : oldMap.keySet()) {
				if (newMap == null || !newMap.containsKey(key)) {
					changes.add(new ChangeDTO(key, oldMap.get(key), null, "removed"));
				}
			}
		}
		return changes;
	}

	@Override
	public void logCreation(HistoryRefType type, String refId, AuthenticatedUser aUser, Map<String, Object> object)
			throws ThirdPartyException {
		saveHistory("CREATE", compareMapsAndGetChanges(null, object), aUser, type, refId);
	}

	@Override
	public void logUpdate(HistoryRefType type, String refId, AuthenticatedUser aUser, Map<String, Object> object1,
			Map<String, Object> object2) throws ThirdPartyException {
		saveHistory("UPDATE", compareMapsAndGetChanges(object1, object2), aUser, type, refId);
	}

	private HistoryEntity saveHistory(String action, List<ChangeDTO> changes, AuthenticatedUser aUser,
			HistoryRefType type, String refId) {
		HistoryEntity historyEntiry = new HistoryEntity();
		try {
			historyEntiry.setChanges(CommonUtils.toJSONString(changes, false));
		} catch (JsonProcessingException e) {
			log.error("Json procession error ", e);
		}
		historyEntiry.setAction(action);
		historyEntiry.setCreatedBy(aUser.getUserId());
		historyEntiry.setCreatedById(aUser.getId());
		historyEntiry.setCreatedByName(aUser.getName());
		historyEntiry.setCreatedOn(CommonUtils.getSystemDate());
		historyEntiry.setId(UUID.randomUUID().toString());
		historyEntiry.setReferenceId(refId);
		historyEntiry.setReferenceType(type.name());
		return historyRepository.save(historyEntiry);
	}

	@Override
	public List<HistoryDTO> getHistory(HistoryRefType type, String refId) throws ThirdPartyException {
		List<HistoryEntity> histories=historyRepository.findByReferenceIdAndReferenceType(refId, type.name());
		return histories.stream().map(InfraDTOHelper::convertToHistoryDTO).collect(Collectors.toList());
	}

	@NoLogging
	@Override
	public int configureAuthEmailProvider(String sender, String apikey_sg) throws Exception {
		return authManagementService.updateEmailProvider(true, sender, apikey_sg);
	}

	@Override
	public List<Map<String, String>> getAPIScopes() throws Exception {
		String audience = propertyHelper.getAuth0ResourceAPIAudience();
		List<AuthAPIScope> scopes = authManagementService.getAuthAPIInfo(audience).getScopes();
		List<Map<String, String>> mapp = new ArrayList<Map<String, String>>();
		for (AuthAPIScope scope : scopes) {
			mapp.add(Map.of("name", scope.getValue(), "description", scope.getDescription()));
		}
		return mapp;
	}

//	@Override
//	public String getRulesAndRegulationContent() {
//		String owner = propertyHelper.getGithubOrg();
//		String repo = propertyHelper.getGithubRepo();
//		String discussionId = propertyHelper.getGithubDiscussionId();
//		String body = gitHubExtService.getGitHubDiscussion(owner, repo, discussionId).getBodyHtml();
//		return body.replaceAll("\n", "");
//	}
}
