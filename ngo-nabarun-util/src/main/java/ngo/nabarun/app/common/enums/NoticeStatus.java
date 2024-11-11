package ngo.nabarun.app.common.enums;

public enum NoticeStatus {
	ACTIVE("Active"), 
	EXPIRED("Expired"), 
	DRAFT("Draft");

	private String name;

	NoticeStatus(String name) {this.name=name;}
	
	public String getName(){
		return name;
	}

}
