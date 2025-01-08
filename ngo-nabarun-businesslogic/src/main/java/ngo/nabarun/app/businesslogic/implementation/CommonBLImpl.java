package ngo.nabarun.app.businesslogic.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.ICommonBL;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.domain.CommonDO;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.annotation.NoLogging;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.RefDataType;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.util.SecurityUtils.AuthenticatedUser;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.UserDTO;

@Slf4j
@Service
public class CommonBLImpl extends BaseBLImpl implements ICommonBL {

	@Autowired
	private BusinessDomainHelper businessHelper;

	@Autowired
	private CommonDO commonDO;

	@Override
	public void uploadDocuments(MultipartFile[] files, String docIndexId, DocumentIndexType docIndexType)
			throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notNull(docIndexId, "docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");

		for (MultipartFile file : files) {
			DocumentDetailUpload doc = new DocumentDetailUpload();
			doc.setContent(file.getBytes());
			doc.setContentType(file.getContentType());
			doc.setOriginalFileName(file.getOriginalFilename());
			commonDO.uploadDocument(doc, docIndexId, docIndexType);
		}
	}

	@Override
	public void uploadDocuments(List<DocumentDetailUpload> files, String docIndexId, DocumentIndexType docIndexType)
			throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notEmpty(files, "Files cannot be empty !");
		Assert.notNull(docIndexId, "docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");

		for (DocumentDetailUpload file : files) {
			commonDO.uploadDocument(file, docIndexId, docIndexType);
		}
	}

	@Override
	public DocumentDetail getDocument(String docId) throws Exception {
		Assert.notNull(docId, "docId must not be null !");
		DocumentDetail documentDetail = new DocumentDetail();
		documentDetail.setDownloadURL(commonDO.getDocumentUrl(docId).toString());
		return documentDetail;
	}

	@Override
	public boolean deleteDocument(String docId) throws Exception {
		Assert.notNull(docId, "docId must not be null !");
		return commonDO.deleteDocument(docId);
	}

	@NoLogging
	@Override
	public Map<String, List<KeyValue>> getReferenceData(List<RefDataType> names, Map<String, String> attr)
			throws Exception {
		Map<String, List<KeyValue>> obj = new HashMap<>();
		if (names == null || names.contains(RefDataType.DONATION)) {
			DonationType type = null;
			DonationStatus currStatus = null;
			if (attr != null && attr.containsKey("donationType") && attr.containsKey("currentDonationStatus")) {
				type = DonationType.valueOf(attr.get("donationType"));
				currStatus = DonationStatus.valueOf(attr.get("currentDonationStatus"));
			}
			obj.putAll(businessHelper.getDonationRefData(type, currStatus));
		}

		if (names == null || names.contains(RefDataType.USER)) {
			String stateCode = null;
			String countryCode = null;
			if (attr != null && attr.containsKey("countryCode")) {
				countryCode = attr.get("countryCode");
			}
			if (attr != null && attr.containsKey("countryCode") && attr.containsKey("stateCode")) {
				stateCode = attr.get("stateCode");
				countryCode = attr.get("countryCode");
			}
			obj.putAll(businessHelper.getUserRefData(countryCode, stateCode));
		}
		if (names == null || names.contains(RefDataType.ACCOUNT)) {
			obj.putAll(businessHelper.getAccountRefData());
		}
		if (names == null || names.contains(RefDataType.WORKFLOW)) {
			RequestType type = null;
			if (attr != null && attr.containsKey("workflowType")) {
				type = RequestType.valueOf(attr.get("workflowType"));
			}
			obj.putAll(businessHelper.getWorkflowRefData(type));
		}
		if (names == null || names.contains(RefDataType.ADMIN)) {
			obj.putAll(businessHelper.getAdminRefData());
		}
		return obj;
	}

	@NoLogging
	@Override
	public Map<String, List<KeyValue>> getReferenceData(List<RefDataType> names) throws Exception {
		return getReferenceData(names, null);
	}

	@Override
	public List<AdditionalField> getReferenceFields(String identifier) throws Exception {
		return businessHelper.findAddtlFieldDTOList(identifier).stream().filter(f -> !f.isHidden())
				.map(BusinessObjectConverter::toAdditionalField).collect(Collectors.toList());
	}

	@Override
	public Paginate<Map<String, String>> getNotifications(Integer pageIndex, Integer pageSize) {
		return commonDO.getNotifications(pageIndex, pageSize);
	}

	@Async
	@Override
	public void manageNotification(AuthenticatedUser user, String action, Map<String, Object> payload)
			throws Exception {
		switch (action.toUpperCase()) {
		case "SAVE_TOKEN_AND_GET_COUNTS":
			commonDO.saveNotificationToken(user.getUserId(), payload.get("token").toString());
			commonDO.sendDashboardCounts(user.getUserId());
			break;
		case "DELETE_TOKEN":
			commonDO.removeNotificationToken(user.getUserId(), payload.get("token").toString());
			break;
		case "UPDATE_NOTIFICATION":
			commonDO.updateNotification(payload.get("id").toString(), payload);
			break;
		}
	}

	@Override
	public List<DocumentDetail> getDocuments(String id, DocumentIndexType type) {
		return commonDO.getDocuments(id, type).stream().map(BusinessObjectConverter::toDocumentDetail)
				.collect(Collectors.toList());
	}

	@Async
	@Override
	public void triggerJob(String triggerId,List<ServiceDetail> triggerDetail) {
		for (ServiceDetail trigger : triggerDetail) {
			try {
				//Map<String, String> parameters = trigger.getParameters();
				switch (trigger.getName()) {
				case SYNC_SYSTEMS:
					commonDO.syncSystems();
					break;
				case CREATE_DONATION:
					JobDTO<ServiceDetail,List<DonationDTO>> donationjob = new JobDTO<ServiceDetail, List<DonationDTO>>(triggerId,trigger.getName().name());
					List<UserDTO> users = userDO.retrieveAllUsers(null, null, new UserDetailFilter()).getContent();
					commonDO.startJob(donationjob,trigger);
					List<DonationDTO> donations=donationDO.createBulkMonthlyDonation(users,donationjob.getLog());
					commonDO.endJob(donationjob,donations);
					break;
				case DONATION_REMINDER_EMAIL:
					donationDO.sendDonationReminderEmail();
					break;
				case UPDATE_DONATION:
					donationDO.convertToPendingDonation();
					break;
				case TASK_REMINDER_EMAIL:
					requestDO.sendTaskReminderEmail();
					break;
				default:
					throw new BusinessException("Invalid Service " + trigger.getName());
				}
			} catch (Exception e) {
				log.error("Error in cron service: ", e);
			}
		}
	}



}
