package ngo.nabarun.app.infra.service;

import java.util.List;

import ngo.nabarun.app.infra.misc.EmailTemplate;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;

public interface ICorrespondenceInfraService {

	void sendEmail(String senderName, List<CorrespondentDTO> recipient, EmailTemplate template);

	void sendEmail(String senderName, List<CorrespondentDTO> recipients, EmailTemplate template,
			List<DocumentDTO> attachFrom);

	void sendEmail(String senderName, List<CorrespondentDTO> recipients,
			String templateId, EmailTemplate template, List<DocumentDTO> attachFrom);
	

}
