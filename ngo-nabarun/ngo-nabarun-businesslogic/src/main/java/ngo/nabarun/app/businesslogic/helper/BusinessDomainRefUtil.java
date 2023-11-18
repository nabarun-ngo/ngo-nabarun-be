package ngo.nabarun.app.businesslogic.helper;

import java.util.List;
import java.util.Optional;

import ngo.nabarun.app.infra.misc.KeyValuePair;
import ngo.nabarun.app.infra.misc.UserConfigTemplate;

public class BusinessDomainRefUtil {
	
	
	public static boolean isTitleGenderAligned(UserConfigTemplate userConfig,String title,String gender) throws Exception{		
		Optional<KeyValuePair> userTitle=userConfig.getUserTitles().stream().filter(f->f.getKey().equalsIgnoreCase(title)).findFirst();
		if(userTitle.isEmpty()) {
			return false;
		}
		String allowedGender=userTitle.get().getAttributes().getOrDefault("GENDER","").toString();
		return allowedGender.contains("*") || allowedGender.toUpperCase().contains(gender.toUpperCase());
	}
	
	public static String getGenderValue(UserConfigTemplate userConfig,String key) throws Exception {
		Optional<KeyValuePair> gender=userConfig.getUserGenders().stream().filter(f->f.getKey().equalsIgnoreCase(key)).findFirst();
		return gender.isEmpty() ? key : gender.get().getDisplayValue();
	}

	public static String getTitleValue(UserConfigTemplate userConfig,String titleKey) throws Exception {
		Optional<KeyValuePair> title=userConfig.getUserTitles().stream().filter(f->f.getKey().equalsIgnoreCase(titleKey)).findFirst();
		return title.isEmpty() ? titleKey : title.get().getDisplayValue();
	}

//	public static String isSameRoleGroup(UserConfigTemplate userConfig,String roleCode) throws Exception {
//		Optional<KeyValuePair> title=userConfig.getUserTitles().stream().filter(f->f.getKey().equalsIgnoreCase(titleKey)).findFirst();
//		return title.isEmpty() ? titleKey : title.get().getDisplayValue();
//	}
//	


}
