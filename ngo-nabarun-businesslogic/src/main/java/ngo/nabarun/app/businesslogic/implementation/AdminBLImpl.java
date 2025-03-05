package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.IAdminBL;
import ngo.nabarun.app.businesslogic.businessobjects.ApiKeyDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail.JobDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.UserDTO;

@Service

public class AdminBLImpl extends BaseBLImpl implements IAdminBL {

	@Autowired
	private CacheManager cacheManager;

	@Override
	public void clearSystemCache(List<String> names) {
		if (names == null || names.size() == 0) {
			System.out.println(cacheManager.getCacheNames());
			cacheManager.getCacheNames().stream().forEach(name -> cacheManager.getCache(name).clear());
		} else {
			names.stream().forEach(name -> cacheManager.getCache(name).clear());
		}
	}

	@Override
	public void adminServices(ServiceDetail trigger) throws Exception {
		Map<String, String> parameters = trigger.getParameters();
		switch (trigger.getName()) {
		case SYNC_SYSTEMS:
			commonDO.syncSystems(new JobDTO());
			break;
		case CREATE_DONATION:
			List<UserDTO> users = userDO.retrieveAllUsers(null, null, new UserDetailFilter()).getContent();
			donationDO.createBulkMonthlyDonation(users);
			break;
		case DONATION_REMINDER_EMAIL:
			donationDO.sendDonationReminderEmail(new JobDTO());
			break;
		case UPDATE_DONATION:
			donationDO.convertToPendingDonation(new JobDTO());
			break;
		case SYNC_USERS:
			boolean syncRole = parameters.get("sync_role") == null ? false
					: parameters.get("sync_role").equalsIgnoreCase("Y");
			String user_id = parameters.get("user_id") == null ? null : parameters.get("user_id");
			String user_email = parameters.get("user_email") == null ? null : parameters.get("user_email");
			userDO.syncUserDetail(syncRole, user_id, user_email);
			break;
		case TASK_REMINDER_EMAIL:
			requestDO.sendTaskReminderEmail(new JobDTO());
			break;
		default:
			throw new Exception("Invalid Service " + trigger.getName());
		}
	}

	@Override
	public ApiKeyDetail generateApiKey(ApiKeyDetail detail) {
		ApiKeyDTO apiKeyDTO = commonDO.generateAPIKey(detail);
		return BusinessObjectConverter.toApiKeyDetail(apiKeyDTO, apiKeyDTO.getApiKey());
	}

	@Override
	public ApiKeyDetail updateApiKey(String id, ApiKeyDetail keyDetail, boolean revoke) {
		ApiKeyDTO apiKeyDTO = commonDO.updateAPIKey(id, keyDetail, revoke);
		return BusinessObjectConverter.toApiKeyDetail(apiKeyDTO, null);
	}

	@Override
	public List<ApiKeyDetail> getApiKeys() {
		return commonDO.getAPIKeys(ApiKeyStatus.ACTIVE).stream()
				.map(m -> BusinessObjectConverter.toApiKeyDetail(m, null)).collect(Collectors.toList());
	}

	@Override
	public List<KeyValue> getApiKeyScopes() throws Exception {
		return commonDO.getApiScopes().stream().map(m -> {
			KeyValue keyValue = new KeyValue();
			keyValue.setKey(m.get("name"));
			keyValue.setDescription(m.get("description"));
			keyValue.setValue(keyValue.getKey()+" ["+keyValue.getDescription()+"]");
			return keyValue;
		}).toList();
	}

	@Override
	public Paginate<JobDetail> getJobList(Integer pageIndex, Integer pageSize, JobDetailFilter filter) {
		return commonDO.retrieveJobs(pageIndex, pageSize, filter).map(BusinessObjectConverter::toJobDetail);
	}
}
