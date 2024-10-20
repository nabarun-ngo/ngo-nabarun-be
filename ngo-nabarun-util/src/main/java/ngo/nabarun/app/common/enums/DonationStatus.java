package ngo.nabarun.app.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum DonationStatus {
	RAISED,PAID,PENDING,PAYMENT_FAILED,PAY_LATER,CANCELLED, UPDATE_MISTAKE
}
