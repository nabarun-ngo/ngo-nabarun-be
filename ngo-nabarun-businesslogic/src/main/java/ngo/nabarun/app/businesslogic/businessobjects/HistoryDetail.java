package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class HistoryDetail {
	private String id;	
	private String title;	
	private List<ChangeDetail> changes;	
	private String createdBy;
	private Date createdOn;
	
	@Data
	public static class ChangeDetail{
		private String fieldName;
		private Object from;
		private Object to;
		private boolean add;
		private boolean remove;
		private boolean change;
		private String message;
	}
}
