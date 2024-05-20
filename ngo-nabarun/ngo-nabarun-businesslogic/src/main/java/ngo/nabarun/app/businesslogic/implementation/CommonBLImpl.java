package ngo.nabarun.app.businesslogic.implementation;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import ngo.nabarun.app.businesslogic.ICommonBL;
import ngo.nabarun.app.businesslogic.businessobjects.AuthorizationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetailUpload;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.helper.BusinessHelper;
import ngo.nabarun.app.common.enums.AuthRefType;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.RefDataType;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.IDonationInfraService;
import ngo.nabarun.app.infra.service.IMeetingInfraService;

@Service
public class CommonBLImpl implements ICommonBL {

	@Autowired
	private IDocumentInfraService docInfraService;
	
	@Autowired
	private IDonationInfraService donationInfraService;
	
	@Autowired
	private IMeetingInfraService meetingInfraService;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private BusinessHelper businessHelper;

	
	@Override
	public void uploadDocuments(MultipartFile[] files, String docIndexId, DocumentIndexType docIndexType) throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notNull(docIndexId,"docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");
		
		String id=docIndexId;
		if(docIndexType == DocumentIndexType.DONATION) {
			id=donationInfraService.getDonation(docIndexId).getId();
		}
		
		for (MultipartFile file : files) {
			docInfraService.uploadDocument(file, id, docIndexType);
		}
	}
	
	@Override
	public void uploadDocuments(List<DocumentDetailUpload> files,String docIndexId, DocumentIndexType docIndexType) throws Exception {
		Assert.noNullElements(files, "Files cannot be null !");
		Assert.notEmpty(files,"Files cannot be empty !");
		Assert.notNull(docIndexId,"docIndexId must not be null !");
		Assert.notNull(docIndexType, "docIndexType must not be null !");
		
		String id=docIndexId;
		if(docIndexType == DocumentIndexType.DONATION) {
			id=donationInfraService.getDonation(docIndexId).getId();
		}
		
		for (DocumentDetailUpload file : files) {
			byte[] content = Base64.decodeBase64(file.getBase64Content());
			docInfraService.uploadDocument(file.getOriginalFileName(),file.getContentType(), id, docIndexType,content);
		}		
	}

	@Override
	public URL getDocumentUrl(String docId) throws Exception {
		Assert.notNull(docId,"docId must not be null !");
		return docInfraService.getTempDocumentUrl(docId,15,TimeUnit.MINUTES);
	}
	
	@Override
	public boolean deleteDocument(String docId) throws Exception {
		Assert.notNull(docId,"docId must not be null !");
		return docInfraService.hardDeleteDocument(docId);
	}

	@Override
	public String generateAuthorizationUrl(AuthorizationDetail authDetail) throws Exception {
		if(authDetail.getAuthRefType() == AuthRefType.MEETING) {
			MeetingDTO meetingDTO=meetingInfraService.getMeeting(authDetail.getAuthRefId());
			if(meetingDTO.getAuthUrl() == null) {
				TicketDTO ticketDTO = new TicketDTO(TicketType.LINK);
				//ticketDTO=ticketInfraService.createTicket(ticketDTO);
				meetingDTO.setAuthUrl(meetingInfraService.createAuthorizationLink(ticketDTO.getToken(),authDetail.getCallbackUrl()));
				meetingDTO.setAuthCallbackUrl(authDetail.getCallbackUrl());
				meetingDTO=meetingInfraService.updateMeeting(meetingDTO.getId(), meetingDTO);
			}
			return meetingDTO.getAuthUrl();
		}
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
		
		return obj;
	}
	
	@Override
	public Map<String,List<KeyValue>> getReferenceData(List<RefDataType> names) throws Exception {
		return getReferenceData(names,null);
	}

	

	
}
