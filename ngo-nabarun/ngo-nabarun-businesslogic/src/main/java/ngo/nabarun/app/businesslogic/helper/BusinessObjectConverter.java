package ngo.nabarun.app.businesslogic.helper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import ngo.nabarun.app.businesslogic.businessobjects.UserAddress;
import ngo.nabarun.app.businesslogic.businessobjects.AccountDetail;
import ngo.nabarun.app.businesslogic.businessobjects.AdditionalField;
import ngo.nabarun.app.businesslogic.businessobjects.BankDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetail;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDiscussion;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.TransactionDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UPIDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserPhoneNumber;
import ngo.nabarun.app.businesslogic.businessobjects.UserSocialMedia;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.common.enums.TransactionType;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserRole;
import ngo.nabarun.app.infra.dto.AccountDTO;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.BankDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.FieldDTO;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.SocialMediaDTO;
import ngo.nabarun.app.infra.dto.TransactionDTO;
import ngo.nabarun.app.infra.dto.UpiDTO;
import ngo.nabarun.app.infra.dto.UserDTO;
import ngo.nabarun.app.infra.dto.WorkFlowDTO;
import ngo.nabarun.app.infra.dto.WorkListDTO;
import ngo.nabarun.app.infra.misc.ConfigTemplate.KeyValuePair;

public class BusinessObjectConverter {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public static UserDetail toPublicUserDetail(UserDTO userDTO) {
		UserDetail userDetails = new UserDetail();
		userDetails.setEmail(userDTO.getEmail());
		userDetails.setPicture(userDTO.getImageUrl() != null ? userDTO.getImageUrl()
				: (userDetails.getInitials() == null ? null
						: "https://i0.wp.com/cdn.auth0.com/avatars/" + userDetails.getInitials().toLowerCase()
								+ ".png?ssl=1"));
		String title = userDTO.getTitle() == null ? "" : userDTO.getTitle() + " ";
		if (userDTO.getName() != null) {
			userDetails.setFullName(title + userDTO.getName());
		} else {
			String firstName = userDTO.getFirstName() == null ? "" : userDTO.getFirstName() + " ";
			String middleName = userDTO.getMiddleName() == null ? "" : userDTO.getMiddleName() + " ";
			String lastName = userDTO.getLastName() == null ? "" : userDTO.getLastName();
			userDetails.setFullName(title + firstName + middleName + lastName);
		}

		if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
			userDetails.setRoles(userDTO
					.getRoles().stream().map(m -> UserRole.builder().roleName(m.getName()).roleCode(m.getCode())
							.description(m.getDescription()).roleId(m.getId()).build())
					.toList());
			userDetails.setRoleString(String.join(", ", userDTO.getRoles().stream().map(m->m.getName()).toList()));	
		}
		userDetails.setSocialMediaLinks(toUserSocialMedia(userDTO.getSocialMedias()));
		return userDetails;

	}

	public static UserDetail toUserDetail(UserDTO userDTO) {
		UserDetail userDetails = new UserDetail();
		userDetails.setAbout(userDTO.getAbout());
		userDetails.setActiveContributor(userDTO.getAdditionalDetails() != null
				? (userDTO.getAdditionalDetails().isActiveContributor() ? "Yes" : "No")
				: null);
		userDetails.setDateOfBirth(userDTO.getDateOfBirth());
		userDetails.setEmail(userDTO.getEmail());
		userDetails.setFirstName(userDTO.getFirstName());
		userDetails.setGender(userDTO.getGender());
		userDetails.setId(userDTO.getProfileId());
		userDetails.setInitials((userDTO.getFirstName() == null || userDTO.getLastName() == null) ? null
				: (userDTO.getFirstName().substring(0, 1) + userDTO.getLastName().substring(0, 1)).toUpperCase());
		userDetails.setLastName(userDTO.getLastName());
		userDetails.setMemberSince(
				userDTO.getAdditionalDetails() != null
						? (userDTO.getAdditionalDetails().getCreatedOn() == null ? null
								: dateFormat.format(userDTO.getAdditionalDetails().getCreatedOn()))
						: null);
		userDetails.setMiddleName(userDTO.getMiddleName());
		userDetails.setPicture(userDTO.getImageUrl() != null ? userDTO.getImageUrl()
				: (userDetails.getInitials() == null ? null
						: "https://i0.wp.com/cdn.auth0.com/avatars/" + userDetails.getInitials().toLowerCase()
								+ ".png?ssl=1"));
		userDetails.setStatus(userDTO.getStatus());
		userDetails.setTitle(userDTO.getTitle());
		userDetails.setUserId(userDTO.getUserId());
		String title = userDTO.getTitle() == null ? "" : userDTO.getTitle() + " ";
		if (userDTO.getName() != null) {
			userDetails.setFullName(title + userDTO.getName());
		} else {
			String firstName = userDTO.getFirstName() == null ? "" : userDTO.getFirstName() + " ";
			String middleName = userDTO.getMiddleName() == null ? "" : userDTO.getMiddleName() + " ";
			String lastName = userDTO.getLastName() == null ? "" : userDTO.getLastName();
			userDetails.setFullName(title + firstName + middleName + lastName);
		}
		/*
		 * Role management
		 */

		if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
			userDetails.setRoles(userDTO
					.getRoles().stream().map(m -> UserRole.builder().roleName(m.getName()).roleCode(m.getCode())
							.description(m.getDescription()).roleId(m.getId()).build())
					.toList());
			userDetails.setRoleString(String.join(", ", userDTO.getRoles().stream().map(m->m.getName()).toList()));	
		}
		
		userDetails.setPublicProfile(
				userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().isDisplayPublic() : false);

		userDetails.setAddresses(userDTO.getAddresses() == null ? List.of()
				: userDTO.getAddresses().stream().map(m -> toUserAddress(m)).collect(Collectors.toList()));
		userDetails.setPhoneNumbers(toUserPhoneNumber(userDTO.getPhones()));
		userDetails.setSocialMediaLinks(toUserSocialMedia(userDTO.getSocialMedias()));
		userDetails.setPrimaryNumber(userDTO.getPhoneNumber());
		return userDetails;

	}



	public static DonationDetail toDonationDetail(DonationDTO donationDTO) {
		return toDonationDetail(donationDTO, null, null);
	}

	public static DonationDetail toDonationDetail(DonationDTO donationDTO, String attachment, EventDetail eventDetail) {
		DonationDetail donationDetail = new DonationDetail();
		donationDetail.setAmount(donationDTO.getAmount());
		donationDetail.setDonationStatus(donationDTO.getStatus());
		donationDetail.setDonationType(donationDTO.getType());
		donationDetail.setDonorDetails(toUserDetail(donationDTO.getDonor()));
		donationDetail.setEndDate(donationDTO.getEndDate());
		donationDetail.setEvent(eventDetail);
		donationDetail.setId(donationDTO.getId());
		donationDetail.setIsGuest(donationDTO.getGuest());
		donationDetail.setPaidOn(donationDTO.getPaidOn());
		donationDetail.setPaymentConfirmedBy(toUserDetail(donationDTO.getConfirmedBy()));
		donationDetail.setPaymentConfirmedOn(donationDTO.getConfirmedOn());
		donationDetail.setPaymentMethod(donationDTO.getPaymentMethod());
		donationDetail.setRaisedOn(donationDTO.getRaisedOn());
		donationDetail.setStartDate(donationDTO.getStartDate());

		donationDetail.setTxnRef(donationDTO.getTransactionRefNumber());
		donationDetail.setPaidUsingUPI(donationDTO.getUpiName());
		donationDetail.setPaymentNotified(
				donationDTO.getIsPaymentNotified() == null ? false : donationDTO.getIsPaymentNotified());

		donationDetail.setReceivedAccount(toAccountDetail(donationDTO.getPaidToAccount()));
		donationDetail.setRemarks(donationDTO.getComment());
		donationDetail.setCancelletionReason(donationDTO.getCancelReason());
		donationDetail.setLaterPaymentReason(donationDTO.getPayLaterReason());
		donationDetail.setPaymentFailureDetail(donationDTO.getPaymentFailDetail());

		donationDetail.setAdditionalFields(donationDTO.getAdditionalFields() == null ? List.of()
				: donationDTO.getAdditionalFields().stream().filter(f->!f.isHidden()).map(BusinessObjectConverter::toAdditionalField)
						.collect(Collectors.toList()));
		return donationDetail;
	}
	
	public static RequestDetail toRequestDetail(WorkFlowDTO workflow) {
		RequestDetail request= new RequestDetail();
		request.setAdditionalFields(workflow.getAdditionalFields() == null ? List.of() : workflow.getAdditionalFields().stream().filter(f->!f.isHidden()).map(m->toAdditionalField(m)).toList());		
		request.setCreatedOn(workflow.getCreatedOn());
		request.setDelegated(workflow.isDelegated());
		request.setDelegatedRequester(toUserDetail(workflow.getDelegatedRequester()));
		request.setDescription(workflow.getWorkflowDescription());
		request.setId(workflow.getId());
		request.setName(workflow.getWorkflowName());
		request.setRemarks(workflow.getRemarks());
		request.setRequester(toUserDetail(workflow.getRequester()));
		request.setResolvedOn(workflow.getResolvedOn());
		request.setStatus(workflow.getWorkflowStatus() );
		request.setType(workflow.getWorkflowType() );
		return request;
	}

	public static AdditionalField toAdditionalField(FieldDTO fieldDTO) {
		
		AdditionalField additionalField = new AdditionalField(null, null, fieldDTO.isHidden());
		additionalField.setId(fieldDTO.getFieldId());
		additionalField.setKey(fieldDTO.getFieldKey());
		additionalField.setName(fieldDTO.getFieldName());
		additionalField.setType(fieldDTO.getFieldType());
		additionalField.setValue(fieldDTO.isHidden() ? null :fieldDTO.getFieldValue());
		return additionalField;
	}

	public static UserAddress toUserAddress(AddressDTO m) {
		UserAddress address = new UserAddress();
		address.setAddressType(m.getAddressType());
		address.setAddressLine1(m.getAddressLine1());
		address.setAddressLine2(m.getAddressLine2());
		address.setAddressLine3(m.getAddressLine3());
		address.setHometown(m.getHometown());
		address.setDistrict(m.getDistrict());
		address.setState(m.getState());
		address.setCountry(m.getCountry());
		address.setId(m.getId());
		// address.setDelete(m.isDelete());
		return address;

	}

	public static List<UserPhoneNumber> toUserPhoneNumber(List<PhoneDTO> phoneDTO) {
		return phoneDTO == null ? List.of() : phoneDTO.stream().map(m -> {
			UserPhoneNumber phone = new UserPhoneNumber();
			phone.setPhoneType(m.getPhoneType());
			phone.setPhoneCode(m.getPhoneCode());
			phone.setPhoneNumber(m.getPhoneNumber());
			phone.setDisplayNumber(m.getPhoneCode() + " " + m.getPhoneNumber() + " (" + phone.getPhoneType() + ")");
			phone.setId(m.getId());
			// phone.setDelete(m.isDelete());
			return phone;
		}).collect(Collectors.toList());
	}

	public static List<UserSocialMedia> toUserSocialMedia(List<SocialMediaDTO> socialMedia) {
		return socialMedia == null ? List.of() : socialMedia.stream().map(m -> {
			UserSocialMedia sm = new UserSocialMedia();
			sm.setMediaIcon(null);
			sm.setMediaLink(m.getSocialMediaURL());
			sm.setMediaName(m.getSocialMediaName());
			sm.setMediaType(m.getSocialMediaType());
			sm.setId(m.getId());
			// sm.setDelete(m.isDelete());
			return sm;
		}).collect(Collectors.toList());
	}

	public static EventDetail toEventDetail(EventDTO eventDTO) {
		EventDetail eventDetail = new EventDetail();
		eventDetail.setCoverPicture(eventDTO.getCoverPic());
		eventDetail.setCreatorName(eventDTO.getCreatorId());
		eventDetail.setDraft(eventDTO.isDraft());
		eventDetail.setEventBudget(eventDTO.getBudget());
		eventDetail.setEventDate(eventDTO.getEventDate());
		eventDetail.setEventDescription(eventDTO.getDescription());
		eventDetail.setEventLocation(eventDTO.getLocation());
		eventDetail.setEventType(eventDTO.getType());
		eventDetail.setId(eventDTO.getId());
		eventDetail.setTitle(eventDTO.getTitle());
		eventDetail.setTotalExpenditure(null);
		return eventDetail;
	}

	public static NoticeDetail toNoticeDetail(NoticeDTO noticeDTO) {
		NoticeDetail noticeDetail = new NoticeDetail();

		noticeDetail.setCreatorName(noticeDTO.getCreatedBy());
		noticeDetail.setCreatorRole(noticeDTO.getCreatorRole());
		noticeDetail.setCreatorRoleCode(noticeDTO.getCreatorRole());
		noticeDetail.setDescription(noticeDTO.getDescription());
		noticeDetail.setId(noticeDTO.getId());
		// noticeDetail.setMeeting(noticeDTO.getMeeting());
		noticeDetail.setNoticeDate(noticeDTO.getNoticeDate());
		// noticeDetail.setNoticeNumber(noticeDTO.getNoticeNumber());
		noticeDetail.setPublishDate(noticeDTO.getPublishDate());
		noticeDetail.setTitle(noticeDTO.getTitle());
		return noticeDetail;
	}

	public static MeetingDetail toMeetingDetail(MeetingDTO meetingDTO) {
		MeetingDetail meetingDetail = new MeetingDetail();
		meetingDetail.setExtAudioConferenceLink(meetingDTO.getAudioMeetingLink());
		meetingDetail.setExtMeetingId(meetingDTO.getExtMeetingId());
		meetingDetail.setExtVideoConferenceLink(meetingDTO.getVideoMeetingLink());
		meetingDetail.setId(meetingDTO.getId());
		meetingDetail.setMeetingAttendees(meetingDTO.getAttendees() == null ? List.of()
				: meetingDTO.getAttendees().stream().map(m -> toUserDetail(m)).toList());
		meetingDetail.setMeetingDescription(meetingDTO.getDescription());
		meetingDetail.setMeetingDiscussions(meetingDTO.getDiscussions() == null ? List.of()
				: meetingDTO.getDiscussions().stream()
						.map(m -> new MeetingDiscussion(m.getId(), m.getAgenda(), m.getMinutes())).toList());
		meetingDetail.setMeetingEndTime(meetingDTO.getEndTime());
		meetingDetail.setMeetingLocation(meetingDTO.getLocation());
		meetingDetail.setMeetingRefId(meetingDTO.getRefId());
		meetingDetail.setMeetingRefType(meetingDTO.getRefType());
		meetingDetail.setMeetingRemarks(meetingDTO.getRemarks());
		meetingDetail.setMeetingStartTime(meetingDTO.getStartTime());
		meetingDetail.setMeetingStatus(meetingDTO.getStatus());
		meetingDetail.setMeetingSummary(meetingDTO.getSummary());
		meetingDetail.setMeetingType(meetingDTO.getType());

		meetingDetail.setExtHtmlLink(meetingDTO.getHtmlLink());
//		if(meetingDTO.getAuthUrl() != null) {
//			MeetingAuthorization meetAuth = new MeetingAuthorization();
//			meetAuth.setAuthorizationUrl(meetingDTO.getAuthUrl());	
//			meetAuth.setNeedAuthorization(meetingDTO.isNeedAuthorization());
//			meetingDetail.setAuthorization(meetAuth);
//		}

		return meetingDetail;
	}

	public static List<KeyValue> toKeyValueList(List<KeyValuePair> keyValPair) {
		return keyValPair == null ? List.of() : keyValPair.stream().map(m -> {
			KeyValue kv = new KeyValue();
			kv.setKey(m.getKey());
			kv.setValue(m.getValue());
			return kv;
		}).toList();
	}

	public static AccountDetail toAccountDetail(AccountDTO accountDTO) {
		AccountDetail accountDetail = new AccountDetail();
		if (accountDTO.getProfile() != null) {
			accountDetail.setAccountHolder(toUserDetail(accountDTO.getProfile()));
		}
		accountDetail.setAccountHolderName(accountDTO.getAccountName());
		accountDetail.setAccountStatus(accountDTO.getAccountStatus());
		accountDetail.setAccountType(accountDTO.getAccountType());
		accountDetail.setActivatedOn(accountDTO.getActivatedOn());
		if (accountDTO.getBankDetail() != null) {
			accountDetail.setBankDetail(toBankDetail(accountDTO.getBankDetail()));
		}
		accountDetail.setCurrentBalance(accountDTO.getCurrentBalance());
		accountDetail.setId(accountDTO.getId());
		if (accountDTO.getUpiDetail() != null) {
			accountDetail.setUpiDetail(toUPIDetail(accountDTO.getUpiDetail()));
		}
		return accountDetail;
	}

	public static BankDetail toBankDetail(BankDTO bankDTO) {
		BankDetail bankDetail = new BankDetail();
		bankDetail.setBankAccountHolderName(bankDTO.getAccountHolderName());
		bankDetail.setBankAccountNumber(bankDTO.getAccountNumber());
		bankDetail.setBankAccountType(bankDTO.getAccountType());
		bankDetail.setBankBranch(bankDTO.getBranchName());
		bankDetail.setBankName(bankDTO.getBankName());
		bankDetail.setIFSCNumber(bankDTO.getIFSCNumber());
		return bankDetail;
	}

	public static UPIDetail toUPIDetail(UpiDTO upiDTO) {
		UPIDetail upiDetail = new UPIDetail();
		upiDetail.setMobileNumber(upiDTO.getMobileNumber());
		upiDetail.setPayeeName(upiDTO.getPayeeName());
		upiDetail.setUpiId(upiDTO.getUpiId());
		upiDetail.setQrData(CommonUtils.getUPIURI(upiDTO.getUpiId(), upiDTO.getPayeeName(), null, null, null, null));
		return upiDetail;
	}

	public static TransactionDetail toTransactionDetail(TransactionDTO transactionDTO, boolean includeFull) {
		TransactionDetail txnDetail = new TransactionDetail();

		txnDetail.setTxnAmount(transactionDTO.getTxnAmount());
		txnDetail.setTxnDate(transactionDTO.getTxnDate());
		txnDetail.setTxnDescription(transactionDTO.getTxnDescription());
		txnDetail.setTxnParticulars(null);
		txnDetail.setTxnId(transactionDTO.getId());

		txnDetail.setTxnStatus(transactionDTO.getTxnStatus());
		txnDetail.setTxnType(transactionDTO.getTxnType());

		if (transactionDTO.getTxnType() == TransactionType.IN) {
			txnDetail.setAccBalance(transactionDTO.getToAccBalAfterTxn());
		} else if (transactionDTO.getTxnType() == TransactionType.OUT) {
			txnDetail.setAccBalance(transactionDTO.getFromAccBalAfterTxn());
		} else {
			Double balance = transactionDTO.getId().equals(transactionDTO.getToAccount().getId())
					? transactionDTO.getToAccBalAfterTxn()
					: transactionDTO.getFromAccBalAfterTxn();
			txnDetail.setAccBalance(balance);
		}

		if (includeFull) {
			txnDetail.setComment(transactionDTO.getComment());
			txnDetail.setTransferFrom(toAccountDetail(transactionDTO.getFromAccount()));
			txnDetail.setTransferTo(toAccountDetail(transactionDTO.getFromAccount()));
			txnDetail.setTxnRefId(transactionDTO.getTxnRefId());
			txnDetail.setTxnRefType(transactionDTO.getTxnRefType());
		}

		return txnDetail;
	}
	
	public static WorkDetail toWorkItem(WorkListDTO workitemDTO) {
		WorkDetail wiDetail = new WorkDetail();
		wiDetail.setCreatedOn(workitemDTO.getCreatedOn());
		wiDetail.setDecision(workitemDTO.getDecision());
		wiDetail.setDecisionDate(workitemDTO.getDecisionDate());
		wiDetail.setDescription(workitemDTO.getDescription());
		wiDetail.setId(workitemDTO.getId());
		
		wiDetail.setRemarks(workitemDTO.getRemarks());
		wiDetail.setStepCompleted(workitemDTO.getStepCompleted());
		wiDetail.setWorkflowId(workitemDTO.getWorkflowId());
		wiDetail.setWorkflowStatus(workitemDTO.getWorkflowStatus());
		if(workitemDTO.getStepCompleted() != null && workitemDTO.getStepCompleted()) {
			wiDetail.setDecisionOwner(toUserDetail(workitemDTO.getDecisionMaker()));
		}else {
			wiDetail.setPendingWithRoles(workitemDTO.getPendingWithRoles());
		}
		
		wiDetail.setWorkType(workitemDTO.getWorkType());
		return wiDetail;
	}
	
	
	
	/**
	 * User details to DTO
	 */
	
	public static UserDTO toUserDTO(UserDetail requester) {
		UserDTO requesterDTO = new UserDTO();
		requesterDTO.setProfileId(requester == null ? null : requester.getId());
		requesterDTO.setFirstName(requester == null ? null : requester.getFirstName());
		requesterDTO.setLastName(requester == null ? null : requester.getLastName());
		requesterDTO.setName(requester == null ? null : requester.getFullName());
		requesterDTO.setEmail(requester == null ? null : requester.getEmail());
		return requesterDTO;
	}
	
	public static AddressDTO toAddressDTO(UserAddress m) {
		AddressDTO address = new AddressDTO();
		address.setAddressType(m.getAddressType());
		address.setAddressLine1(m.getAddressLine1());
		address.setAddressLine2(m.getAddressLine2());
		address.setAddressLine3(m.getAddressLine3());
		address.setHometown(m.getHometown());
		address.setDistrict(m.getDistrict());
		address.setState(m.getState());
		address.setCountry(m.getCountry());
		address.setId(m.getId());
		return address;

	}
	
	

	public static List<PhoneDTO> toPhoneDTO(List<UserPhoneNumber> phoneNumbers) {
		return phoneNumbers == null ? List.of() : phoneNumbers.stream().map(m -> {
			PhoneDTO phone = new PhoneDTO();
			phone.setPhoneType(m.getPhoneType());
			phone.setPhoneCode(m.getPhoneCode());
			phone.setPhoneNumber(m.getPhoneNumber());
			phone.setId(m.getId());
			return phone;
		}).collect(Collectors.toList());
	}

	public static List<SocialMediaDTO> toSocialMediaDTO(List<UserSocialMedia> socialMedia) {
		return socialMedia == null ? List.of() : socialMedia.stream().map(m -> {
			SocialMediaDTO sm = new SocialMediaDTO();
			sm.setSocialMediaURL(m.getMediaLink());
			sm.setSocialMediaName(m.getMediaName());
			sm.setSocialMediaType(m.getMediaType());
			sm.setId(m.getId());
			return sm;
		}).collect(Collectors.toList());
	}
}
