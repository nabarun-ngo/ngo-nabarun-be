package ngo.nabarun.app.common.util;

public class UserUtil {
	
	public static String getName(String title,String fullName,String firstName,String middleName,String lastName) {
		String titleUser = title == null ? "" : title + " ";
		if (fullName != null) {
			return title +fullName;
		} else {
			String firstNameUser = firstName == null ? "" : firstName + " ";
			String middleNameUser = middleName == null ? "" : middleName + " ";
			String lastNameUser = lastName == null ? "" : lastName;
			return titleUser + firstNameUser + middleNameUser + lastNameUser;
		}
		
	}

}
