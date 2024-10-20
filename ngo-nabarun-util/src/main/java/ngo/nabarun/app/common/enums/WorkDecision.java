package ngo.nabarun.app.common.enums;

public enum WorkDecision {
	//APPROVE,DECLINE;
	APPROVE("Approved"),DECLINE("Declined");

	private String value;

	WorkDecision(String string) {
		this.value=string;
	}
	
	public String getValue() {
		return this.value;
	}
}
