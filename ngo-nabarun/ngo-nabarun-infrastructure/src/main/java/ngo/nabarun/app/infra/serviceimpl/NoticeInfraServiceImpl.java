package ngo.nabarun.app.infra.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.NoticeEntity;
import ngo.nabarun.app.infra.core.repo.NoticeRepository;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.service.INoticeInfraService;

@Service
public class NoticeInfraServiceImpl implements INoticeInfraService {

	@Autowired
	private NoticeRepository noticeRepository;

	@Override
	public List<NoticeDTO> getNoticeList(Integer page, Integer size, NoticeDTO filter) {
		List<NoticeEntity> events = null;
		if (filter != null) {
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase()
					.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
			NoticeEntity example = new NoticeEntity();
			example.setCreatedBy(filter.getCreatedBy());
			example.setDescription(filter.getDescription());
			example.setDraft(filter.isDraft());
			example.setNoticeDate(filter.getNoticeDate());
			//example.setNoticeNumber(filter.getNoticeNumber());
			example.setTitle(filter.getTitle());
			events = (page == null || size == null) ? noticeRepository.findAll(Example.of(example, matcher))
					: noticeRepository.findAll(Example.of(example, matcher), PageRequest.of(page, size)).getContent();
		} else if (page != null && size != null) {
			events = noticeRepository.findAll(PageRequest.of(page, size)).getContent();
		} else {
			events = noticeRepository.findAll();
		}
		return events.stream().map(m -> InfraDTOHelper.convertToNoticeDTO(m)).collect(Collectors.toList());
	}

	@Override
	public NoticeDTO createNotice(NoticeDTO noticeDTO) throws Exception {
		NoticeEntity notice = new NoticeEntity();
		notice.setCreatedBy(noticeDTO.getCreatedBy());
		notice.setCreatedOn(CommonUtils.getSystemDate());
		notice.setCreatorRole(noticeDTO.getCreatorRole());
		notice.setDescription(noticeDTO.getDescription());
		notice.setDisplayTill(0);
		notice.setDraft(noticeDTO.isDraft());
		notice.setId(noticeDTO.getId());
		notice.setNeedMeeting(null);
		notice.setNoticeDate(noticeDTO.getNoticeDate());
		notice.setPublishedOn(noticeDTO.isDraft() ? null : notice.getCreatedOn());
		notice.setRedirectUrl(null);
		notice.setTitle(noticeDTO.getTitle());
		notice.setVisibility(null);
		notice=noticeRepository.save(notice);
		return InfraDTOHelper.convertToNoticeDTO(notice);
	}

	@Override
	public NoticeDTO getNotice(String id) {
		NoticeEntity notice =noticeRepository.findById(id).orElseThrow();
		return InfraDTOHelper.convertToNoticeDTO(notice);
	}

	@Override
	public void deleteNotice(String id) {
		NoticeEntity notice =noticeRepository.findById(id).orElseThrow();
		noticeRepository.delete(notice);
	}

	@Override
	public long getNoticeCount() {
		return noticeRepository.count();
	}

	@Override
	public NoticeDTO updateNotice(String id, NoticeDTO noticeDTO) throws Exception {
		NoticeEntity notice =noticeRepository.findById(id).orElseThrow();
		NoticeEntity updated_notice = new NoticeEntity();
		updated_notice.setCreatedBy(noticeDTO.getCreatedBy());
		updated_notice.setCreatedOn(CommonUtils.getSystemDate());
		updated_notice.setCreatorRole(noticeDTO.getCreatorRole());
		updated_notice.setDescription(noticeDTO.getDescription());
		updated_notice.setDisplayTill(0);
		updated_notice.setDraft(noticeDTO.isDraft());
		updated_notice.setId(noticeDTO.getId());
		updated_notice.setNeedMeeting(null);
		updated_notice.setNoticeDate(noticeDTO.getNoticeDate());
		//updated_notice.setNoticeNumber(noticeDTO.getNoticeNumber());
		updated_notice.setPublishedOn(noticeDTO.isDraft() ? null : notice.getCreatedOn());
		updated_notice.setRedirectUrl(null);
		updated_notice.setTitle(noticeDTO.getTitle());
		updated_notice.setVisibility(null);
		CommonUtils.copyNonNullProperties(updated_notice, notice);
		notice=noticeRepository.save(notice);
		return InfraDTOHelper.convertToNoticeDTO(notice);

	}

}
