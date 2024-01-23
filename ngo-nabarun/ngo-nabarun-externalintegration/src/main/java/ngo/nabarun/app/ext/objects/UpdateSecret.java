package ngo.nabarun.app.ext.objects;

import java.util.List;

import lombok.Data;

@Data
public class UpdateSecret {
	private String project;
	private String config;
	private List<ChangeRequest> change_requests;
	
	@Data
	public static class ChangeRequest{
		private String name;
		private String value;
		private String originalName;
		private String originalValue;
		private String visibility;
		private String originalVisibility;
		private boolean shouldDelete;

	}

}
