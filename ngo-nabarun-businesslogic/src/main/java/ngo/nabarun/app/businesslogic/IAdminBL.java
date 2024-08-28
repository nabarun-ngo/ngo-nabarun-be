package ngo.nabarun.app.businesslogic;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface IAdminBL {
	Map<String, String> generateApiKey(List<String> scopes);
	void syncUsers() throws Exception;
}
