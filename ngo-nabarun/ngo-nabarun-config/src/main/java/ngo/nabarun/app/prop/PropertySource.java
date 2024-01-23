package ngo.nabarun.app.prop;

import java.util.Map;

public abstract class PropertySource {


	public abstract Map<String, Object> loadProperties() throws Exception;
	public Map<String, Object> loadProperties(String secretKey) {
		return null;
	}
	void addOrUpdateProperty(String key, Object value, String description, boolean encrypted, String secretKey)
			throws Exception {
	}
	void close() {
	}

}
