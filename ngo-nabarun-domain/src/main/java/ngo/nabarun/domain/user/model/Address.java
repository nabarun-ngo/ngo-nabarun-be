package ngo.nabarun.domain.user.model;

import lombok.Value;
import lombok.Builder;

@Value
@Builder(toBuilder = true)
public class Address {
    String addressLine1;
    String addressLine2;
    String addressLine3;
    String hometown;
    String zipCode;
    String state;
    String district;
    String country;
}
