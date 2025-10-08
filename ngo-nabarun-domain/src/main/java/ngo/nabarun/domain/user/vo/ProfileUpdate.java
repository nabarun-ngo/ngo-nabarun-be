package ngo.nabarun.domain.user.vo;

import java.util.List;

import ngo.nabarun.domain.user.model.Address;
import ngo.nabarun.domain.user.model.Link;
import ngo.nabarun.domain.user.model.PhoneNumber;

public record ProfileUpdate(
    String title,
    String firstName,
    String middleName,
    String lastName,
    java.util.Date dateOfBirth,
    String gender,
    String about,
    String picture,
    PhoneNumber primaryNumber,
    PhoneNumber secondaryNumber,
    Address presentAddress,
    Address permanentAddress,
    List<Link> socialMediaLinks,
    Boolean isPublicProfile,
    Boolean isAddressSame
) {}
