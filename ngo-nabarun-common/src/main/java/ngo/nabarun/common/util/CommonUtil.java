package ngo.nabarun.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtil {
	private final static ObjectMapper objectMapper = new ObjectMapper();

	public static String getURLToFileName(String url) {
		try {
			return Paths.get(new URI(url).getPath()).getFileName().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T jsonToPojo(String json, Class<T> classz) {
		try {
			return objectMapper.readValue(json, classz);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T defaultIfNull(T newValue, T currentValue) {
	    return newValue != null ? newValue : currentValue;
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = ConcurrentHashMap.newKeySet();
		return t -> seen.add(keyExtractor.apply(t));
	}
	
	public static boolean isNotNull(Object obj) {
		if(obj == null) return false;
		if(obj instanceof String) {
			String str = (String) obj;
			return str.trim().length() > 0;
		}
		return true;
	}
	
	public static boolean isNotNull(Object... objs) {
		for(Object obj:objs) {
			if(!isNotNull(obj)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isNullOrEmpty(String str) {
		return !isNotNull(str);
	}

	public static byte[] toByteArray(URL url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
			is = url.openStream();
			byte[] byteChunk = new byte[4096];
			int n;

			while ((n = is.read(byteChunk)) > 0) {
				baos.write(byteChunk, 0, n);
			}
		} catch (IOException e) {
			System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
			e.printStackTrace();
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return baos.toByteArray();
	}

	public static Map<String, Object> toMap(Object object) {
		return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {
		});
	}

	public static <T> T convertToType(Object object, TypeReference<T> type) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		return objectMapper.convertValue(object, type);
	}

	
	public static <T> T convertToType(Object object, Class<T> type) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		return objectMapper.convertValue(object, type);
	}
	
	public static String toJSONString(Object obj,boolean pretty) throws JsonProcessingException {
		if(pretty) {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		}
		return objectMapper.writeValueAsString(obj);
	}
	
	public static boolean areEqual(Object oldValue, Object newValue) {
        if (oldValue == null) {
            return newValue == null;
        } else {
            return oldValue.equals(newValue);
        }
    }
	
	public static String getEnvProperty(String key,String defaultValue) {
		String value= System.getenv(key) == null ? System.getProperty(key) : System.getenv(key);
		return value == null ? defaultValue : value;
	}

	public static String getEnvProperty(String key) {
		return getEnvProperty(key,null);
	}

	public static ObjectMapper getObjectMapper(){
		return objectMapper;
	}

}