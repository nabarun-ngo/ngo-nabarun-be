package ngo.nabarun.app.infra.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO;
import ngo.nabarun.app.infra.dto.NotificationDTO.NotificationDTOFilter;

public interface ICorrespondenceInfraService {

	@Async
	void sendEmail(String senderName, List<CorrespondentDTO> recipient, EmailTemplateDTO template);
	@Async
	void sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplateDTO template,
			List<DocumentDTO> attachFrom);
	@Async
	void sendEmail(String senderName, List<CorrespondentDTO> recipients,
			String templateId, EmailTemplateDTO template, List<DocumentDTO> attachFrom);
	

	Page<NotificationDTO> getNotifications(Integer index, Integer size, NotificationDTOFilter filterDTO);
	@Async
	NotificationDTO createAndSendNotification(NotificationDTO notificationDTO) throws Exception;
	NotificationDTO updateNotification(String id,NotificationDTO notificationDTO);
	boolean saveNotificationToken(String userId,String token) throws Exception;
	boolean deleteNotificationTargetToken(String userId,String token) throws Exception;


}
