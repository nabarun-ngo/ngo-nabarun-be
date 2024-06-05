package ngo.nabarun.app.ext.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ObjectFilter;

@Service
public interface ICollectionExtService {

	Map<String, Object> updateCollectionData(String collectionName, String id, Map<String, Object> items) throws ThirdPartyException;

	Map<String, Object> storeCollectionData(String collectionName, Map<String, Object> item)
			throws ThirdPartyException;

	List<Map<String, Object>> getCollectionData(String collectionName, Integer page, Integer size,
			List<ObjectFilter> filters) throws ThirdPartyException;

	void removeCollectionData(String collectionName, String id) throws ThirdPartyException;

}
