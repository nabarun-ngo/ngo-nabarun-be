package ngo.nabarun.domain.user.vo;

import java.util.List;

import ngo.nabarun.domain.user.enums.LoginMethod;
import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.model.Role;

public record AdminUpdate(
    List<Role> roles,
    Boolean isActiveDonor,
    String userId,
    UserStatus status,
    List<LoginMethod> loginMethod
) {}