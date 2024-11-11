package ngo.nabarun.app.common.enums;

public enum WorkType {
	DECISION("Decision"),
	NA("NA"),
	CONFIRMATION("Confirmation"),
	USER_INPUT("User Input");
	
	private String name;

	WorkType(String name) {this.name=name;}
	
	public String getName(){
		return name;
	}
}
