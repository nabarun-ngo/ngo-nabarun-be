package ngo.nabarun.domain.user.vo;

import ngo.nabarun.domain.user.model.PhoneNumber;

// Domain value objects (parameter objects)
public record RegistrationDetail(
    String firstName,
    String lastName,
    String email,
    PhoneNumber number
) {}

