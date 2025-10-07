package ngo.nabarun.infra.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ngo.nabarun.domain.user.enums.LoginMethod;
import ngo.nabarun.domain.user.model.PhoneNumber;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.infra.mongo.entity.UserEntity;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface InfraUserMapper {

    // ----------------------
    // Domain -> Entity
    // ----------------------
    @Mapping(source = "picture", target = "avatarUrl")
    @Mapping(source = "primaryNumber", target = "phoneNumber", qualifiedByName = "mapPhoneNumber")
    @Mapping(source = "secondaryNumber", target = "altPhoneNumber", qualifiedByName = "mapAltPhoneNumber")
    @Mapping(source = "presentAddress.addressLine1", target = "addressLine1")
    @Mapping(source = "presentAddress.addressLine2", target = "addressLine2")
    @Mapping(source = "presentAddress.addressLine3", target = "addressLine3")
    @Mapping(source = "presentAddress.hometown", target = "hometown")
    @Mapping(source = "presentAddress.zipCode", target = "zipCode")
    @Mapping(source = "presentAddress.district", target = "district")
    @Mapping(source = "presentAddress.state", target = "state")
    @Mapping(source = "presentAddress.country", target = "country")
    @Mapping(source = "permanentAddress.addressLine1", target = "permanentAddressLine1")
    @Mapping(source = "permanentAddress.addressLine2", target = "permanentAddressLine2")
    @Mapping(source = "permanentAddress.addressLine3", target = "permanentAddressLine3")
    @Mapping(source = "permanentAddress.hometown", target = "permanentHometown")
    @Mapping(source = "permanentAddress.zipCode", target = "permanentZipCode")
    @Mapping(source = "permanentAddress.district", target = "permanentDistrict")
    @Mapping(source = "permanentAddress.state", target = "permanentState")
    @Mapping(source = "permanentAddress.country", target = "permanentCountry")
    @Mapping(source = "addressSame", target = "presentPermanentSame")
    @Mapping(source = "activeDonor", target = "activeContributor")
    @Mapping(source = "loginMethod", target = "loginMethods", qualifiedByName = "mapLoginMethods")
    UserEntity toEntity(User user);

    // ----------------------
    // Entity -> Domain
    // ----------------------
    @Mapping(target = "picture", source = "avatarUrl")
    @Mapping(target = "primaryNumber", source = "phoneNumber", qualifiedByName = "mapPhoneNumberToDomain")
    @Mapping(target = "secondaryNumber", source = "altPhoneNumber", qualifiedByName = "mapAltPhoneNumberToDomain")
    @Mapping(target = "presentAddress.addressLine1", source = "addressLine1")
    @Mapping(target = "presentAddress.addressLine2", source = "addressLine2")
    @Mapping(target = "presentAddress.addressLine3", source = "addressLine3")
    @Mapping(target = "presentAddress.hometown", source = "hometown")
    @Mapping(target = "presentAddress.zipCode", source = "zipCode")
    @Mapping(target = "presentAddress.district", source = "district")
    @Mapping(target = "presentAddress.state", source = "state")
    @Mapping(target = "presentAddress.country", source = "country")
    @Mapping(target = "permanentAddress.addressLine1", source = "permanentAddressLine1")
    @Mapping(target = "permanentAddress.addressLine2", source = "permanentAddressLine2")
    @Mapping(target = "permanentAddress.addressLine3", source = "permanentAddressLine3")
    @Mapping(target = "permanentAddress.hometown", source = "permanentHometown")
    @Mapping(target = "permanentAddress.zipCode", source = "permanentZipCode")
    @Mapping(target = "permanentAddress.district", source = "permanentDistrict")
    @Mapping(target = "permanentAddress.state", source = "permanentState")
    @Mapping(target = "permanentAddress.country", source = "permanentCountry")
    @Mapping(target = "addressSame", source = "presentPermanentSame")
    @Mapping(target = "activeDonor", source = "activeContributor")
    @Mapping(target = "loginMethod", source = "loginMethods", qualifiedByName = "mapLoginMethodsToDomain")
    User toDomain(UserEntity userEntity);

    // ----------------------
    // Custom Mapping Methods
    // ----------------------
    @Named("mapPhoneNumber")
    default String mapPhoneNumber(PhoneNumber number) {
        if (number != null) {
            return number.getPhoneCode() + "-" + number.getPhoneNumber();
        }
        return null;
    }

    @Named("mapAltPhoneNumber")
    default String mapAltPhoneNumber(PhoneNumber number) {
        return mapPhoneNumber(number);
    }

    @Named("mapPhoneNumberToDomain")
    default PhoneNumber mapPhoneNumberToDomain(String number) {
        if (number == null || !number.contains("-")) return null;
        String[] parts = number.split("-", 2);
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setPhoneCode(parts[0]);
        phoneNumber.setPhoneNumber(parts[1]);
        return phoneNumber;
    }

    @Named("mapAltPhoneNumberToDomain")
    default PhoneNumber mapAltPhoneNumberToDomain(String number) {
        return mapPhoneNumberToDomain(number);
    }

    @Named("mapLoginMethods")
    default String mapLoginMethods(List<LoginMethod> loginMethods) {
        if (loginMethods == null) return null;
        return loginMethods.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Named("mapLoginMethodsToDomain")
    default List<LoginMethod> mapLoginMethodsToDomain(String loginMethods) {
        if (loginMethods == null || loginMethods.isEmpty()) return new ArrayList<>();
        return Arrays.stream(loginMethods.split(","))
                     .map(LoginMethod::valueOf)
                     .collect(Collectors.toList());
    }
}
