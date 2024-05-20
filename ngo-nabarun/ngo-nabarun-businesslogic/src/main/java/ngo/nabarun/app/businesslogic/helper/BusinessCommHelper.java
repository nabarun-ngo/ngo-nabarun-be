package ngo.nabarun.app.businesslogic.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EmailRecipientType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.CorrespondentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO.EmailBodyTemplate;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO.EmailBodyTemplate.ContentTemplate;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO.EmailBodyTemplate.DetailsTemplate;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO.EmailBodyTemplate.FooterTemplate;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO.EmailBodyTemplate.HeaderTemplate;
import ngo.nabarun.app.infra.dto.EmailTemplateDTO.EmailBodyTemplate.DetailsTemplate.FieldTemplate;
import ngo.nabarun.app.infra.service.ICorrespondenceInfraService;
import ngo.nabarun.app.infra.service.IGlobalDataInfraService;

@Service
@Deprecated
class BusinessCommHelper {

	@Autowired
	private ICorrespondenceInfraService correspondenceInfraService;

	

	public void sendEmail(DonationDTO donation,EmailTemplateDTO actualTemplate) {
		List<CorrespondentDTO> recipients= new ArrayList<>();
		recipients.add(CorrespondentDTO.builder().email(donation.getDonor().getEmail()).name(donation.getDonor().getName())
				.emailRecipientType(EmailRecipientType.TO).build());
		
		correspondenceInfraService.sendEmail(null, recipients, actualTemplate);
	}

	
	@Autowired
	private IGlobalDataInfraService domainRefConfigInfraService;
	
	public void sendEmailOnDonationCreate(DonationDTO donation) {
		/**
		 * For Guest Donation during creation no mail to be sent Mail will be sent on
		 * confirmation
		 */
		if (donation.getGuest() == Boolean.TRUE) {
			return;
		}
		
		/**
		 * Initializing Email Templates
		 */
		String subject;
		HeaderTemplate header;
		ContentTemplate content;
		FooterTemplate footer;
		List<CorrespondentDTO> recipients = new ArrayList<>();
		EmailTemplateDTO actualTemplate;
		
		
		try {

			/**
			 * Calculating last day of donation payment
			 */
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, 15);// take from config
			Date lastDate = cal.getTime();
			if (lastDate.before(new Date())) {
				lastDate = donation.getEndDate();
			}

			if (donation.getType() == DonationType.REGULAR) {
				/**
				 * Retrieving template from configuration
				 */
				EmailTemplateDTO template = domainRefConfigInfraService.getEmailTemplate("ON_DONATION_CREATE_REGULAR");
				header = template.getBody().getHeader();
				content = template.getBody().getContent();
				footer = template.getBody().getFooter();

				/**
				 * Calculating/ storing values to be replaced
				 */
				String months = Joiner.on(", ").join(
						CommonUtils.getMonthsBetween(donation.getStartDate(), donation.getEndDate(), "MMMM yyyy"));
				String donorName = donation.getDonor().getFirstName();
				String amount = String.format("%.2f", donation.getAmount());

				/**
				 * Substituting variables
				 */
				subject=template.getSubject() + " | " + months;
				header.setSubHeading(String.format(header.getSubHeading(), donorName, amount, months));
				List<DetailsTemplate> details = content.getDetails();

				for (DetailsTemplate detail : details) {
					for (FieldTemplate field : detail.getFields()) {
						switch (field.getValue()) {
						case "TYPE":
							field.setValue(donation.getType().name());
							break;
						case "AMOUNT":
							field.setValue(amount);
							break;
						case "PERIOD":
							field.setValue(CommonUtils.getFormattedDate(donation.getStartDate(), "dd MMMM yyyy") + " - "
									+ CommonUtils.getFormattedDate(donation.getEndDate(), "dd MMMM yyyy"));
							break;
						case "STATUS":
							field.setValue(donation.getStatus().name());
							break;
						default:
							break;
						}
					}
				}
				content.setParagraph2_blue(String.format(content.getParagraph2_blue(),
						CommonUtils.getFormattedDate(lastDate, "dd MMMM yyyy")));
				recipients.add(CorrespondentDTO.builder().email(donation.getDonor().getEmail()).name(donorName)
						.emailRecipientType(EmailRecipientType.TO).build());
			} else {
				/**
				 * Retrieving template from configuration
				 */
				EmailTemplateDTO template = domainRefConfigInfraService.getEmailTemplate("ON_DONATION_CREATE_ONETIME");
				header = template.getBody().getHeader();
				content = template.getBody().getContent();
				footer = template.getBody().getFooter();
				
				/**
				 * Calculating/ storing values to be replaced
				 */
				String donorName = donation.getDonor().getFirstName();
				String amount = String.format("%.2f", donation.getAmount());
				
				/**
				 * Substituting variables
				 */
				subject=template.getSubject();
				header.setSubHeading(String.format(header.getSubHeading(), donorName,amount));
				List<DetailsTemplate> details = content.getDetails();

				for (DetailsTemplate detail : details) {
					for (FieldTemplate field : detail.getFields()) {
						switch (field.getValue()) {
						case "TYPE":
							field.setValue(donation.getType().name());
							break;
						case "AMOUNT":
							field.setValue(amount);
							break;
						case "STATUS":
							field.setValue(donation.getStatus().name());
							break;
						default:
							break;
						}
					}
				}
				
				content.setParagraph2_blue(String.format(content.getParagraph2_blue(),
						CommonUtils.getFormattedDate(lastDate, "dd MMMM yyyy")));
				recipients.add(CorrespondentDTO.builder().email(donation.getDonor().getEmail()).name(donorName)
						.emailRecipientType(EmailRecipientType.TO).build());
			}
			actualTemplate = new EmailTemplateDTO();
			actualTemplate.setBody(new EmailBodyTemplate(header, content, footer));
			actualTemplate.setSubject(subject);
			correspondenceInfraService.sendEmail(null, recipients, actualTemplate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
