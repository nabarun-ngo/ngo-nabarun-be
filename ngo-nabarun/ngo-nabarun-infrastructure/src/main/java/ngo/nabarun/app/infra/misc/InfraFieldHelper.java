package ngo.nabarun.app.infra.misc;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class InfraFieldHelper {

	public static String stringListToString(List<String> fieldList) {
		if(fieldList == null) {
			return null;
		}
		return fieldList.isEmpty() ? "" : String.join(",", fieldList);
	}

	public static String integerListToString(List<Integer> fieldList) {
		if(fieldList == null) {
			return null;
		}
		return fieldList.isEmpty() ? ""
				: String.join(",", fieldList.stream().map(m -> String.valueOf(m)).toList());
	}

	public static List<String> stringToStringList(String fieldString) {
		return fieldString == null ? List.of() : List.of(fieldString.split(fieldString));
	}

	public static List<Integer> stringToIntegerList(String fieldString) {
		return fieldString == null ? List.of()
				: List.of(fieldString.split(fieldString)).stream().map(m -> Integer.parseInt(m)).toList();
	}

}
