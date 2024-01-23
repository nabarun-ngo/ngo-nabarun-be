package ngo.nabarun.app.infra.misc;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class InfraFieldHelper {

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
		return fieldString == null ? List.of() : List.of(fieldString.split(splitter));
	}

	public static List<Integer> stringToIntegerList(String fieldString,String splitter) {
		return fieldString == null ? List.of()
				: List.of(fieldString.split(splitter)).stream().map(m -> Integer.parseInt(m)).toList();
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

}
