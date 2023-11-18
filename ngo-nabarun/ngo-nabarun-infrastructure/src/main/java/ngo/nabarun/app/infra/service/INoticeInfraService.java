package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.NoticeDTO;

@Service
public interface INoticeInfraService {
	List<NoticeDTO> getNoticeList(Integer index,Integer size,NoticeDTO filter);
	NoticeDTO createNotice(NoticeDTO noticeDTO) throws Exception;
	NoticeDTO getNotice(String id);
	void deleteNotice(String id);
	long getNoticeCount();
	NoticeDTO updateNotice(String id,NoticeDTO noticeDTO) throws Exception;

}
