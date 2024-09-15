package ngo.nabarun.app.businesslogic.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ngo.nabarun.app.businesslogic.businessobjects.EventDetail;
import ngo.nabarun.app.businesslogic.businessobjects.MeetingDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserAddress;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserPhoneNumber;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserSocialMedia;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailUpdate;
import ngo.nabarun.app.infra.dto.AddressDTO;
import ngo.nabarun.app.infra.dto.EventDTO;
import ngo.nabarun.app.infra.dto.MeetingDTO;
import ngo.nabarun.app.infra.dto.PhoneDTO;
import ngo.nabarun.app.infra.dto.SocialMediaDTO;
import ngo.nabarun.app.infra.dto.UserAdditionalDetailsDTO;
import ngo.nabarun.app.infra.dto.UserDTO;

@Deprecated
public class BusinessObjectToDTOConverter {

	public static UserDTO toUserDTO(UserDetail userDetails) {
		UserDTO userDTO = new UserDTO();
		userDTO.setAbout(userDetails.getAbout());
		userDTO.setImageUrl(userDetails.getPicture());
		userDTO.setDateOfBirth(userDetails.getDateOfBirth());
		userDTO.setEmail(userDetails.getEmail());
		userDTO.setFirstName(userDetails.getFirstName());
		userDTO.setGender(userDetails.getGender());
		userDTO.setProfileId(userDetails.getId());
		userDTO.setLastName(userDetails.getLastName());
		userDTO.setMiddleName(userDetails.getMiddleName());
		userDTO.setStatus(userDetails.getStatus());
		userDTO.setTitle(userDetails.getTitle());
		userDTO.setUserId(userDetails.getUserId());
//		if (userDetails.getPhoneNumbers() != null) {
//			Optional<UserPhoneNumber> number = userDetails.getPhoneNumbers().stream().filter(f -> f.isPrimary())
//					.findFirst();
//			userDTO.setPhoneNumber(number == null || number.isEmpty() ? null
//					: number.get().getPhoneCode() + number.get().getPhoneNumber());
//		}
		UserAdditionalDetailsDTO addDTO = new UserAdditionalDetailsDTO();
		addDTO.setActiveContributor("Yes".equalsIgnoreCase(userDetails.getActiveContributor()));
		addDTO.setDisplayPublic(userDetails.isPublicProfile());
		userDTO.setAdditionalDetails(addDTO);
		userDTO.setAddresses(userDetails.getAddresses() == null ? List.of()
				: userDetails.getAddresses().stream().map(m -> toAddressDTO(m)).collect(Collectors.toList()));

		userDTO.setPhones(toPhoneDTO(userDetails.getPhoneNumbers()));
		userDTO.setSocialMedias(toSocialMediaDTO(userDetails.getSocialMediaLinks()));
		return userDTO;

	}

	public static UserDTO toUserDTO(UserDetailUpdate updatedUserDetails) {
		UserDTO updatedUserDTO = new UserDTO();
		updatedUserDTO.setTitle(updatedUserDetails.getTitle());
		updatedUserDTO.setFirstName(updatedUserDetails.getFirstName());
		updatedUserDTO.setMiddleName(updatedUserDetails.getMiddleName());
		updatedUserDTO.setLastName(updatedUserDetails.getLastName());
		updatedUserDTO.setGender(updatedUserDetails.getGender());
		updatedUserDTO.setDateOfBirth(updatedUserDetails.getDateOfBirth());
		updatedUserDTO.setAbout(updatedUserDetails.getAbout());

		updatedUserDTO.setAddresses(updatedUserDetails.getAddresses() == null ? List.of()
				: updatedUserDetails.getAddresses().stream().map(m -> toAddressDTO(m)).collect(Collectors.toList()));
		updatedUserDTO.setPhones(toPhoneDTO(updatedUserDetails.getPhoneNumbers()));
		updatedUserDTO.setSocialMedias(toSocialMediaDTO(updatedUserDetails.getSocialMediaLinks()));
		return updatedUserDTO;

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
		// address.setDelete(m.isDelete());
		return address;

	}

	public static List<PhoneDTO> toPhoneDTO(List<UserPhoneNumber> phoneNumbers) {
		return phoneNumbers == null ? List.of() : phoneNumbers.stream().map(m -> {
			PhoneDTO phone = new PhoneDTO();
			phone.setPhoneType(m.getPhoneType());
			phone.setPhoneCode(m.getPhoneCode());
			phone.setPhoneNumber(m.getPhoneNumber());
			phone.setId(m.getId());
			//phone.setDelete(m.isDelete());
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
			//sm.setDelete(m.isDelete());
			return sm;
		}).collect(Collectors.toList());
	}

	@Deprecated
	public static EventDTO toEventDTO1(EventDetail eventDetail) {
		EventDTO eventDTO = new EventDTO();
		eventDTO.setBudget(eventDetail.getEventBudget());
		eventDTO.setCoverPic(eventDetail.getCoverPicture());
		eventDTO.setCreatorId(eventDetail.getCreatorName());
		eventDTO.setDescription(eventDetail.getEventDescription());
		eventDTO.setDraft(eventDetail.isDraft());
		eventDTO.setEventDate(eventDetail.getEventDate());
		eventDTO.setId(eventDetail.getId());
		eventDTO.setLocation(eventDetail.getEventLocation());
		eventDTO.setTitle(eventDetail.getTitle());
		eventDTO.setType(eventDetail.getEventType());
		return eventDTO;
	}

//	@Deprecated
//	public static EventDTO toEventDTO(EventDetailUpdate updatedEventDetail) {
//		EventDTO eventDTO = new EventDTO();
//		eventDTO.setBudget(updatedEventDetail.getEventBudget());
//		eventDTO.setDescription(updatedEventDetail.getEventDescription());
//		if (updatedEventDetail.getPublish() != null) {
//			eventDTO.setDraft(!updatedEventDetail.getPublish());
//		}
//		eventDTO.setEventDate(updatedEventDetail.getEventDate());
//		eventDTO.setLocation(updatedEventDetail.getEventLocation());
//		eventDTO.setTitle(updatedEventDetail.getTitle());
//		eventDTO.setType(updatedEventDetail.getEventType());
//		return eventDTO;
//	}

//	@Deprecated
//	public static EventDTO toEventDTO(EventDetailCreate eventDetail) {
//		EventDTO eventDTO = new EventDTO();
//		eventDTO.setBudget(eventDetail.getEventBudget());
//		eventDTO.setDescription(eventDetail.getEventDescription());
//		eventDTO.setDraft(eventDetail.isDraft());
//		eventDTO.setEventDate(eventDetail.getEventDate());
//		eventDTO.setLocation(eventDetail.getEventLocation());
//		eventDTO.setTitle(eventDetail.getTitle());
//		eventDTO.setType(EventType.INTERNAL);
//		return eventDTO;
//	}

//	@Deprecated
//	public static NoticeDTO toNoticeDTO(NoticeDetailCreate noticeDetail) {
//		NoticeDTO noticeDTO = new NoticeDTO();
//		noticeDTO.setCreatorRole(noticeDetail.getCreatorRoleCode());
//		noticeDTO.setDescription(noticeDetail.getDescription());
//		//noticeDTO.setDraft(noticeDetail.getDraft());
//		noticeDTO.setNoticeDate(noticeDetail.getNoticeDate());
//		noticeDTO.setTitle(noticeDetail.getTitle());
//		// noticeDTO.setType(noticeEntity.getVisibility());
//		return noticeDTO;
//	}
//
//	@Deprecated
//	public static NoticeDTO toNoticeDTO(NoticeDetailUpdate noticeDetail) {
//		NoticeDTO noticeDTO = new NoticeDTO();
//		noticeDTO.setCreatorRole(noticeDetail.getCreatorRoleCode());
//		noticeDTO.setDescription(noticeDetail.getDescription());
//		if (noticeDetail.getPublish() != null && noticeDetail.getPublish() == Boolean.TRUE) {
//			//noticeDTO.setDraft(noticeDetail.getPublish());
//		}
//		noticeDTO.setNoticeDate(noticeDetail.getNoticeDate());
//		noticeDTO.setTitle(noticeDetail.getTitle());
//		// noticeDTO.setType(noticeEntity.getVisibility());
//		return noticeDTO;
//	}

	public static MeetingDTO toMeetingDTO(MeetingDetail meetingDetail) {
		MeetingDTO meetingDTO = new MeetingDTO();
		meetingDTO.setDescription(meetingDetail.getMeetingDescription());

		meetingDTO.setEndTime(meetingDetail.getMeetingEndTime());
		List<UserDTO> attendees = new ArrayList<>();
		if (meetingDetail.getMeetingAttendees() != null) {
			meetingDetail.getMeetingAttendees().forEach(user -> {
				attendees.add(toUserDTO(user));
			});
		}
		meetingDTO.setAttendees(attendees);
		//meetingDTO.setDefaultReminder(true);
		meetingDTO.setLocation(meetingDetail.getMeetingLocation());
		meetingDTO.setStartTime(meetingDetail.getMeetingStartTime());
		meetingDTO.setSummary(meetingDetail.getMeetingSummary());
		meetingDTO.setType(meetingDetail.getMeetingType());
		meetingDTO.setRefId(meetingDetail.getMeetingRefId());
		meetingDTO.setRefType(meetingDetail.getMeetingRefType());
		meetingDTO.setRemarks(meetingDetail.getMeetingRemarks());

//		List<DiscussionDTO> discussions = new ArrayList<>();
//		if (meetingDetail.getMeetingDiscussions() != null) {
//			meetingDetail.getMeetingDiscussions().forEach(disc -> {
//				discussions.add(new DiscussionDTO(disc.getId(), disc.getAgenda(), disc.getMinutes()));
//			});
//		}

		//meetingDTO.setDiscussions(discussions);
//		meetingDTO.setDraft(meetingDetail.getDraft() == null ? false : meetingDetail.getDraft());
//		meetingDTO.setAuthCode(meetingDetail.getAuthorization() == null ? null
//				: meetingDetail.getAuthorization().getAuthorizationCode());
//		meetingDTO.setAuthState(meetingDetail.getAuthorization() == null ? null
//				: meetingDetail.getAuthorization().getAuthorizationState());
//		meetingDTO.setAuthCallbackUrl(
//				meetingDetail.getAuthorization() == null ? null : meetingDetail.getAuthorization().getCallbackUrl());
		return meetingDTO;
	}

}
