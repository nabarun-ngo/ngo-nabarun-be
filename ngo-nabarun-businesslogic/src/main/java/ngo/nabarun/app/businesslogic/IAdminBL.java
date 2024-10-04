package ngo.nabarun.app.businesslogic;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.enums.TriggerEvent;

@Service
public interface IAdminBL {
	Map<String, String> generateApiKey(List<String> scopes);

	void cronTrigger(List<TriggerEvent> trigger, Map<String, String> param);
}
