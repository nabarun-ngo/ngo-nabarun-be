package ngo.nabarun.app.infra.misc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class InfraFieldHelper {
	
	public static final String metadata_active_user= "active_user";
	public static final String metadata_profile_id= "profile_id";

	public static String stringListToString(List<String> fieldList,String splitter) {
		if(fieldList == null) {
			return null;
		}
		return fieldList.isEmpty() ? "" : String.join(splitter, fieldList);
	}

	public static String integerListToString(List<Integer> fieldList,String splitter) {
		if(fieldList == null) {
			return null;
		}
		return fieldList.isEmpty() ? ""
				: String.join(splitter, fieldList.stream().map(m -> String.valueOf(m)).toList());
	}

	public static List<String> stringToStringList(String fieldString,String splitter) {
		return fieldString == null ? new ArrayList<>() : new ArrayList<>(List.of(fieldString.split(splitter)));
	}

	public static List<Integer> stringToIntegerList(String fieldString,String splitter) {
		return fieldString == null ? new ArrayList<>()
				: new ArrayList<>(List.of(fieldString.split(splitter))).stream().map(m -> Integer.parseInt(m)).toList();
	}
	
	public static String stringListToString(List<String> fieldList) {
		return stringListToString(fieldList,",");
	}

	public static String integerListToString(List<Integer> fieldList) {
		return integerListToString(fieldList,",");
	}

	public static List<String> stringToStringList(String fieldString) {
		return stringToStringList(fieldString,",");
	}

	public static List<Integer> stringToIntegerList(String fieldString) {
		return stringToIntegerList(fieldString,",");
	}
	
	public static String removeItemFromString(String str,String item) {
		List<String> strList=stringToStringList(str);
		if(strList.size() > 0) {
			strList.remove(item);
		}
		return stringListToString(strList);
	}
	
	public static String addItemToString(String str,String item) {
		List<String> strList=stringToStringList(str);
		strList.add(item);
		return stringListToString(strList);
	}

}
