package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.IAdminBL;
@Service
public class AdminBLImpl extends BaseBLImpl implements IAdminBL {
	
	@Override
	public Map<String, String> generateApiKey(List<String> scopes) {
		return commonDO.generateAPIKey(scopes);
	}

	@Override
	public void syncUsers() throws Exception {
		userDO.syncUserDetail(null);
	}
}
