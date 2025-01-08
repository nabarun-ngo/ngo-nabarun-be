package ngo.nabarun.app.infra.service;


import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO.NoticeDTOFilter;

@Service
public interface INoticeInfraService {
	Page<NoticeDTO> getNoticeList(Integer index,Integer size,NoticeDTOFilter filter);
	NoticeDTO createNotice(NoticeDTO noticeDTO) throws Exception;
	NoticeDTO getNotice(String id);
	void deleteNotice(String id);
	long getNoticeCount();
	NoticeDTO updateNotice(String id,NoticeDTO noticeDTO) throws Exception;

}
