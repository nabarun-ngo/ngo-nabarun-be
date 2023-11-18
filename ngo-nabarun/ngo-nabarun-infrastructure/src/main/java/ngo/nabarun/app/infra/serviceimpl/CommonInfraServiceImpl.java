package ngo.nabarun.app.infra.serviceimpl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.exception.NotFoundException;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.ext.objects.RemoteConfig;
import ngo.nabarun.app.ext.service.IAuthManagementExtService;
import ngo.nabarun.app.ext.service.IEmailExtService;
import ngo.nabarun.app.ext.service.IFileStorageExtService;
import ngo.nabarun.app.ext.service.IRemoteConfigExtService;
import ngo.nabarun.app.infra.core.entity.DBSequenceEntity;
import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;
import ngo.nabarun.app.infra.core.entity.TicketInfoEntity;
import ngo.nabarun.app.infra.core.repo.DBSequenceRepository;
import ngo.nabarun.app.infra.core.repo.DocumentRefRepository;
import ngo.nabarun.app.infra.core.repo.TicketRepository;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.misc.EmailTemplate;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.InfraFieldHelper;
import ngo.nabarun.app.infra.misc.UserConfigTemplate;
import ngo.nabarun.app.infra.service.ICorrespondenceInfraService;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IDomainRefConfigInfraService;
import ngo.nabarun.app.infra.service.IHistoryInfraService;
import ngo.nabarun.app.infra.service.ISequenceInfraService;
import ngo.nabarun.app.infra.service.ITicketInfraService;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;

@Service
public class CommonInfraServiceImpl implements ISequenceInfraService, ITicketInfraService, IDocumentInfraService,
		IHistoryInfraService, ICorrespondenceInfraService, IDomainRefConfigInfraService {

	@Autowired
	private DBSequenceRepository dbSeqRepository;

	@Autowired
	private TicketRepository ticketRepo;

	@Autowired
	private IFileStorageExtService fileStorageService;

	@Autowired
	private DocumentRefRepository documentRefRepository;

	@Autowired
	private IEmailExtService emailExtService;

	@Autowired
	private GenericPropertyHelper propertyHelper;

	@Autowired
	private IRemoteConfigExtService remoteConfigService;
	
	@Autowired
	private IAuthManagementExtService authManagementService;

	/**
	 * Constants
	 */
	private static final String EMAIL_TEMPLATES_CONFIG = "EMAIL_TEMPLATES";
	private static final String USER_CONFIG = "USER_CONFIG";

	@Override
	public int getLastSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findByName(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		return seqUpdate.getSeq();
	}

	@Override
	public int resetSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findByName(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		seqUpdate.setSeq(1);
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate.setLastSeqResetOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate.getSeq();
	}

	@Override
	public Date getLastResetDate(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findByName(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		return seqUpdate.getLastSeqResetOn();
	}

	private DBSequenceEntity createEntity(String seqName) {
		DBSequenceEntity seqUpdate = new DBSequenceEntity();
		seqUpdate.setSeq(1);
		seqUpdate.setName(seqName.toUpperCase());
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate.setLastSeqResetOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate;
	}

	@Override
	public int incrementSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findByName(seqName.toUpperCase())
				.orElse(createEntity(seqName.toUpperCase()));
		seqUpdate.setSeq(seqUpdate.getSeq() + 1);
		seqUpdate.setLastSeqUpdateOn(CommonUtils.getSystemDate());
		seqUpdate = dbSeqRepository.save(seqUpdate);
		return seqUpdate.getSeq();
	}

	@Override
	public int decrementSequence(String seqName) {
		DBSequenceEntity seqUpdate = dbSeqRepository.findByName(seqName.toUpperCase())
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
		if (ticket.getCommunicationMethods() != null) {
			ticketInfo.setCommunicationMethod(InfraFieldHelper
					.stringListToString(ticket.getCommunicationMethods().stream().map(m -> m.name()).toList()));
		}
		ticketInfo.setCreatedBy("Company");
		ticketInfo.setCreatedOn(CommonUtils.getSystemDate());
		ticketInfo.setEmail(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getEmail());
		ticketInfo.setExpireOn(
				CommonUtils.addSecondsToDate(CommonUtils.getSystemDate(), ticket.getExpireTicketAfterSec()));
		ticketInfo.setIncorrectOTPCount(0);
		ticketInfo.setMobileNumber(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getPrimaryPhoneNumber());
		ticketInfo.setName(ticket.getUserInfo() == null ? null : ticket.getUserInfo().getFirstName());
		ticketInfo.setOneTimePassword(CommonUtils.generateRandomNumber(ticket.getOtpDigits()));
		ticketInfo.setRefId(ticket.getRefId());
		if (ticket.getTicketScope() != null) {
			ticketInfo.setScope(InfraFieldHelper
					.stringListToString( ticket.getTicketScope().stream().map(m -> m.name()).toList()));
		}
		ticketInfo.setToken(CommonUtils.generateRandomString(128, true));
		ticketInfo.setBaseTicketUrl(ticket.getBaseTicketUrl());
		ticketInfo.setType(null);// otp- link
		ticketInfo.setStatus(ticket.getTicketStatus().name());
		ticketRepo.save(ticketInfo);
		return InfraDTOHelper.convertToTicketDTO(ticketInfo);
	}

	@Override
	public TicketDTO updateTicket(String id, TicketDTO updatedTicket) {
		return null;
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
		return docType.getDocFolderName() + "/"
				+ UUID.randomUUID().toString().concat(originalFileName.substring(originalFileName.lastIndexOf(".")));
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
	public void sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplate template) {
		sendEmail(senderName, recipients, template, List.of());
	}

	@Override
	public void sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplate template,
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
			EmailTemplate template, List<DocumentDTO> attachFrom) {
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
		emailExtService.sendEmail(template.getSubject(), senderName, recipientsList, templateId, template.getBody(),
				attachmentList);
	}

	@Override
	public EmailTemplate getEmailTemplate(String emailName) throws Exception {
		List<RemoteConfig> configs = remoteConfigService.getRemoteConfigs();
		for (RemoteConfig config : configs) {
			if (EMAIL_TEMPLATES_CONFIG.equalsIgnoreCase(config.getName())) {
				EmailTemplate[] templates = CommonUtils.jsonToPojo(config.getValue().toString(), EmailTemplate[].class);
				for (EmailTemplate template : templates) {
					if (emailName.equalsIgnoreCase(template.getTemplateName())) {
						return template;
					}
				}
				break;
			}
		}
		return null;
	}

	@Override
	public UserConfigTemplate getUserConfig() throws Exception {
		
		RemoteConfig config = remoteConfigService.getRemoteConfig(USER_CONFIG);
		List<AuthUserRole> auth0Roles=authManagementService.getAllAvailableRoles();
		UserConfigTemplate userConfig=CommonUtils.jsonToPojo(config.getValue().toString(), UserConfigTemplate.class);		
		userConfig.getAvailableUserRoles().stream().map(m->{
			/**
			 * fetching the role from auth0 matching the 'roleName' attribute
			 * local config 'key' = 'roleName' 
			 */
			Optional<AuthUserRole> roleOpt=auth0Roles.stream().filter(f->f.getRoleName().equalsIgnoreCase(m.getKey())).findFirst();
			if(roleOpt.isPresent()) {
				AuthUserRole role = roleOpt.get();
				Map<String,Object> attr = new HashMap<>(m.getAttributes());
				attr.put("ROLE_ID",role.getRoleId());
				attr.put("ROLE_NAME",role.getRoleName());
				attr.put("ROLE_DESCRIPTION",role.getRoleDescription());
				m.setAttributes(attr);
			}
			return m;
		}).toList();
		return userConfig;
	}

}
