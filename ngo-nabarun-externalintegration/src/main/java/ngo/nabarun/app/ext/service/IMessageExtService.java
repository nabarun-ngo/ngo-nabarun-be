package ngo.nabarun.app.ext.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;

@Service
public interface IMessageExtService {

	List<String> sendMessage(String title, String body, String imageUrl, List<String> tokens, Map<String, String> data) throws ThirdPartyException;

	String saveItemInRealtimeDB(String url, Object data) throws ThirdPartyException;
	

}
