package ngo.nabarun.domain.user.model;

import lombok.Value;
import lombok.Builder;

@Value
@Builder(toBuilder = true)
public class PhoneNumber {
    String phoneCode;
    String phoneNumber;

    public String normalized() {
        return (phoneCode == null ? "" : phoneCode) + "-" + (phoneNumber == null ? "" : phoneNumber);
    }
}
