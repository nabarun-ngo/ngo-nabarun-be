package ngo.nabarun.domain.user.model;

import lombok.Value;
import lombok.Builder;

@Value
@Builder(toBuilder = true)
public class Link {
    String type; // e.g. FACEBOOK, INSTAGRAM, WEBSITE
    String url;
}
