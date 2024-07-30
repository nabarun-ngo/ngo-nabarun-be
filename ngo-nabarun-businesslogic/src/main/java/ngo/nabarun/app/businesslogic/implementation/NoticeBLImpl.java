package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.INoticeBL;
import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.domain.NoticeDO;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.infra.dto.NoticeDTO;

@Service
public class NoticeBLImpl implements INoticeBL {

	@Autowired
	private NoticeDO noticeDO;

	
	@Override
	public Paginate<NoticeDetail> getAllNotice(Integer page, Integer size, NoticeDetailFilter filter) {
		return noticeDO.retrieveNotices(page, size, filter).map(BusinessObjectConverter::toNoticeDetail);		
	}

	@Override
	public NoticeDetail getNoticeDetail(String id) {
		NoticeDTO noticeDTO=noticeDO.retrieveNotice(id);
		return BusinessObjectConverter.toNoticeDetail(noticeDTO);
	}

	@Override
	public NoticeDetail createNotice(NoticeDetail noticeDetail) throws Exception {
		NoticeDTO noticeDTO=noticeDO.createNotice(noticeDetail,false);
		return BusinessObjectConverter.toNoticeDetail(noticeDTO);
	}

	@Override
	public NoticeDetail updateNotice(String id, NoticeDetail noticeDetail) throws Exception {
		NoticeDTO noticeDTO=noticeDO.updateNotice(id,noticeDetail);
		return BusinessObjectConverter.toNoticeDetail(noticeDTO);
	}

	@Override
	public List<DocumentDetail> getNoticeDocs(String id) {
		return List.of();
//		return documentInfraService.getDocumentList(id, DocumentIndexType.NOTICE).stream().map(m -> {
//			DocumentDetail doc = new DocumentDetail();
//			doc.setDocId(m.getDocId());
//			doc.setDocumentIndexId(id);
//			doc.setImage(m.isImage());
//			doc.setOriginalFileName(m.getOriginalFileName());
//			return doc;
//		}).toList();
	}

	@Override
	public NoticeDetail getDraftedNotice() {
//		NoticeDTO filter =  new NoticeDTO();
//		filter.setCreatedBy(SecurityUtils.getAuthUserId());
//		filter.setDraft(true);
//		List<NoticeDTO> draftNotice=noticeIndraService.getNoticeList(null, null, filter);
//		if(draftNotice.size()==0) {
//			return null;
//		}
//		return BusinessObjectConverter.toNoticeDetail(draftNotice.get(0));
		return null;
	}

	@Override
	public void deleteNotice(String id) {
		//noticeIndraService.deleteNotice(id);
	}
	
}
