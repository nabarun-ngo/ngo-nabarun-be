package ngo.nabarun.web.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import ngo.nabarun.common.util.CommonUtil;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

	@JsonProperty("info")
	private final String info = "Error";

	@JsonProperty("timestamp")
	private Date timestamp = new Date();
	
	@JsonProperty("traceId")
	private final String traceId= MDC.get("CorrelationId");

	@JsonProperty("status")
	private int status;

	@JsonProperty("messages")
	private List<String> messages = new ArrayList<String>();

	@JsonProperty("errorCause")
	private String errorCause;

	@JsonProperty("details")
	private List<String> details = new ArrayList<String>();

	@JsonProperty("stackTrace")
	private String stackTrace;
	
	@JsonProperty("version")
	private final String version= CommonUtil.getEnvProperty("VERSION");

	public ErrorResponse(Exception e,boolean includeStacktrace) {
		messages.add(e.getMessage());
		details.add("Notification : " + e.getMessage());
		if (e.getCause() != null) {
			details.add("Cause : " + e.getCause().getMessage());
		}
		details.add("Exception : " + e.getClass().getSimpleName());
		errorCause = e.getCause() != null ? e.getCause().getMessage() : null;
		if(includeStacktrace) {
			this.stackTrace=ExceptionUtils.getStackTrace(e);
		}

	}

	public ErrorResponse message(String... messages) {
		this.messages.clear();
		for (String message : messages) {
			this.messages.add(message);
		}
		return this;
	}

	public ResponseEntity<ErrorResponse> get(HttpStatus status) {
		this.status = status.value();
		return new ResponseEntity<>(this, status);
	}

}