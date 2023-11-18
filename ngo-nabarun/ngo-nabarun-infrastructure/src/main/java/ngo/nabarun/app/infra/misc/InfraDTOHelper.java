package ngo.nabarun.app.infra.misc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import ngo.nabarun.app.common.enums.AddressType;
import ngo.nabarun.app.common.enums.CommunicationMethod;
import ngo.nabarun.app.common.enums.DBContactType;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EventType;
import ngo.nabarun.app.common.enums.MeetingRefType;
import ngo.nabarun.app.common.enums.MeetingStatus;
import ngo.nabarun.app.common.enums.MeetingType;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.SocialMediaType;
import ngo.nabarun.app.common.enums.TicketScope;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;
import ngo.nabarun.app.infra.core.entity.DonationEntity;
import ngo.nabarun.app.infra.core.entity.MeetingEntity;
import ngo.nabarun.app.infra.core.entity.NoticeEntity;
import ngo.nabarun.app.infra.core.entity.SocialEventEntity;
import ngo.nabarun.app.infra.core.entity.TicketInfoEntity;
import ngo.nabarun.app.infra.core.entity.UserContactEntity;
import ngo.nabarun.app.infra.core.entity.UserProfileEntity;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.DiscussionDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.EmailDTO;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.SocialMediaDTO;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;

@Component
public class InfraDTOHelper {

	public static UserDTO convertToUserDTO(UserProfileEntity profile, AuthUser user) {
		UserDTO userDTO = new UserDTO();
		userDTO.setAbout(profile.getAbout());
		userDTO.setDateOfBirth(profile.getDateOfBirth());
		userDTO.setEmail(profile.getEmail());
		userDTO.setFirstName(profile.getFirstName());
		userDTO.setGender(profile.getGender());
		userDTO.setImageUrl(StringUtils.hasLength(profile.getAvatarUrl()) ? profile.getAvatarUrl()
				: (user == null ? null : user.getPicture()));
		userDTO.setLastName(profile.getLastName());
		userDTO.setMiddleName(profile.getMiddleName());
		userDTO.setProfileId(profile.getId());
		userDTO.setStatus(ProfileStatus.valueOf(profile.getProfileStatus()));
		/**
		 * Add User Ids
		 */
		userDTO.setUserIds(List.of(profile.getUserId()));
		userDTO.setTitle(profile.getTitle());

		UserAdditionalDetailsDTO uaDTO = new UserAdditionalDetailsDTO();
		uaDTO.setActiveContributor(profile.getActiveContributor());
		uaDTO.setBlocked(user == null ? false : user.isBlocked());
		uaDTO.setCreatedBy(null);
		uaDTO.setCreatedOn(user != null ? user.getCreatedAt() : profile.getCreatedOn());
		uaDTO.setDisplayPublic(profile.getDisplayPublic());
		uaDTO.setEmailVerified(user != null ? user.isEmailVerified() : false);
		uaDTO.setLastLogin(user != null ? user.getLastLogin() : null);
		uaDTO.setLastPasswordChange(user != null ? user.getLastPasswordReset() : null);
		uaDTO.setLoginsCount(user == null ? 0 : user.getLoginsCount());
		uaDTO.setUpdatedOn(user != null ? user.getUpdatedAt() : null);
		userDTO.setAdditionalDetails(uaDTO);
		userDTO.setRoleNames(InfraFieldHelper.stringToStringList(profile.getRoleString()));

		List<AddressDTO> addresses = new ArrayList<>();
		List<EmailDTO> emails = new ArrayList<>();
		List<PhoneDTO> phones = new ArrayList<>();
		List<SocialMediaDTO> socialmedias = new ArrayList<>();
		if (profile.getContacts() != null) {
			for (UserContactEntity contact : profile.getContacts()) {
				if (contact.getContactType() == DBContactType.ADDRESS) {
					addresses.add(new AddressDTO(contact.getId(), AddressType.valueOf(contact.getAddressType()),
							contact.getAddressLine(), contact.getAddressHometown(), contact.getAddressState(),
							contact.getAddressDistrict(), contact.getAddressCountry(), false));
				} else if (contact.getContactType() == DBContactType.EMAIL) {
					emails.add(new EmailDTO(contact.getId(), contact.getEmailType(), contact.getEmailValue()));
				} else if (contact.getContactType() == DBContactType.PHONE) {
					phones.add(new PhoneDTO(contact.getId(), PhoneType.valueOf(contact.getPhoneType()),
							contact.getPhoneCode(), contact.getPhoneNumber(), false));
				} else if (contact.getContactType() == DBContactType.SOCIALMEDIA) {
					socialmedias.add(
							new SocialMediaDTO(contact.getId(), SocialMediaType.valueOf(contact.getSocialMediaType()),
									contact.getSocialMediaName(), contact.getSocialMediaURL(), false));
				}
			}
		}
		userDTO.setAddresses(addresses);
		userDTO.setEmails(emails);
		userDTO.setSocialMedias(socialmedias);
		userDTO.setPhones(phones);

		return userDTO;
	}

	public static DonationDTO convertToDonationDTO(DonationEntity donation) {
		DonationDTO donationDTO = new DonationDTO();
		donationDTO.setAccountId(donation.getAccountId());
		donationDTO.setAmount(donation.getAmount());
		donationDTO.setConfirmedBy(donation.getPaymentConfirmedBy());
		donationDTO.setConfirmedOn(donation.getPaymentConfirmedOn());
		UserDTO userDTO = new UserDTO();
		if (donation.getIsGuest()) {
			userDTO.setFirstName(donation.getGuestFullNameOrOrgName());
			userDTO.setEmail(donation.getGuestEmailAddress());

			PhoneDTO phoneDTO = new PhoneDTO();
			phoneDTO.setPhoneNumber(donation.getGuestContactNumber());
			userDTO.setPhones(List.of(phoneDTO));
			donationDTO.setDonor(userDTO);
		} else {
			userDTO.setProfileId(donation.getProfile());
			donationDTO.setDonor(userDTO);
		}
		donationDTO.setEndDate(donation.getEndDate());
		donationDTO.setGuest(donation.getIsGuest());
		donationDTO.setId(donation.getId());
		donationDTO.setPaidOn(donation.getPaidOn());
		donationDTO.setPaymentMethod(donation.getPaymentMethod());
		donationDTO.setRaisedOn(donation.getRaisedOn());
		donationDTO.setStartDate(donation.getStartDate());
		donationDTO.setStatus(DonationStatus.valueOf(donation.getContributionStatus()));
		donationDTO.setTransactionRefNumber(donation.getTransactionRefNumber());
		donationDTO.setType(DonationType.valueOf(donation.getContributionType()));
		donationDTO.setForEventId(donation.getEventId());
		;
		return donationDTO;
	}

	public static DocumentDTO convertToDocumentDTO(DocumentRefEntity docRef) {
		DocumentDTO documentDTO = new DocumentDTO();
		documentDTO.setDocumentRefId(docRef.getDocumentRefId());
		documentDTO.setDocumentType(DocumentIndexType.valueOf(docRef.getDocumentType()));
		documentDTO.setFileType(docRef.getFileType());
		documentDTO.setImage(docRef.getFileType() == null ? false : docRef.getFileType().startsWith("image"));
		documentDTO.setOriginalFileName(docRef.getOriginalFileName());
		documentDTO.setDocId(docRef.getId());
		documentDTO.setDocumentURL(docRef.getDownloadUrl());
		return documentDTO;
	}

	public static RoleDTO convertToRoleDTO(AuthUserRole userRole) {
		RoleDTO roleDTO = new RoleDTO();
		//roleDTO.setCode(userRole.getRoleName());
		roleDTO.setDescription(userRole.getRoleDescription());
		roleDTO.setDisplayName(userRole.getRoleName());
		roleDTO.setId(userRole.getRoleId());
		roleDTO.setName(userRole.getRoleName());
		return roleDTO;
	}

	public static EventDTO convertToEventDTO(SocialEventEntity event) {
		EventDTO eventDTO = new EventDTO();
		eventDTO.setBudget(event.getEventBudget());
		eventDTO.setCoverPic(event.getCoverPicture());
		eventDTO.setCreatorId(event.getCreatedBy());
		eventDTO.setDescription(event.getDescription());
		eventDTO.setDraft(event.getDraft());
		eventDTO.setEventDate(event.getEventDate());
		eventDTO.setId(event.getId());
		eventDTO.setLocation(event.getEventLocation());
		eventDTO.setTitle(event.getTitle());
		eventDTO.setType(EventType.valueOf(event.getEventState()));
		return eventDTO;
	}

	public static NoticeDTO convertToNoticeDTO(NoticeEntity noticeEntity) {
		NoticeDTO noticeDTO = new NoticeDTO();
		noticeDTO.setCreatedBy(noticeEntity.getCreatedBy());
		noticeDTO.setCreatorRole(noticeEntity.getCreatorRole());
		noticeDTO.setDescription(noticeEntity.getDescription());
		noticeDTO.setDraft(noticeEntity.isDraft());
		noticeDTO.setId(noticeEntity.getId());
		noticeDTO.setNoticeDate(noticeEntity.getNoticeDate());
		noticeDTO.setNoticeNumber(noticeEntity.getNoticeNumber());
		noticeDTO.setPublishDate(
				noticeEntity.getPublishedOn() == null ? noticeEntity.getCreatedOn() : noticeEntity.getPublishedOn());
		// noticeDTO.setRecentNotice(noticeDTO.getPublishDate().compareTo(CommonUtils.getSystemDate()));
		noticeDTO.setTitle(noticeEntity.getTitle());
		// noticeDTO.setType(noticeEntity.getVisibility());
		return noticeDTO;
	}

	public static MeetingDTO convertToMeetingDTO(MeetingEntity meetEntity) {
		MeetingDTO meetingDTO = new MeetingDTO();
		meetingDTO.setAudioMeetingLink(meetEntity.getMeetingLinkA());
		meetingDTO.setDefaultReminder(meetEntity.isDefaultReminder());
		meetingDTO.setDescription(meetEntity.getDescription());
		meetingDTO.setDiscussions(meetEntity.getAdditionalDetails() == null ? null
				: meetEntity.getAdditionalDetails().stream().filter(f -> f.isDiscussionDetail())
						.map(m -> new DiscussionDTO(m.getId(), m.getAgenda(), m.getMinutes())).toList());

		meetingDTO.setAttendees(meetEntity.getAdditionalDetails() == null ? null
				: meetEntity.getAdditionalDetails().stream().filter(f -> f.isAttendeeDetail()).map(m -> {
					UserDTO userDTO = new UserDTO();
					String[] attendee = m.getAttendeeName().split(" ");
					userDTO.setFirstName(attendee.length > 0 ? attendee[0] : "");
					userDTO.setLastName(attendee.length > 1 ? attendee[1] : "");
					userDTO.setEmail(m.getAttendeeEmail());
					return userDTO;
				}).toList());

		meetingDTO.setEndTime(meetEntity.getEndTime());
		meetingDTO.setExtMeetingId(meetEntity.getExtMeetingId());
		meetingDTO.setId(meetEntity.getId());
		meetingDTO.setLocation(meetEntity.getLocation());
		meetingDTO.setRefId(meetEntity.getMeetingRefId());
		meetingDTO.setRefType(MeetingRefType.valueOf(meetEntity.getMeetingRefType()));
		meetingDTO.setRemarks(meetEntity.getRemarks());
		meetingDTO.setStartTime(meetEntity.getStartTime());
		meetingDTO.setStatus(MeetingStatus.valueOf(meetEntity.getMeetingStatus()));
		meetingDTO.setSummary(meetEntity.getSummary());
		meetingDTO.setType(MeetingType.valueOf(meetEntity.getType()));
		meetingDTO.setVideoMeetingLink(meetEntity.getMeetingLinkV());
		meetingDTO.setHtmlLink(meetEntity.getHtmlLink());
		meetingDTO.setExternalStatus(meetEntity.getExtEventStatus());

		if (meetEntity.getEmailReminderBeforeMin() != null) {
			meetingDTO.setEmailReminderBeforeMin(
					InfraFieldHelper.stringToIntegerList(meetEntity.getEmailReminderBeforeMin()));
		}
		if (meetEntity.getPopupReminderBeforeMin() != null) {
			meetingDTO.setPopupReminderBeforeMin(
					InfraFieldHelper.stringToIntegerList(meetEntity.getPopupReminderBeforeMin()));
		}

		return meetingDTO;
	}

	public static TicketDTO convertToTicketDTO(TicketInfoEntity tokenEntity) {
		TicketDTO ticketDTO = new TicketDTO();
		if (tokenEntity.getCommunicationMethod() != null) {
			ticketDTO.setCommunicationMethods(InfraFieldHelper.stringToStringList(tokenEntity.getCommunicationMethod())
					.stream().map(m -> CommunicationMethod.valueOf(m)).toList());
		}

		ticketDTO.setExpired(CommonUtils.getSystemDate().after(tokenEntity.getExpireOn()));
		ticketDTO.setId(tokenEntity.getId());
		ticketDTO.setIncorrectOTPCount(tokenEntity.getIncorrectOTPCount());
		ticketDTO.setOneTimePassword(tokenEntity.getOneTimePassword());
		ticketDTO.setRefId(tokenEntity.getRefId());
		if (tokenEntity.getScope() != null) {
			ticketDTO.setTicketScope(InfraFieldHelper.stringToStringList(tokenEntity.getScope()).stream()
					.map(m -> TicketScope.valueOf(m)).toList());
		}
		if (tokenEntity.getBaseTicketUrl() != null) {
			String url = UriComponentsBuilder.fromUriString(tokenEntity.getBaseTicketUrl())
					.queryParam("token", tokenEntity.getToken()).build().toUri().toString();
			ticketDTO.setTicketUrl(url);
		}

		ticketDTO.setToken(tokenEntity.getToken());

		UserDTO userInfo = new UserDTO();
		userInfo.setFirstName(tokenEntity.getName());
		userInfo.setEmail(tokenEntity.getEmail());
		userInfo.setPrimaryPhoneNumber(tokenEntity.getMobileNumber());
		ticketDTO.setUserInfo(userInfo);
		ticketDTO.setTicketStatus(TicketStatus.valueOf(tokenEntity.getStatus()));
		return ticketDTO;
	}

}
