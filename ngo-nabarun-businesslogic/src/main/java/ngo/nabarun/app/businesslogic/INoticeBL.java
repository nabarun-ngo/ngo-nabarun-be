package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.DocumentDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetailUpdate;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;

@Service
public interface INoticeBL {

	Paginate<NoticeDetail> getAllNotice(Integer page,Integer size,NoticeDetailFilter filter);
	NoticeDetail getNoticeDetail(String id);
	NoticeDetail createNotice(NoticeDetailCreate noticeDetail) throws Exception;
	NoticeDetail updateNotice(String id,NoticeDetailUpdate updatedNoticeDetail) throws Exception;
	List<DocumentDetail> getNoticeDocs(String id);
	NoticeDetail getDraftedNotice();
	void deleteNotice(String id);
}
