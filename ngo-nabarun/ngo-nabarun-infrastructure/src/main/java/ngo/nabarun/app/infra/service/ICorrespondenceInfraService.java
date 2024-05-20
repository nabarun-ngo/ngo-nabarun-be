package ngo.nabarun.app.infra.service;

import java.util.List;

import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;

public interface ICorrespondenceInfraService {

	void sendEmail(String senderName, List<CorrespondentDTO> recipient, EmailTemplateDTO template);

	void sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplateDTO template,
			List<DocumentDTO> attachFrom);

	void sendEmail(String senderName, List<CorrespondentDTO> recipients,
			String templateId, EmailTemplateDTO template, List<DocumentDTO> attachFrom);
	

}
