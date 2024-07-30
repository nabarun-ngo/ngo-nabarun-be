package ngo.nabarun.app.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum RefDataType {
	USER,DONATION,PUBLIC, ACCOUNT
}
