package ngo.nabarun.app.infra.misc;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.AccountType;
import ngo.nabarun.app.common.enums.AddressType;
import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.common.enums.CommunicationMethod;
import ngo.nabarun.app.common.enums.DocumentIndexType;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.EventType;
import ngo.nabarun.app.common.enums.ExpenseRefType;
import ngo.nabarun.app.common.enums.ExpenseStatus;
import ngo.nabarun.app.common.enums.LoginMethod;
import ngo.nabarun.app.common.enums.AdditionalFieldKey;
import ngo.nabarun.app.common.enums.MeetingStatus;
import ngo.nabarun.app.common.enums.MeetingType;
import ngo.nabarun.app.common.enums.NoticeStatus;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.common.enums.SocialMediaType;
import ngo.nabarun.app.common.enums.TicketStatus;
import ngo.nabarun.app.common.enums.TicketType;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.enums.UPIOption;
import ngo.nabarun.app.common.enums.WorkAction;
import ngo.nabarun.app.common.enums.WorkType;
import ngo.nabarun.app.common.enums.WorkDecision;
import ngo.nabarun.app.common.enums.RequestStatus;
import ngo.nabarun.app.common.enums.RequestType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.common.util.CryptUtil;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.infra.core.entity.AccountEntity;
import ngo.nabarun.app.infra.core.entity.ApiKeyEntity;
import ngo.nabarun.app.infra.core.entity.CustomFieldEntity;
import ngo.nabarun.app.infra.core.entity.DocumentRefEntity;
import ngo.nabarun.app.infra.core.entity.DonationEntity;
import ngo.nabarun.app.infra.core.entity.ExpenseEntity;
import ngo.nabarun.app.infra.core.entity.ExpenseItemEntity;
import ngo.nabarun.app.infra.core.entity.LogsEntity;
import ngo.nabarun.app.infra.core.entity.NoticeEntity;
import ngo.nabarun.app.infra.core.entity.SocialEventEntity;
import ngo.nabarun.app.infra.core.entity.TicketInfoEntity;
import ngo.nabarun.app.infra.core.entity.TransactionEntity;
import ngo.nabarun.app.infra.core.entity.UserProfileEntity;
import ngo.nabarun.app.infra.core.entity.WorkListEntity;
import ngo.nabarun.app.infra.core.entity.WorkflowEntity;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;
import ngo.nabarun.app.infra.dto.BankDTO;
import ngo.nabarun.app.infra.dto.DocumentDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.ExpenseDTO;
import ngo.nabarun.app.infra.dto.ExpenseDTO.ExpenseItemDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.LogsDTO;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.SocialMediaDTO;
import ngo.nabarun.app.infra.dto.TicketDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UpiDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;

@Component
public class InfraDTOHelper {

	public static UserDTO convertToUserDTO(UserProfileEntity profile, AuthUser user) {
		return convertToUserDTO(profile, user, null);
	}

	public static UserDTO convertToUserDTO(UserProfileEntity profile, AuthUser user, List<AuthUserRole> roles) {

		UserDTO userDTO = new UserDTO();
		userDTO.setAbout(profile == null ? null : profile.getAbout());
		userDTO.setDateOfBirth(profile == null ? null : profile.getDateOfBirth());
		userDTO.setEmail(profile == null ? (user == null ? null : user.getEmail()) : profile.getEmail());
		userDTO.setFirstName(profile == null ? (user == null ? null : user.getFirstName()) : profile.getFirstName());
		userDTO.setGender(profile == null ? null : profile.getGender());
		userDTO.setImageUrl(
				StringUtils.hasLength(profile != null ? profile.getAvatarUrl() : null) ? profile.getAvatarUrl()
						: (user == null ? null : user.getPicture()));

		userDTO.setLastName(profile == null ? (user == null ? null : user.getLastName()) : profile.getLastName());
		userDTO.setMiddleName(profile == null ? null : profile.getMiddleName());
		userDTO.setName(profile == null ? (user == null ? null : user.getFullName())
				: profile.getFirstName() + " " + profile.getLastName());
		userDTO.setProfileId(profile == null ? (user == null ? null : user.getProfileId()) : profile.getId());
		userDTO.setStatus(
				profile == null || profile.getStatus() == null ? null : ProfileStatus.valueOf(profile.getStatus()));

		userDTO.setUserId(profile == null ? (user == null ? null : user.getUserId()) : profile.getUserId());
		userDTO.setTitle(profile == null ? null : profile.getTitle());
		
		userDTO.setLoginProviders(user == null ? (profile == null ? null
				: InfraFieldHelper.stringToStringList(profile.getLoginMethods()).stream()
				.map(m -> LoginMethod.valueOf(m)).collect(Collectors.toList())) : user.getProviders());
		/**
		 * additional details
		 */
		UserAdditionalDetailsDTO uaDTO = new UserAdditionalDetailsDTO();
		uaDTO.setActiveContributor(profile == null ? false : profile.getActiveContributor());
		uaDTO.setBlocked(user == null ? false : user.isBlocked());
		uaDTO.setCreatedBy(null);
		uaDTO.setCreatedOn(user != null ? user.getCreatedAt() : profile.getCreatedOn());
		uaDTO.setDisplayPublic(profile == null ? false : profile.getPublicProfile());
		uaDTO.setEmailVerified(
				user != null ? (user.getEmailVerified() == null ? false : user.getEmailVerified()) : false);
		uaDTO.setPasswordResetRequired(
				user == null ? false : user.getResetPassword() == null ? false : user.getResetPassword());
		uaDTO.setLastLogin(user != null ? user.getLastLogin() : null);
		uaDTO.setLastPasswordChange(user != null ? user.getLastPasswordReset() : null);
		uaDTO.setLoginsCount(user == null ? 0 : user.getLoginsCount());
		uaDTO.setUpdatedOn(user != null ? user.getUpdatedAt() : null);
		uaDTO.setAttributes(user != null ? user.getAttributes() : null);
		uaDTO.setDonPauseStartDate(profile == null ? null : profile.getDonationPauseStartDate());
		uaDTO.setDonPauseEndDate(profile == null ? null : profile.getDonationPauseEndDate());
		userDTO.setAdditionalDetails(uaDTO);

		if (profile != null) {
			/**
			 * roles
			 */
			// userDTO.setRoleNames(InfraFieldHelper.stringToStringList(profile.getRoleNames()));
			if (roles != null) {
				userDTO.setRoles(roles.stream().map(m -> convertToRoleDTO(m)).toList());
			} else {
				List<String> roleCodes = InfraFieldHelper.stringToStringList(profile.getRoleCodes());
				List<String> roleNames = InfraFieldHelper.stringToStringList(profile.getRoleNames());
				List<RoleDTO> roleList = new ArrayList<RoleDTO>();
				for (int i = 0; i < roleCodes.size(); i++) {
					if (StringUtils.hasLength(roleCodes.get(i))) {
						RoleDTO role = new RoleDTO();
						role.setName(roleNames.size() > i ? roleNames.get(i) : null);
						role.setCode(RoleCode.valueOf(roleCodes.get(i)));
						roleList.add(role);
					}
					;
				}
				userDTO.setRoles(roleList);
			}

			/**
			 * address
			 */
			List<AddressDTO> addresses = new ArrayList<>();
			if (profile.getAddressLine1() != null || profile.getAddressLine2() != null
					|| profile.getAddressLine3() != null || profile.getHometown() != null
					|| profile.getDistrict() != null || profile.getState() != null || profile.getCountry() != null) {
				AddressDTO present = new AddressDTO();
				present.setAddressType(AddressType.PRESENT);
				present.setAddressLine1(profile.getAddressLine1());
				present.setAddressLine2(profile.getAddressLine2());
				present.setAddressLine3(profile.getAddressLine3());
				present.setHometown(profile.getHometown());
				present.setDistrict(profile.getDistrict());
				present.setState(profile.getState());
				present.setCountry(profile.getCountry());
				addresses.add(present);
			}
			if (profile.getPermanentAddressLine1() != null || profile.getPermanentAddressLine2() != null
					|| profile.getPermanentAddressLine3() != null || profile.getPermanentHometown() != null
					|| profile.getPermanentDistrict() != null || profile.getPermanentState() != null
					|| profile.getPermanentCountry() != null) {
				AddressDTO permanent = new AddressDTO();
				permanent.setAddressType(AddressType.PERMANENT);
				permanent.setAddressLine1(profile.getPermanentAddressLine1());
				permanent.setAddressLine2(profile.getPermanentAddressLine2());
				permanent.setAddressLine3(profile.getPermanentAddressLine3());
				permanent.setHometown(profile.getPermanentHometown());
				permanent.setDistrict(profile.getPermanentDistrict());
				permanent.setState(profile.getPermanentState());
				permanent.setCountry(profile.getPermanentCountry());
				addresses.add(permanent);
			}
			userDTO.setAddresses(addresses);
			userDTO.setPresentPermanentSame(profile.getPresentPermanentSame());

			/**
			 * phone number
			 */
			List<PhoneDTO> phones = new ArrayList<>();
			if (profile.getPhoneNumber() != null) {
				PhoneDTO primary_phone = new PhoneDTO();
				primary_phone.setPhoneType(PhoneType.PRIMARY);
				List<String> fieldValue = InfraFieldHelper.stringToStringList(profile.getPhoneNumber(), "-");
				if (fieldValue.size() == 2) {
					primary_phone.setPhoneCode(fieldValue.get(0));
					primary_phone.setPhoneNumber(fieldValue.get(1));
					userDTO.setPhoneNumber(fieldValue.get(0) + fieldValue.get(1));
				} else if (fieldValue.size() == 1) {
					primary_phone.setPhoneNumber(fieldValue.get(0));
					userDTO.setPhoneNumber(fieldValue.get(0));
				}
				phones.add(primary_phone);
			}

			if (profile.getAltPhoneNumber() != null) {
				PhoneDTO alt_phone = new PhoneDTO();
				alt_phone.setPhoneType(PhoneType.ALTERNATIVE);
				List<String> fieldValue = InfraFieldHelper.stringToStringList(profile.getAltPhoneNumber(), "-");
				if (fieldValue.size() == 2) {
					alt_phone.setPhoneCode(fieldValue.get(0));
					alt_phone.setPhoneNumber(fieldValue.get(1));
				} else if (fieldValue.size() == 1) {
					alt_phone.setPhoneNumber(fieldValue.get(0));
				}
				phones.add(alt_phone);
			}
			userDTO.setPhones(phones);

			/**
			 * social media
			 */
			List<SocialMediaDTO> socialmedias = new ArrayList<>();

			if (profile.getFacebookLink() != null) {
				SocialMediaDTO socialMedia = new SocialMediaDTO();
				socialMedia.setSocialMediaType(SocialMediaType.FACEBOOK);
				socialMedia.setSocialMediaURL(profile.getFacebookLink());
				socialmedias.add(socialMedia);
			}

			if (profile.getInstagramLink() != null) {
				SocialMediaDTO socialMedia = new SocialMediaDTO();
				socialMedia.setSocialMediaType(SocialMediaType.INSTAGRAM);
				socialMedia.setSocialMediaURL(profile.getInstagramLink());
				socialmedias.add(socialMedia);
			}

			if (profile.getTwitterLink() != null) {
				SocialMediaDTO socialMedia = new SocialMediaDTO();
				socialMedia.setSocialMediaType(SocialMediaType.TWITTER);
				socialMedia.setSocialMediaURL(profile.getTwitterLink());
				socialmedias.add(socialMedia);
			}

			if (profile.getLinkedInLink() != null) {
				SocialMediaDTO socialMedia = new SocialMediaDTO();
				socialMedia.setSocialMediaType(SocialMediaType.LINKEDIN);
				socialMedia.setSocialMediaURL(profile.getLinkedInLink());
				socialmedias.add(socialMedia);
			}

			if (profile.getWhatsappLink() != null) {
				SocialMediaDTO socialMedia = new SocialMediaDTO();
				socialMedia.setSocialMediaType(SocialMediaType.WHATSAPP);
				socialMedia.setSocialMediaURL(profile.getWhatsappLink());
				socialmedias.add(socialMedia);
			}
			userDTO.setSocialMedias(socialmedias);
		}

		return userDTO;
	}

	public static DonationDTO convertToDonationDTO(DonationEntity donation) {
		DonationDTO donationDTO = new DonationDTO();
		donationDTO.setAmount(donation.getAmount());
		UserDTO confirmedByDTO = new UserDTO();
		confirmedByDTO.setName(donation.getPaymentConfirmedByName());
		confirmedByDTO.setProfileId(donation.getPaymentConfirmedBy());
		donationDTO.setConfirmedBy(confirmedByDTO);
		donationDTO.setConfirmedOn(donation.getPaymentConfirmedOn());
		UserDTO userDTO = new UserDTO();
		if (!donation.getIsGuest()) {
			userDTO.setProfileId(donation.getProfile());
			userDTO.setUserId(donation.getUserId());
		}
		userDTO.setName(donation.getDonorName());
		userDTO.setEmail(donation.getDonorEmailAddress());
		userDTO.setPhoneNumber(donation.getDonorContactNumber());
		donationDTO.setDonor(userDTO);

		donationDTO.setEndDate(donation.getEndDate());
		donationDTO.setGuest(donation.getIsGuest());
		donationDTO.setId(donation.getId());
		donationDTO.setPaidOn(donation.getPaidOn());
		donationDTO.setPaymentMethod(
				donation.getPaymentMethod() == null ? null : PaymentMethod.valueOf(donation.getPaymentMethod()));
		donationDTO.setRaisedOn(donation.getRaisedOn());
		donationDTO.setStartDate(donation.getStartDate());
		donationDTO.setStatus(DonationStatus.valueOf(donation.getStatus()));
		donationDTO.setTransactionRefNumber(donation.getTransactionRefNumber());
		donationDTO.setType(DonationType.valueOf(donation.getType()));
		donationDTO.setForEventId(donation.getEventId());
		donationDTO.setComment(donation.getComment());
		;

		donationDTO.setUpiName(donation.getPaidUPIName() == null ? null : UPIOption.valueOf(donation.getPaidUPIName()));
		donationDTO.setIsPaymentNotified(donation.getIsPaymentNotified());
		donationDTO.setPaymentNotificationDate(donation.getNotifiedOn());
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setId(donation.getAccountId());
		accountDTO.setAccountName(donation.getAccountName());
		donationDTO.setPaidToAccount(accountDTO);

		donationDTO.setCancelReason(donation.getCancelReason());
		donationDTO.setPayLaterReason(donation.getPayLaterReason());
		donationDTO.setPaymentFailDetail(donation.getPaymentFailDetail());

		if (donation.getCustomFields() != null) {
			List<FieldDTO> list = donation.getCustomFields().stream().map(m -> {
				return convertToFieldDTO(m);
			}).toList();
			donationDTO.setAdditionalFields(list);
		}
		return donationDTO;
	}

	public static FieldDTO convertToFieldDTO(CustomFieldEntity field) {
		return convertToFieldDTO(field, "");
	}

	public static FieldDTO convertToFieldDTO(CustomFieldEntity field, String secret) {
		// log.debug("Custom Field",field);
		FieldDTO fieldDTO = new FieldDTO();
		fieldDTO.setFieldDescription(field.getFieldDescription());
		fieldDTO.setFieldKey(AdditionalFieldKey.valueOf(field.getFieldKey()));
		fieldDTO.setFieldName(field.getFieldName());
		fieldDTO.setFieldType(field.getFieldType());
		fieldDTO.setFieldId(field.getId());
		fieldDTO.setFieldSource(field.getSource());
		fieldDTO.setFieldSourceType(field.getSourceType());

		fieldDTO.setHidden(field.isHidden());
		if (field.isEncrypted()) {
			IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(field.getEncryptionIV()));
			String salt = field.getEncryptionSalt();
			String value = field.getFieldValue();
			SecretKey secretKeyObj;
			try {
				secretKeyObj = CryptUtil.getKeyFromPassword(secret, salt);
				fieldDTO.setFieldValue(CryptUtil.decrypt(value, secretKeyObj, iv));
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			fieldDTO.setFieldValue(field.getFieldValue());
		}
		fieldDTO.setEncrypted(field.isEncrypted());

		fieldDTO.setMandatory(field.isMandatory());
		fieldDTO.setFieldOptions(InfraFieldHelper.stringToStringList(field.getFieldValueOptions()));
		fieldDTO.setFieldValueType(field.getFieldValueType());
		return fieldDTO;
	}

	public static DocumentDTO convertToDocumentDTO(DocumentRefEntity docRef) {
		DocumentDTO documentDTO = new DocumentDTO();
		documentDTO.setDocumentRefId(docRef.getDocumentRefId());
		documentDTO.setDocumentType(DocumentIndexType.valueOf(docRef.getDocumentType()));
		documentDTO.setFileType(docRef.getFileType());
		documentDTO.setImage(docRef.getFileType() == null ? false : docRef.getFileType().startsWith("image"));
		documentDTO.setOriginalFileName(docRef.getOriginalFileName());
		documentDTO.setRemoteFileName(docRef.getRemoteFileName());
		documentDTO.setDocId(docRef.getId());
		documentDTO.setDocumentURL(docRef.getDownloadUrl());
		return documentDTO;
	}

	public static RoleDTO convertToRoleDTO(AuthUserRole userRole) {
		RoleDTO roleDTO = new RoleDTO();
		roleDTO.setDescription(userRole.getRoleDescription());
		// roleDTO.setName(userRole.getRoleDescription());
		roleDTO.setCode(RoleCode.valueOf(userRole.getRoleName()));
		roleDTO.setAuth0Id(userRole.getRoleId());
		return roleDTO;
	}

//	public static RoleDTO convertToRoleDTO(UserRoleEntity userRole) {
//		RoleDTO roleDTO = new RoleDTO();
//		roleDTO.setCode(RoleCode.valueOf(userRole.getRoleCode()));
//		roleDTO.setDescription(userRole.getRoleDescription());
//		roleDTO.setName(userRole.getRoleName());
//		roleDTO.setId(userRole.getId());
//		roleDTO.setAuth0Id(userRole.getExtRoleId());
//		List<String> groups=InfraFieldHelper.stringToStringList(userRole.getRoleGroup());
//		//roleDTO.setGroups(groups.stream().map(m->RoleGroup.valueOf(m)).toList());
//		roleDTO.setGroups(groups);
//		return roleDTO;
//	}

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
		UserDTO createdBy = new UserDTO();
		createdBy.setName(noticeEntity.getCreatedBy());
		createdBy.setProfileId(noticeEntity.getCreatedById());
		noticeDTO.setCreatedBy(createdBy);
		noticeDTO.setCreatorRole(noticeEntity.getCreatorRole());
		noticeDTO.setDescription(noticeEntity.getDescription());
		noticeDTO.setId(noticeEntity.getId());
		noticeDTO.setNoticeDate(noticeEntity.getNoticeDate());
		// noticeDTO.setNoticeNumber(noticeEntity.getNoticeNumber());
		noticeDTO.setPublishDate(
				noticeEntity.getPublishedOn() == null ? noticeEntity.getCreatedOn() : noticeEntity.getPublishedOn());
		noticeDTO.setTitle(noticeEntity.getTitle());
		noticeDTO.setStatus(noticeEntity.getStatus() == null ? null : NoticeStatus.valueOf(noticeEntity.getStatus()));
		noticeDTO.setNeedMeeting(noticeEntity.getNeedMeeting());
		if (noticeEntity.getNeedMeeting() != null && noticeEntity.getNeedMeeting()) {
			MeetingDTO meetingDTO = new MeetingDTO();
			meetingDTO.setAudioMeetingLink(noticeEntity.getMeetingLinkA());
			meetingDTO.setDescription(noticeEntity.getMeetingDescription());

			if (noticeEntity.getAttendeeEmails() != null) {
				List<UserDTO> attendees = new ArrayList<>();
				List<String> attendeesEmail = InfraFieldHelper.stringToStringList(noticeEntity.getAttendeeEmails());
				List<String> attendeesNames = InfraFieldHelper.stringToStringList(noticeEntity.getAttendeeNames());

				for (int i = 0; i < attendeesEmail.size(); i++) {
					UserDTO attendee = new UserDTO();
					attendee.setEmail(attendeesEmail.get(i));
					attendee.setName(attendeesNames.get(i));
					attendees.add(attendee);
				}
				meetingDTO.setAttendees(attendees);
			}

			meetingDTO.setEndTime(noticeEntity.getMeetingEndTime());
			meetingDTO.setExtMeetingId(noticeEntity.getExtMeetingId());
			meetingDTO.setLocation(noticeEntity.getMeetingLocation());
			meetingDTO.setRemarks(noticeEntity.getMeetingRemarks());
			meetingDTO.setStartTime(noticeEntity.getMeetingStartTime());
			meetingDTO.setStatus(noticeEntity.getMeetingStatus() == null ? null
					: MeetingStatus.valueOf(noticeEntity.getMeetingStatus()));
			meetingDTO.setSummary(noticeEntity.getMeetingSummary());
			meetingDTO.setType(
					noticeEntity.getMeetingType() == null ? null : MeetingType.valueOf(noticeEntity.getMeetingType()));
			meetingDTO.setVideoMeetingLink(noticeEntity.getMeetingLinkV());
			meetingDTO.setHtmlLink(noticeEntity.getHtmlLink());
			meetingDTO.setExternalStatus(noticeEntity.getExtEventStatus());
			meetingDTO.setDate(noticeEntity.getMeetingDate());
			noticeDTO.setMeeting(meetingDTO);
		}
		return noticeDTO;
	}

	public static LogsDTO convertToLogsDTO(LogsEntity logsEntity) {
		LogsDTO logsDTO = new LogsDTO();
		logsDTO.setCorelationId(logsEntity.getCorelationId());
		logsDTO.setDuration(logsEntity.getEndTime().getTime() - logsEntity.getStartTime().getTime());
		logsDTO.setEndTime(logsEntity.getEndTime());
		logsDTO.setId(logsEntity.getId());
		logsDTO.setInputs(logsEntity.getInputs());
		logsDTO.setMethodName(logsEntity.getMethodName());
		logsDTO.setOutputs(logsEntity.getOutputs());
		logsDTO.setStartTime(logsEntity.getStartTime());
		logsDTO.setType(logsEntity.getType());
		logsDTO.setError(logsEntity.getError());
		return logsDTO;
	}

	public static TicketDTO convertToTicketDTO(TicketInfoEntity tokenEntity) {
		TicketDTO ticketDTO = new TicketDTO(TicketType.valueOf(tokenEntity.getType()));
		if (tokenEntity.getCommunicationMethod() != null) {
			ticketDTO.setCommunicationMethods(InfraFieldHelper.stringToStringList(tokenEntity.getCommunicationMethod())
					.stream().map(m -> CommunicationMethod.valueOf(m)).toList());
		}

		ticketDTO.setExpired(CommonUtils.getSystemDate().after(tokenEntity.getExpireOn()));
		ticketDTO.setId(tokenEntity.getId());
		ticketDTO.setIncorrectOTPCount(tokenEntity.getIncorrectOTPCount());
		ticketDTO.setOneTimePassword(tokenEntity.getOneTimePassword());
		ticketDTO.setRefId(tokenEntity.getRefId());
		ticketDTO.setAcceptCode(tokenEntity.getAcceptCode());
		ticketDTO.setDeclineCode(tokenEntity.getDeclineCode());

		if (tokenEntity.getScope() != null) {
			ticketDTO.setTicketScope(InfraFieldHelper.stringToStringList(tokenEntity.getScope()));
		}

		ticketDTO.setToken(tokenEntity.getToken());

		UserDTO userInfo = new UserDTO();
		userInfo.setProfileId(tokenEntity.getForUserId());
		userInfo.setName(tokenEntity.getName());
		userInfo.setEmail(tokenEntity.getEmail());
		userInfo.setPhoneNumber(tokenEntity.getMobileNumber());
		ticketDTO.setUserInfo(userInfo);
		ticketDTO.setTicketStatus(TicketStatus.valueOf(tokenEntity.getStatus()));
		ticketDTO.setExpireOn(tokenEntity.getExpireOn());
		return ticketDTO;
	}

	public static TransactionDTO convertToTransactionDTO(TransactionEntity txnEntity, AccountEntity fromAccount,
			AccountEntity toAccount) {
		TransactionDTO transactionDTO = new TransactionDTO();
		transactionDTO.setComment(txnEntity.getComment());
		transactionDTO.setFromAccBalAfterTxn(txnEntity.getFromAccBalAfterTxn());
		transactionDTO.setTxnDescription(txnEntity.getTransactionDescription());

		if (fromAccount != null) {
			transactionDTO.setFromAccount(convertToAccountDTO(fromAccount, null));
		} else {
			AccountDTO fromAcc = new AccountDTO();
			fromAcc.setId(txnEntity.getFromAccount());
			UserDTO fromAccUser = new UserDTO();
			fromAccUser.setUserId(txnEntity.getFromAccountUserId());
			fromAcc.setProfile(fromAccUser);
			transactionDTO.setFromAccount(fromAcc);
		}

		transactionDTO.setId(txnEntity.getId());
		transactionDTO.setToAccBalAfterTxn(txnEntity.getToAccBalAfterTxn());
		if (toAccount != null) {
			transactionDTO.setToAccount(convertToAccountDTO(toAccount, null));
		} else {
			AccountDTO toAcc = new AccountDTO();
			toAcc.setId(txnEntity.getToAccount());
			UserDTO toAccUser = new UserDTO();
			toAccUser.setUserId(txnEntity.getToAccountUserId());
			toAcc.setProfile(toAccUser);
			transactionDTO.setToAccount(toAcc);
		}

		transactionDTO.setTxnAmount(txnEntity.getTransactionAmt());
		transactionDTO.setTxnDate(txnEntity.getTransactionDate());
		transactionDTO.setTxnRefId(txnEntity.getTransactionRefId());
		transactionDTO.setTxnRefType(txnEntity.getTransactionRefType() == null ? null
				: TransactionRefType.valueOf(txnEntity.getTransactionRefType()));
		transactionDTO
				.setTxnStatus(txnEntity.getStatus() == null ? null : TransactionStatus.valueOf(txnEntity.getStatus()));
		transactionDTO.setTxnType(txnEntity.getTransactionType() == null ? null
				: TransactionType.valueOf(txnEntity.getTransactionType()));

		UserDTO createdBy = new UserDTO();
		createdBy.setName(txnEntity.getCreatedByName());
		createdBy.setProfileId(txnEntity.getCreatedById());
		createdBy.setEmail(txnEntity.getCreatedByEmail());

		transactionDTO.setCreatedBy(createdBy);

		UserDTO revertedBy = new UserDTO();
		revertedBy.setName(txnEntity.getRevertedByName());
		revertedBy.setProfileId(txnEntity.getRevertedById());
		revertedBy.setEmail(txnEntity.getRevertedByEmail());
		transactionDTO.setRevertedBy(revertedBy);
		return transactionDTO;
	}

	public static AccountDTO convertToAccountDTO(AccountEntity accountInfo, UserProfileEntity userEntity) {
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setAccountName(accountInfo.getAccountName());
		accountDTO.setAccountStatus(
				accountInfo.getAccountStatus() == null ? null : AccountStatus.valueOf(accountInfo.getAccountStatus()));
		accountDTO.setAccountType(
				accountInfo.getAccountType() == null ? null : AccountType.valueOf(accountInfo.getAccountType()));
		accountDTO.setActivatedOn(accountInfo.getActivatedOn());
		accountDTO.setCurrentBalance(accountInfo.getCurrentBalance());
		accountDTO.setId(accountInfo.getId());
		accountDTO.setOpeningBalance(accountInfo.getOpeningBalance());
		if (userEntity != null) {
			accountDTO.setProfile(convertToUserDTO(userEntity, null, null));
		} else {
			UserDTO userDTO = new UserDTO();
			userDTO.setUserId(accountInfo.getUserId());
			userDTO.setProfileId(accountInfo.getProfile());
			userDTO.setName(accountInfo.getAccountName());
			accountDTO.setProfile(userDTO);
		}

		BankDTO bankDTO = new BankDTO();
		bankDTO.setAccountHolderName(accountInfo.getBankAccountHolderName());
		bankDTO.setAccountNumber(accountInfo.getBankAccountNumber());
		bankDTO.setAccountType(accountInfo.getBankAccountType());
		bankDTO.setBankName(accountInfo.getBankName());
		bankDTO.setBranchName(accountInfo.getBankBranchName());
		bankDTO.setIFSCNumber(accountInfo.getBankIFSCNumber());
		accountDTO.setBankDetail(bankDTO);

		UpiDTO upiDTO = new UpiDTO();
		upiDTO.setMobileNumber(accountInfo.getUpiMobileNumber());
		upiDTO.setPayeeName(accountInfo.getUpiPayeeName());
		upiDTO.setUpiId(accountInfo.getUpiId());
		accountDTO.setUpiDetail(upiDTO);

		UserDTO createdBy = new UserDTO();
		createdBy.setName(accountInfo.getCreatedByName());
		createdBy.setProfileId(accountInfo.getCreatedById());
		createdBy.setEmail(accountInfo.getCreatedByEmail());
		accountDTO.setCreatedBy(createdBy);

		return accountDTO;
	}

	public static RequestDTO convertToWorkflowDTO(WorkflowEntity workflow, String secret) {
		RequestDTO workFlowDTO = new RequestDTO();
		workFlowDTO.setRefId(workflow.getRefId());
		workFlowDTO.setSystemGenerated(workflow.isSystemGenerated());
		workFlowDTO.setCreatedBy(workflow.getCreatedBy());
		workFlowDTO.setCreatedOn(workflow.getCreatedOn());
		workFlowDTO.setDelegated(workflow.isDelegated());
		UserDTO delegateDTO = new UserDTO();
		delegateDTO.setUserId(workflow.getDelegateUserId());
		delegateDTO.setProfileId(workflow.getDelegateProfileId());
		delegateDTO.setEmail(workflow.getDelegateProfileEmail());
		delegateDTO.setName(workflow.getDelegateProfileName());

		workFlowDTO.setDelegatedRequester(delegateDTO);
		workFlowDTO.setId(workflow.getId());
		workFlowDTO.setRemarks(workflow.getRemarks());
		UserDTO requesterDTO = new UserDTO();
		requesterDTO.setProfileId(workflow.getProfileId());
		requesterDTO.setUserId(workflow.getUserId());
		requesterDTO.setEmail(workflow.getProfileEmail());
		requesterDTO.setName(workflow.getProfileName());
		workFlowDTO.setRequester(requesterDTO);

		UserDTO systemReqDTO = new UserDTO();
		systemReqDTO.setUserId(workflow.getSystemRequestOwnerId());
		systemReqDTO.setEmail(workflow.getSystemRequestOwnerEmail());
		systemReqDTO.setName(workflow.getSystemRequestOwnerName());
		workFlowDTO.setSystemRequestOwner(systemReqDTO);

		workFlowDTO.setResolvedOn(workflow.getResolvedOn());
		workFlowDTO.setDescription(workflow.getDescription());
		workFlowDTO.setWorkflowName(workflow.getName());
		workFlowDTO.setStatus(workflow.getStatus() == null ? null : RequestStatus.valueOf(workflow.getStatus()));
		workFlowDTO.setLastStatus(
				workflow.getLastStatus() == null ? null : RequestStatus.valueOf(workflow.getLastStatus()));
		workFlowDTO.setType(RequestType.valueOf(workflow.getType()));
		if (workflow.getCustomFields() != null) {
			List<FieldDTO> list = workflow.getCustomFields().stream().map(m -> {
				return convertToFieldDTO(m, secret);
			}).toList();
			workFlowDTO.setAdditionalFields(list);
		}
		return workFlowDTO;
	}

	public static WorkDTO convertToWorkListDTO(WorkListEntity worklist) {
		WorkDTO workListDTO = new WorkDTO();
		workListDTO.setWorkSourceRefId(worklist.getSourceRefId());
		workListDTO.setActionPerformed(worklist.isActionPerformed());
		workListDTO.setCreatedOn(worklist.getCreatedOn());
		workListDTO.setCurrentAction(
				worklist.getCurrentAction() == null ? null : WorkAction.valueOf(worklist.getCurrentAction()));
		workListDTO.setDecision(worklist.getDecision() == null ? null : WorkDecision.valueOf(worklist.getDecision()));
		UserDTO dMaker = new UserDTO();
		dMaker.setProfileId(worklist.getDecisionMakerId());
		dMaker.setName(worklist.getDecisionMakerName());
		workListDTO.setDecisionMaker(dMaker);
		workListDTO.setDecisionMakerRoleGroup(worklist.getDecisionMakerRoleGroup());
		workListDTO.setDescription(worklist.getDescription());
		workListDTO.setGroupWork(worklist.isGroupWork());
		workListDTO.setId(worklist.getId());
		workListDTO.setPendingWithRoleGroups(InfraFieldHelper.stringToStringList(worklist.getPendingWithRoleGroups()));
		workListDTO.setPendingWithRoles(InfraFieldHelper.stringToStringList(worklist.getPendingWithRoles()).stream()
				.map(m -> RoleCode.valueOf(m)).collect(Collectors.toList()));

		List<UserDTO> pendingWithUsers = new ArrayList<>();
		List<String> pendingIds = InfraFieldHelper.stringToStringList(worklist.getPendingWithUserId());
		List<String> pendingNames = InfraFieldHelper.stringToStringList(worklist.getPendingWithUserName());

		for (int i = 0; i < pendingIds.size(); i++) {
			UserDTO pendingWithUser = new UserDTO();
			pendingWithUser.setUserId(pendingIds.get(i));
			pendingWithUser.setName(pendingNames.get(i));
			pendingWithUsers.add(pendingWithUser);
		}
		workListDTO.setPendingWithUsers(pendingWithUsers);

		workListDTO.setRemarks(worklist.getRemarks());
		workListDTO.setStepCompleted(worklist.getStepCompleted());
		workListDTO.setWorkSourceId(worklist.getSourceId());
		workListDTO.setWorkSourceStatus(
				worklist.getSourceStatus() == null ? null : RequestStatus.valueOf(worklist.getSourceStatus()));
		workListDTO.setWorkSourceType(
				worklist.getSourceType() == null ? null : RequestType.valueOf(worklist.getSourceType()));
		workListDTO.setDecisionDate(worklist.getDecisionDate());
		workListDTO.setWorkType(worklist.getWorkType() == null ? null : WorkType.valueOf(worklist.getWorkType()));
		workListDTO.setFinalStep(worklist.isFinalStep());
		if (worklist.getCustomFields() != null) {
			List<FieldDTO> list = worklist.getCustomFields().stream().map(m -> {
				return convertToFieldDTO(m, "");
			}).toList();
			workListDTO.setAdditionalFields(list);
		}
		return workListDTO;
	}

	public static ApiKeyDTO convertToApiKeyDTO(ApiKeyEntity apiKeyEntity) {
		ApiKeyDTO apiKeyDTO = new ApiKeyDTO();
		apiKeyDTO.setName(apiKeyEntity.getName());
		apiKeyDTO.setApiKey(apiKeyEntity.getApiKey());
		apiKeyDTO.setCreatedOn(apiKeyEntity.getCreatedOn());
		apiKeyDTO.setExpireable(apiKeyEntity.isExpireable());
		apiKeyDTO.setExpiryDate(apiKeyEntity.getExpireOn());
		apiKeyDTO.setId(apiKeyEntity.getId());
		apiKeyDTO.setScopes(InfraFieldHelper.stringToStringList(apiKeyEntity.getScopes()));
		apiKeyDTO.setStatus(ApiKeyStatus.valueOf(apiKeyEntity.getStatus()));
		return apiKeyDTO;
	}

	public static ExpenseDTO convertToExpenseDTO(ExpenseEntity expense) {
		ExpenseDTO expenseDTO = new ExpenseDTO();
		expenseDTO.setFinalized(expense.isFinalized());
		expenseDTO.setFinalizedOn(expense.getFinalizedOn());
		UserDTO finalizedBy = new UserDTO();
		finalizedBy.setProfileId(expense.getFinalizedById());
		finalizedBy.setUserId(expense.getFinalizedByUserId());
		finalizedBy.setName(expense.getFinalizedByName());
		expenseDTO.setFinalizedBy(finalizedBy);

		UserDTO createdBy = new UserDTO();
		createdBy.setProfileId(expense.getCreatedById());
		createdBy.setUserId(expense.getCreatedByUserId());
		createdBy.setName(expense.getCreatedByName());
		expenseDTO.setCreatedBy(createdBy);

		expenseDTO.setCreatedOn(expense.getExpenseCreatedOn());
		expenseDTO.setDescription(expense.getExpenseDescription());
		
		List<ExpenseItemDTO> expItems = new ArrayList<>();
		if (expense.getExpenses() != null) {
			for (ExpenseItemEntity exp : expense.getExpenses()) {
				expItems.add(convertToExpenseItemDTO(exp));
			}
		}
		expenseDTO.setExpenseItems(expItems);
		expenseDTO.setFinalAmount(expense.getExpenseAmount());
		expenseDTO.setId(expense.getId());
		expenseDTO.setName(expense.getExpenseTitle());
		expenseDTO.setRefId(expense.getExpenseRefId());
		expenseDTO.setRefType(
				expense.getExpenseRefType() == null ? null : ExpenseRefType.valueOf(expense.getExpenseRefType()));
		
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setId(expense.getExpenseAccountId());
		accountDTO.setAccountName(expense.getExpenseAccountName());
		expenseDTO.setAccount(accountDTO);
		
		expenseDTO.setTxnNumber(expense.getTransactionRefNumber());
		expenseDTO.setStatus(ExpenseStatus.valueOf(expense.getExpenseStatus()));
		expenseDTO.setExpenseDate(expense.getExpenseDate());

		return expenseDTO;
	}

	public static ExpenseItemDTO convertToExpenseItemDTO(ExpenseItemEntity expenseItem) {
		ExpenseItemDTO expenseItemDTO = new ExpenseItemDTO();
		expenseItemDTO.setId(expenseItem.getId());
		expenseItemDTO.setCreatedOn(expenseItem.getCreatedOn());
		expenseItemDTO.setAmount(expenseItem.getExpenseAmount());	
		expenseItemDTO.setDate(expenseItem.getExpenseDate());
		expenseItemDTO.setDescription(expenseItem.getExpenseDescription());
		expenseItemDTO.setStatus(ExpenseStatus.valueOf(expenseItem.getExpenseStatus()));
		expenseItemDTO.setItemName(expenseItem.getExpenseTitle());
		expenseItemDTO.setTxnNumber(expenseItem.getTransactionRefNumber());
		
		UserDTO confirmedBy = new UserDTO();
		confirmedBy.setProfileId(expenseItem.getPaymentConfirmedById());
		confirmedBy.setUserId(expenseItem.getPaymentConfirmedByName());
		confirmedBy.setName(expenseItem.getPaymentConfirmedByUserId());
		expenseItemDTO.setConfirmedBy(confirmedBy);
		
		UserDTO createdBy = new UserDTO();
		createdBy.setProfileId(expenseItem.getCreatedById());
		createdBy.setUserId(expenseItem.getCreatedByName());
		createdBy.setName(expenseItem.getCreatedByUserId());
		expenseItemDTO.setCreatedBy(createdBy);
		
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setId(expenseItem.getExpenseAccountId());
		accountDTO.setAccountName(expenseItem.getExpenseAccountName());
		expenseItemDTO.setAccount(accountDTO);
		

		return expenseItemDTO;
	}

}
