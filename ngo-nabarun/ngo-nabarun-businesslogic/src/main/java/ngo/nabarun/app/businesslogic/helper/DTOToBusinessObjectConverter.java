package ngo.nabarun.app.businesslogic.helper;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import ngo.nabarun.app.businesslogic.businessobjects.UserAddress;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetail;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDiscussion;
import ngo.nabarun.app.businesslogic.businessobjects.NoticeDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserPhoneNumber;
import ngo.nabarun.app.businesslogic.businessobjects.UserSocialMedia;
import ngo.nabarun.app.common.enums.RoleCode;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserRole;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.DonationDTO;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.NoticeDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.RoleDTO;
import ngo.nabarun.app.infra.dto.SocialMediaDTO;
import ngo.nabarun.app.infra.dto.UserDTO;

public class DTOToBusinessObjectConverter {
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	public static UserDetail toUserDetail(UserDTO userDTO) {
		return toUserDetail(userDTO, null);
	}

	public static UserDetail toUserDetail(UserDTO userDTO, List<RoleDTO> roleDTO) {
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
		userDetails.setMemberSince(userDTO.getAdditionalDetails() != null
				? (userDTO.getAdditionalDetails().getCreatedOn() == null ? null :dateFormat.format(userDTO.getAdditionalDetails().getCreatedOn()))
				: null);
		userDetails.setMiddleName(userDTO.getMiddleName());
		userDetails.setPicture(userDTO.getImageUrl() != null ? userDTO.getImageUrl()
				: (userDetails.getInitials() == null ? null
						: "https://i0.wp.com/cdn.auth0.com/avatars/" + userDetails.getInitials().toLowerCase()
								+ ".png?ssl=1"));
		userDetails.setStatus(userDTO.getStatus());
		userDetails.setTitle(userDTO.getTitle());
		userDetails.setUserIds(userDTO.getUserIds());
		userDetails.setFullName((userDTO.getTitle() == null ? "" : userDTO.getTitle() + " ") + userDTO.getFirstName()
				+ " " + (userDTO.getMiddleName() == null ? "" : userDTO.getMiddleName() + " ") + userDTO.getLastName());
		if (roleDTO != null && !roleDTO.isEmpty()) {
			userDetails.setRoles(roleDTO
					.stream().map(m -> UserRole.builder().roleName(m.getName()).roleCode(m.getCode())
							.roleGroup(m.getGroup()).description(m.getDescription()).roleId(m.getId()).build())
					.toList());
		} else if (userDTO.getRoleNames() != null) {
			userDetails.setRoles(userDTO.getRoleNames().stream().map(m -> UserRole.builder().roleCode(RoleCode.valueOf(m)).build())
					.collect(Collectors.toList()));
		}
		userDetails.setPublicProfile(
				userDTO.getAdditionalDetails() != null ? userDTO.getAdditionalDetails().isDisplayPublic() : false);
		userDetails.setAddresses(toUserAddress(userDTO.getAddresses()));
		userDetails.setPhoneNumbers(toUserPhoneNumber(userDTO.getPhones()));
		userDetails.setSocialMediaLinks(toUserSocialMedia(userDTO.getSocialMedias()));
		return userDetails;

	}

	public static DonationDetail toDonationDetail(DonationDTO donationDTO, String attachment, EventDetail eventDetail) {
		DonationDetail donationDetail = new DonationDetail();
		donationDetail.setAccountId(donationDTO.getAccountId());
		donationDetail.setAmount(donationDTO.getAmount());
		donationDetail.setAttachments(null);
		donationDetail.setDonationStatus(donationDTO.getStatus());
		donationDetail.setDonationType(donationDTO.getType());
		donationDetail.setDonorDetails(toUserDetail(donationDTO.getDonor()));
		donationDetail.setEndDate(donationDTO.getEndDate());
		donationDetail.setEvent(eventDetail);
		donationDetail.setId(donationDTO.getId());
		donationDetail.setIsGuest(donationDTO.getGuest());
		donationDetail.setPaidOn(donationDTO.getPaidOn());
		donationDetail.setPaymentConfirmedBy(donationDTO.getConfirmedBy());
		donationDetail.setPaymentConfirmedOn(donationDTO.getConfirmedOn());
		donationDetail.setPaymentMethod(donationDTO.getPaymentMethod());
		donationDetail.setRaisedOn(donationDTO.getRaisedOn());
		donationDetail.setStartDate(donationDTO.getStartDate());
		return donationDetail;

	}

	public static List<UserAddress> toUserAddress(List<AddressDTO> addressDTO) {
		return addressDTO == null ? List.of() : addressDTO.stream().map(m -> {
			UserAddress address = new UserAddress();
			address.setAddressType(m.getAddressType());
			address.setAddressLine(m.getAddressLine());
			address.setHometown(m.getHometown());
			address.setDistrict(m.getDistrict());
			address.setState(m.getState());
			address.setCountry(m.getCountry());
			address.setId(m.getId());
			address.setDelete(m.isDelete());
			return address;
		}).collect(Collectors.toList());

	}

	public static List<UserPhoneNumber> toUserPhoneNumber(List<PhoneDTO> phoneDTO) {
		return phoneDTO == null ? List.of() : phoneDTO.stream().map(m -> {
			UserPhoneNumber phone = new UserPhoneNumber();
			phone.setPhoneType(m.getPhoneType());
			phone.setPhoneCode(m.getPhoneCode());
			phone.setPhoneNumber(m.getPhoneNumber());
			phone.setDisplayNumber(m.getPhoneCode() + " " + m.getPhoneNumber() + " (" + phone.getPhoneType() + ")");
			phone.setId(m.getId());
			phone.setDelete(m.isDelete());
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
			sm.setDelete(m.isDelete());
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
		NoticeDetail noticeDetail= new NoticeDetail();
		
		noticeDetail.setCreatorName(noticeDTO.getCreatedBy());
		noticeDetail.setCreatorRole(noticeDTO.getCreatorRole());
		noticeDetail.setCreatorRoleCode(noticeDTO.getCreatorRole());
		noticeDetail.setDescription(noticeDTO.getDescription());
		noticeDetail.setId(noticeDTO.getId());
	//	noticeDetail.setMeeting(noticeDTO.getMeeting());
		noticeDetail.setNoticeDate(noticeDTO.getNoticeDate());
		noticeDetail.setNoticeNumber(noticeDTO.getNoticeNumber());
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
		meetingDetail.setMeetingAttendees(meetingDTO.getAttendees() == null ? List.of(): meetingDTO.getAttendees().stream().map(m->toUserDetail(m)).toList());
		meetingDetail.setMeetingDescription(meetingDTO.getDescription());
		meetingDetail.setMeetingDiscussions(meetingDTO.getDiscussions() == null ? List.of(): meetingDTO.getDiscussions().stream().map(m->new MeetingDiscussion(m.getId(),m.getAgenda(),m.getMinutes())).toList());
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

}
