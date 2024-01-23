package ngo.nabarun.app.api.converter;

import java.util.List;

import ngo.nabarun.app.businesslogic.businessobjects.DonationDetail;
import ngo.nabarun.app.businesslogic.businessobjects.DonationDetailCreate;
import ngo.nabarun.app.businesslogic.businessobjects.UserPhoneNumber;
import ngo.nabarun.app.common.enums.PhoneType;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetailUpdate;

public class RequestConverter {
	public static UserDetail convertToUserDetails(UserDetailUpdate request) {
		UserDetail uObj=new UserDetail();
		uObj.setTitle(request.getTitle());
		uObj.setFirstName(request.getFirstName());
		uObj.setMiddleName(request.getMiddleName());
		uObj.setLastName(request.getLastName());
		uObj.setGender(request.getGender());
		uObj.setDateOfBirth(request.getDateOfBirth());
		uObj.setPicture(request.getBase64Image());
		uObj.setAbout(request.getAbout());
		//uObj.setAddresses(request.getAddresses());
		uObj.setSocialMediaLinks(request.getSocialMediaLinks());
		uObj.setPhoneNumbers(request.getPhoneNumbers());;
		return uObj;
	}
	
	
	public static DonationDetail convertToDonationDetails(DonationDetailCreate request) {
		DonationDetail donationDetail=new DonationDetail();
		donationDetail.setAmount(request.getAmount());
		donationDetail.setDonationType(request.getDonationType());
		donationDetail.setIsGuest(request.getIsGuest());
		donationDetail.setStartDate(request.getStartDate());
		donationDetail.setEndDate(request.getEndDate());
	
		UserDetail userDetail=new UserDetail();
		userDetail.setId(request.getDonorId());
		userDetail.setFirstName(request.getDonorName());
		userDetail.setLastName("");
		userDetail.setEmail(request.getDonorEmail());
		
		UserPhoneNumber phoneNumber=new UserPhoneNumber();
		//phoneNumber.setPhoneType(PhoneType.MOBILE);
		phoneNumber.setPhoneNumber(request.getDonorMobile());
		
		userDetail.setPhoneNumbers(List.of(phoneNumber));
		donationDetail.setDonorDetails(userDetail);
		
		return donationDetail;
	}
}
