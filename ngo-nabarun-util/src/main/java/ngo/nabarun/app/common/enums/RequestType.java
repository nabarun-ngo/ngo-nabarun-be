package ngo.nabarun.app.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum RequestType {
	JOIN_REQUEST,JOIN_REQUEST_USER,CHECK_PAYMENT_GUEST, CHECK_PAYMENT, COLLECT_CASH_PAYMENT,TERMINATION_REQUEST,DONATION_PAUSE_REQUEST
	,PROFILE_UPDATE_REQUEST,
}
