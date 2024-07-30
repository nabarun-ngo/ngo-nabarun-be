package ngo.nabarun.app.businesslogic.implementation;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.businesslogic.ICommonBL;
import ngo.nabarun.app.businesslogic.businessobjects.AuthorizationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.domain.CommonDO;
import ngo.nabarun.app.businesslogic.helper.BusinessDomainHelper;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.RefDataType;
import ngo.nabarun.app.common.util.SecurityUtils;

@Service
public class CommonBLImpl implements ICommonBL {
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private BusinessDomainHelper businessHelper;

	@Autowired
	private CommonDO commonDO;
	
	@Override
	public void uploadDocuments(MultipartFile[] files, String docIndexId, DocumentIndexType docIndexType) throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notNull(docIndexId,"docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");
		
		for (MultipartFile file : files) {
			DocumentDetailUpload doc= new DocumentDetailUpload();
			doc.setContent(file.getBytes());
			doc.setContentType(file.getContentType());
			doc.setOriginalFileName(file.getOriginalFilename());
			commonDO.uploadDocument(doc, docIndexId, docIndexType);
		}
	}
	
	@Override
	public void uploadDocuments(List<DocumentDetailUpload> files,String docIndexId, DocumentIndexType docIndexType) throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notEmpty(files,"Files cannot be empty !");
		Assert.notNull(docIndexId,"docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");
		
		for (DocumentDetailUpload file : files) {
			commonDO.uploadDocument(file, docIndexId, docIndexType);
		}		
	}

	@Override
	public URL getDocumentUrl(String docId) throws Exception {
		Assert.notNull(docId,"docId must not be null !");
		return commonDO.getDocumentUrl(docId);
	}
	
	@Override
	public boolean deleteDocument(String docId) throws Exception {
		Assert.notNull(docId,"docId must not be null !");
		return commonDO.deleteDocument(docId);
	}

	@Override
	@Deprecated
	public String generateAuthorizationUrl(AuthorizationDetail authDetail) throws Exception {
//		if(authDetail.getAuthRefType() == AuthRefType.MEETING) {
//			MeetingDTO meetingDTO=meetingInfraService.getMeeting(authDetail.getAuthRefId());
//			if(meetingDTO.getAuthUrl() == null) {
//				TicketDTO ticketDTO = new TicketDTO(TicketType.LINK);
//				//ticketDTO=ticketInfraService.createTicket(ticketDTO);
//				meetingDTO.setAuthUrl(meetingInfraService.createAuthorizationLink(ticketDTO.getToken(),authDetail.getCallbackUrl()));
//				meetingDTO.setAuthCallbackUrl(authDetail.getCallbackUrl());
//				meetingDTO=meetingInfraService.updateMeeting(meetingDTO.getId(), meetingDTO);
//			}
//			return meetingDTO.getAuthUrl();
//		}
		return null;
	}

	@Override
	public void clearSystemCache(List<String> names) {
		if(names == null || names.size() == 0) {
			cacheManager.getCacheNames().stream().forEach(name->cacheManager.getCache(name).clear());
		}else {
			names.stream().forEach(name->cacheManager.getCache(name).clear());
		}
	}
	
	@Override
	public Map<String,List<KeyValue>> getReferenceData(List<RefDataType> names,Map<String,String> attr) throws Exception {
		Map<String,List<KeyValue>> obj=new HashMap<>();
		if(names == null || names.contains(RefDataType.DONATION)) {
			DonationType type=null;
			DonationStatus currStatus=null;
			if(attr != null && attr.containsKey("donationType") && attr.containsKey("currentDonationStatus")) {
				type=DonationType.valueOf(attr.get("donationType"));
				currStatus=DonationStatus.valueOf(attr.get("currentDonationStatus"));
			}
			obj.putAll(businessHelper.getDonationRefData(type, currStatus));
		}
		
		if(names == null || names.contains(RefDataType.USER)) {
			obj.putAll(businessHelper.getUserRefData());
		}
		if(names == null || names.contains(RefDataType.ACCOUNT)) {
			obj.putAll(businessHelper.getAccountRefData());
		}
		return obj;
	}
	
	@Override
	public Map<String,List<KeyValue>> getReferenceData(List<RefDataType> names) throws Exception {
		return getReferenceData(names,null);
	}

	@Override
	public Paginate<Map<String,String>> getNotifications(Integer pageIndex, Integer pageSize) {
		return commonDO.getNotifications(pageIndex, pageSize);
	}


	@Override
	public void manageNotification(String action,Map<String, Object> payload) throws Exception {
		String userId= SecurityUtils.getAuthUserId();
		switch(action.toUpperCase()) {
		case "SAVE_TOKEN":
			commonDO.saveNotificationToken(userId, payload.get("token").toString());
			break;
		case "DELETE_TOKEN":
			commonDO.removeNotificationToken(userId,payload.get("token").toString());
			break;
		case "MARK_READ":
			//commonDO.upda(null,String.valueOf(payload.get("token")));
			break;
		}
	}

	
}
