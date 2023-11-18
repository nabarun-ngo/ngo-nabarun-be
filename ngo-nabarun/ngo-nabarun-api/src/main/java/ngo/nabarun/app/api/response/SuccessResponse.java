package ngo.nabarun.app.api.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;


@Getter
	@JsonInclude(JsonInclude.Include.NON_EMPTY) 
	public class SuccessResponse<T>{
		@JsonProperty("info")
		private final String info="Success";
		
		@JsonProperty("timestamp")
		private Date timestamp=new Date();
		
		@JsonProperty("status")
		private int status;

		@JsonProperty("messages")
		private List<String> messages = new ArrayList<String>();
		
		@JsonInclude(JsonInclude.Include.NON_NULL) 
		@JsonProperty("responsePayload")
		private T responsePayload;
		
		public SuccessResponse<T> message(String... messages) {
			this.messages.clear();
			for (String message : messages) {
				this.messages.add(message);
			}
			return this;
		}

		public SuccessResponse<T> payload(T objects) {
			this.responsePayload=objects;
			return this;
		}
	
		public ResponseEntity<SuccessResponse<T>> get(HttpStatus status) {
			this.status=status.value();
			return new ResponseEntity<SuccessResponse<T>>(this,status);
		}
		
		
		
		public ResponseEntity<Resource> download(Resource resource,HttpStatus status) {
			 return ResponseEntity.status(status)
					.header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);

		}
		
	}