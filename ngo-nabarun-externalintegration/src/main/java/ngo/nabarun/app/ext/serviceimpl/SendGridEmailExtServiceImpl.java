package ngo.nabarun.app.ext.serviceimpl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.ext.service.IEmailExtService;

@Service
@Slf4j
public class SendGridEmailExtServiceImpl implements IEmailExtService {

	@Autowired
	private PropertyHelper propertyHelper;


	@Override
	public int sendEmail(String subject, String senderName, List<Map<String, String>> recipients, String templateId,
			Object templateData, List<Map<String, String>> attachFrom) {
	
		Email from = new Email(propertyHelper.getDefaultEmailSender(), senderName);
		Mail mail = new Mail();
		mail.setFrom(from);
		mail.setSubject(subject);
		
		for (Map<String, String> attach : attachFrom) {
			Attachments attachment = new Attachments();
			attachment.setContent(attach.get("content"));
			attachment.setContentId(attach.get("contentId"));
			attachment.setDisposition(attach.get("disposition"));
			attachment.setFilename(attach.get("fileName"));
			attachment.setType(attach.get("fileType"));
			mail.addAttachments(attachment);
		}

		Personalization personalization = new Personalization();
		
		/**
		 * If Environment is equals to any of the production profile name or prod mode
		 * in test is enabled for any profile or environment
		 */
		if (propertyHelper.isProdEnv() || propertyHelper.isProdModeEnabledForTest()) {
			for (Map<String, String> recipient : recipients) {
				switch (EmailRecipientType.valueOf(recipient.get("recipientType"))) {
				case TO:
					personalization.addTo(new Email(recipient.get("recipientEmail"), recipient.get("recipientName")));
					break;
				case CC:
					personalization.addCc(new Email(recipient.get("recipientEmail"), recipient.get("recipientName")));
					break;
				case BCC:
					personalization.addBcc(new Email(recipient.get("recipientEmail"), recipient.get("recipientName")));
					break;
				}
			}
		} else if (propertyHelper.isEmailMockingEnabledForTest()) {
			for (String testEmail : propertyHelper.getMockedEmailAddress()) {
				personalization.addTo(new Email(testEmail));
			}
		}

		personalization.setSubject(subject);
		personalization.addDynamicTemplateData("subject", subject);
		personalization.addDynamicTemplateData("body", templateData);

		mail.setTemplateId(templateId == null ? propertyHelper.getDefaultEmailTemplateIdSendGrid() : templateId);
		mail.addPersonalization(personalization);
		if (personalization.getTos().isEmpty() && personalization.getCcs().isEmpty()
				&& personalization.getBccs().isEmpty()) {
			log.warn(
					"Email sending skipped as no recipient email added! probably prod mode is disabled in NON PROD env !");
			return 10;
		} else {
			String emailContent = null;
			try {
				emailContent = mail.build();
				SendGrid sg = new SendGrid(propertyHelper.getSendGridAPIKey());
				Request request = new Request();
				request.setMethod(Method.POST);
				request.setEndpoint("mail/send");
				request.setBody(emailContent);
				Response response = sg.api(request);
				if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
					log.debug("Email Sent "
							+ emailRecipientString(personalization.getTos(), personalization.getCcs(),
									personalization.getBccs())
							+ "! " + "Status Code : " + response.getStatusCode() + " Response Body: "
							+ response.getBody());
				} else {
					log.error("Email Sending "
							+ emailRecipientString(personalization.getTos(), personalization.getCcs(),
									personalization.getBccs())
							+ " Failed! " + "Status Code : " + response.getStatusCode() + " Response Body: "
							+ response.getBody());
				}
				return response.getStatusCode();
			} catch (Exception e) {
				e.printStackTrace();
				return 11;
			}
		}
	}

	private String emailRecipientString(List<Email> to, List<Email> cc, List<Email> bcc) {
		return "To " + (to == null ? "No one" : to.stream().map(m -> m.getEmail()).toList()) + ", Cc "
				+ (cc == null ? "No one" : cc.stream().map(m -> m.getEmail()).toList()) + ", Bcc "
				+ (bcc == null ? "No one" : bcc.stream().map(m -> m.getEmail()).toList()) + "";
	}
}
