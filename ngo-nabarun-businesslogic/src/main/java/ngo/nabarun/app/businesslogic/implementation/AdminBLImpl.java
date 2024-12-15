package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.IAdminBL;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.infra.dto.UserDTO;
@Service

public class AdminBLImpl extends BaseBLImpl implements IAdminBL {
	
	@Autowired
	private CacheManager cacheManager;
	
	@Override
	public void clearSystemCache(List<String> names) {
		if(names == null || names.size() == 0) {
			System.out.println(cacheManager.getCacheNames());
			cacheManager.getCacheNames().stream().forEach(name->cacheManager.getCache(name).clear());
		}else {
			names.stream().forEach(name->cacheManager.getCache(name).clear());
		}
	}
	
	@Override
	public Map<String, String> generateApiKey(List<String> scopes) {
		return commonDO.generateAPIKey(scopes);
	}

	@Override
	public void adminServices(ServiceDetail trigger) throws Exception {
		Map<String, String> parameters=trigger.getParameters();
		switch (trigger.getName()) {
		case SYNC_SYSTEMS:
			commonDO.syncSystems();
			break;
		case CREATE_DONATION:
			List<UserDTO> users = userDO.retrieveAllUsers(null, null, new UserDetailFilter()).getContent();
			donationDO.createBulkMonthlyDonation(users);
			break;
		case DONATION_REMINDER_EMAIL:
			donationDO.sendDonationReminderEmail();
			break;
		case UPDATE_DONATION:
			donationDO.convertToPendingDonation();
			break;
		case SYNC_USERS:
			boolean syncRole=parameters.get("sync_role") == null ? false : parameters.get("sync_role").equalsIgnoreCase("Y");
			String user_id=parameters.get("user_id") == null ? null : parameters.get("user_id");
			String user_email=parameters.get("user_email") == null ? null : parameters.get("user_email");
			userDO.syncUserDetail(syncRole,user_id,user_email);
			break;
		case TASK_REMINDER_EMAIL:
			requestDO.sendTaskReminderEmail();
			break;
		default:
			throw new Exception("Invalid Service "+trigger.getName());
		}
	}

	
	@Override
	public Map<String, String> updateApiKey(String id, List<String> scopes, boolean revoke) {
		return null;
	}
}
