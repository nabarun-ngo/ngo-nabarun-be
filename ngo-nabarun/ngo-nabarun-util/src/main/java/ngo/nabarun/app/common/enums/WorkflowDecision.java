package ngo.nabarun.app.common.enums;

public enum WorkflowDecision {
	//APPROVE,DECLINE;
	APPROVE("Approved"),DECLINE("Declined");

	private String value;

	WorkflowDecision(String string) {
		this.value=string;
	}
	
	public String getValue() {
		return this.value;
	}
}
