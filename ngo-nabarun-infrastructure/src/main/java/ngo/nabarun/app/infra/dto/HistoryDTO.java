package ngo.nabarun.app.infra.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class HistoryDTO {
	
	private String id;	
	private List<ChangeDTO> changes;	
	
	private String createdBy;
	private String createdById;
	private String createdByName;

	private String referenceId;
	private String referenceType;
	
	private long createdOn;
	private String action;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChangeDTO {
		private String fieldname;
		private Object from;
		private Object to;
		private String changeType;

	}
	
	
	
}