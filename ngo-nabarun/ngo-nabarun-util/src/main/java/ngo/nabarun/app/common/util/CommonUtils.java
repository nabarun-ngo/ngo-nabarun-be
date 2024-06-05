package ngo.nabarun.app.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtils {
	private final static ObjectMapper objectMapper =new ObjectMapper();

	

	public static boolean isCurrentMonth(Date givenDate) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();

		cal1.setTime(givenDate);
		cal2.setTime(new Date());
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}
	
	public static List<String> getMonthsBetween(Date startDate, Date endDate, String format) {
		List<String> list = new ArrayList<String>();
		Calendar beginCalendar = Calendar.getInstance();
		Calendar finishCalendar = Calendar.getInstance();
		beginCalendar.setTime(startDate);
		finishCalendar.setTime(endDate);
		DateFormat formaterYd = new SimpleDateFormat(format);
		while (beginCalendar.before(finishCalendar)) {
			list.add(formaterYd.format(beginCalendar.getTime()));
			beginCalendar.add(Calendar.MONTH, 1);
		}
		return list;
	}
	
	

	public static List<String> getMonthsBetween(Date startDate, Date endDate) {

		return getMonthsBetween(startDate, endDate, "MMMM yyyy");
	}

	public static Date getFormattedDate(String dateStr, String format) {
		if (dateStr == null || StringUtils.isBlank(dateStr)) {
			return null;
		}
		DateFormat formaterYd = new SimpleDateFormat(format);
		try {
			return formaterYd.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getFormattedDateString(Date date, String format) {
		if (date == null) {
			return null;
		}
		DateFormat formaterYd = new SimpleDateFormat(format);
		return formaterYd.format(date);
	}

	public static String getFormattedDateString(Date date) {
		return getFormattedDateString(date, "MMMM yyyy");
	}

	public static Date addDaysToDate(Date date, int days) {
		return addSecondsToDate(date,days*86400); // One day to 86400 seconds
	}
	
	public static Date addSecondsToDate(Date date, int seconds) {
		if (date == null) {
			return date;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date); 
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}

	public static String getURLToFileName(String url) {
		try {
			return Paths.get(new URI(url).getPath()).getFileName().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void copyNonNullProperties(Object src, Object target) {
		BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
	}

	private static String[] getNullPropertyNames(Object source) {
		final BeanWrapper src = new BeanWrapperImpl(source);
		java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

		Set<String> emptyNames = new HashSet<String>();
		for (java.beans.PropertyDescriptor pd : pds) {
			Object srcValue = src.getPropertyValue(pd.getName());
			if (srcValue == null)
				emptyNames.add(pd.getName());
		}
		String[] result = new String[emptyNames.size()];
		return emptyNames.toArray(result);
	}
	
	
	public static List<String> getProdProfileNames(){
		return List.of("PROD","PRODUCTION");
	}
	

	public static <T> T jsonToPojo(String json, Class<T> classz){
		try {
			return objectMapper.readValue(json, classz);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date getSystemDate() {
		return new Date();
	}
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
	public static byte[] toByteArray(URL url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try {
		  is = url.openStream ();
		  byte[] byteChunk = new byte[4096];
		  int n;

		  while ( (n = is.read(byteChunk)) > 0 ) {
		    baos.write(byteChunk, 0, n);
		  }
		}
		catch (IOException e) {
		  System.err.printf ("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
		  e.printStackTrace ();
		}
		finally {
		  if (is != null) { is.close(); }
		}
		return baos.toByteArray();
	}
	
	public static Map<String,Object> toMap(Object object){
		return objectMapper.convertValue(object, new TypeReference<Map<String,Object>>() {});
	}
	
	public static <T> T convertToType(Object object,TypeReference<T> type){
		ObjectMapper objectMapper = new ObjectMapper();
		//String jsonString = "{\"symbol\":\"ABCD\}";
		objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		//Trade trade = objectMapper.readValue(jsonString, new TypeReference<Symbol>() {});
		return objectMapper.convertValue(object, type);
	}
	
	public static String getUPIURI(String upiId,String payeeName, Double amount, String note, String txnId, String refId) {
		UriComponentsBuilder url= UriComponentsBuilder.fromUriString("upi://pay").queryParam("pa", upiId);
			if(payeeName != null) {
				url=url.queryParam("pn", payeeName);
			}
			if(amount != null) {
				url=url.queryParam("am", amount);
			}
			if(note != null) {
				url=url.queryParam("tn", note);
			}
			if(txnId != null) {
				url=url.queryParam("tid", txnId);
			}
			if(refId != null) {
				url=url.queryParam("tr", refId);
			}
			url=url.queryParam("cu", "INR");
		return url.build().encode().toUriString();
	}
	
}
