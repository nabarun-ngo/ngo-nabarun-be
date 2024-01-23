package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.INoticeBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.helper.BusinessIdGenerator;
import ngo.nabarun.app.businesslogic.helper.DTOToBusinessObjectConverter;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.service.IDocumentInfraService;
import ngo.nabarun.app.infra.service.INoticeInfraService;

@Service
public class NoticeBLImpl implements INoticeBL {

	@Autowired
	private INoticeInfraService noticeIndraService;
	
	@Autowired
	private IDocumentInfraService documentInfraService;
	
	@Autowired
	private  BusinessIdGenerator idGenerator;
	
	@Override
	public Paginate<NoticeDetail> getAllNotice(Integer page, Integer size, NoticeDetailFilter filter) {
		NoticeDTO noticeDTOFilter = null;
		if(filter != null) {
			noticeDTOFilter= new NoticeDTO();
			noticeDTOFilter.setTitle(filter.getTitle());
			//noticeDTOFilter.setNoticeNumber(filter.getNoticeNumber());
		}
		List<NoticeDetail> content =noticeIndraService.getNoticeList(page, size, noticeDTOFilter).stream()
				.filter(f->!f.isDraft())
				.map(m -> DTOToBusinessObjectConverter.toNoticeDetail(m)).collect(Collectors.toList());
		long total;
		if(page != null && size != null){
			total=noticeIndraService.getNoticeCount();
		}else {
			total = content.size();
		}
		return new Paginate<NoticeDetail>(page, size, total, content);
	}

	@Override
	public NoticeDetail getNoticeDetail(String id) {
		NoticeDTO noticeDTO=noticeIndraService.getNotice(id);
		return DTOToBusinessObjectConverter.toNoticeDetail(noticeDTO);
	}

	@Override
	public NoticeDetail createNotice(NoticeDetailCreate noticeDetail) throws Exception {
		NoticeDTO noticeDTO = new NoticeDTO();
		noticeDTO.setCreatorRole(noticeDetail.getCreatorRoleCode());
		noticeDTO.setDescription(noticeDetail.getDescription());
		noticeDTO.setDraft(noticeDetail.getDraft());
		noticeDTO.setNoticeDate(noticeDetail.getNoticeDate());
		noticeDTO.setTitle(noticeDetail.getTitle());
		//noticeDTO.setType(noticeEntity.getVisibility());		
		noticeDTO.setId(idGenerator.generateNoticeId());
		noticeDTO.setCreatedBy(SecurityUtils.getAuthUserId());
		noticeDTO=noticeIndraService.createNotice(noticeDTO);
		return DTOToBusinessObjectConverter.toNoticeDetail(noticeDTO);
	}

	@Override
	public NoticeDetail updateNotice(String id, NoticeDetailUpdate noticeDetail) throws Exception {
		NoticeDTO noticeDTO= new NoticeDTO();
		noticeDTO.setCreatorRole(noticeDetail.getCreatorRoleCode());
		noticeDTO.setDescription(noticeDetail.getDescription());
		if(noticeDetail.getPublish() != null && noticeDetail.getPublish() == Boolean.TRUE) {
			noticeDTO.setDraft(noticeDetail.getPublish());
		}
		noticeDTO.setNoticeDate(noticeDetail.getNoticeDate());
		noticeDTO.setTitle(noticeDetail.getTitle());
		//noticeDTO.setType(noticeEntity.getVisibility());		
		noticeDTO=noticeIndraService.updateNotice(id,noticeDTO);
		return DTOToBusinessObjectConverter.toNoticeDetail(noticeDTO);
	}

	@Override
	public List<DocumentDetail> getNoticeDocs(String id) {
		return documentInfraService.getDocumentList(id, DocumentIndexType.NOTICE).stream().map(m -> {
			DocumentDetail doc = new DocumentDetail();
			doc.setDocId(m.getDocId());
			doc.setDocumentIndexId(id);
			doc.setImage(m.isImage());
			doc.setOriginalFileName(m.getOriginalFileName());
			return doc;
		}).toList();
	}

	@Override
	public NoticeDetail getDraftedNotice() {
		NoticeDTO filter =  new NoticeDTO();
		filter.setCreatedBy(SecurityUtils.getAuthUserId());
		filter.setDraft(true);
		List<NoticeDTO> draftNotice=noticeIndraService.getNoticeList(null, null, filter);
		if(draftNotice.size()==0) {
			return null;
		}
		return DTOToBusinessObjectConverter.toNoticeDetail(draftNotice.get(0));
	}

	@Override
	public void deleteNotice(String id) {
		noticeIndraService.deleteNotice(id);
	}
	
}
