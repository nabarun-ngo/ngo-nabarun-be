package ngo.nabarun.app.ext.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;


@Service
public interface IEmailExtService {

	int sendEmail(String subject, String senderName, List<Map<String,String>> recipients, String templateId, Object templateData,
			List<Map<String,String>> attachFrom);
}
