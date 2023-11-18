package ngo.nabarun.app.ext.service;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.CalendarEvent;

@Service
public interface IGoogleCalendarExtService {
	String getAuthorizationUrl(String callbackUrl, String state) throws ThirdPartyException;
	CalendarEvent createCalendarEvent(String code, String callbackUrl,CalendarEvent event) throws ThirdPartyException;
	boolean authorizationRequired(String id);
}
