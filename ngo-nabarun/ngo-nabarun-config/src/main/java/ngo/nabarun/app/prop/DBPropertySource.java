package ngo.nabarun.app.prop;

import java.util.Map;

public interface DBPropertySource {


	Map<String, Object> loadProperties();
	Map<String, Object> loadProperties(String secretKey);
	void addOrUpdateProperty(String key, Object value, String description, boolean encrypted, String secretKey)
			throws Exception;
	void close();

}
