package ngo.nabarun.app.businesslogic.implementation;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.businesslogic.IAdminBL;
import ngo.nabarun.app.businesslogic.businessobjects.ApiKeyDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail;
import ngo.nabarun.app.businesslogic.businessobjects.JobDetail.JobDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.KeyValue;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.ServiceDetail;
import ngo.nabarun.app.businesslogic.businessobjects.UserDetail.UserDetailFilter;
import ngo.nabarun.app.businesslogic.exception.BusinessException;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.enums.ApiKeyStatus;
import ngo.nabarun.app.common.enums.ProfileStatus;
import ngo.nabarun.app.infra.dto.ApiKeyDTO;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.UserDTO;

@Service
@Slf4j
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
	public List<String> adminServices(ServiceDetail trigger) throws Exception {
		JobDTO job = new JobDTO();
		processJobs(trigger, job);
		return job.getLogs();
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
	
	@Async
	@Override
	public void triggerJob(String triggerId, List<ServiceDetail> triggerDetail) {
		for (ServiceDetail trigger : triggerDetail) {
			JobDTO job = new JobDTO(triggerId, trigger.getName().name());
			try {
				if(trigger.getParameters().containsKey("day_of_month_to_skip")) {
					List<Integer> days = List.of(trigger.getParameters().get("day_of_month_to_skip").split(",")).stream().map(m->Integer.parseInt(m)).collect(Collectors.toList());
					Calendar cal = Calendar.getInstance();
					int dom = cal.get(Calendar.DAY_OF_MONTH);
					if(days.contains(dom)) {
						continue;
					}
				}
				
				commonDO.startJob(job, trigger);
				processJobs(trigger, job);
			} catch (Exception e) {
				job.setError(e);
				log.error("Error in cron service: ", e);
			} finally {
				try {
					commonDO.endJob(job, job.getOutput());
				} catch (Exception e) {
					log.error("Error in ending job: ", e);
				}
			}
		}
	}

	private void processJobs(ServiceDetail trigger,JobDTO job) throws Exception {
		Map<String, String> parameters = trigger.getParameters();
		Object output= null;
		switch (trigger.getName()) {
		//TODO Schedule this everyday at 7AM 
		case SYNC_SYSTEMS:
			commonDO.syncSystems(job);
			break;
		//TODO Schedule this on 1st day of every month at 7AM
		case CREATE_DONATION:
			UserDetailFilter filters = new UserDetailFilter();
			filters.setStatus(List.of(ProfileStatus.ACTIVE, ProfileStatus.BLOCKED));
			List<UserDTO> users = userDO.retrieveAllUsers(null, null, filters).getContent();
			output = donationDO.createBulkMonthlyDonation(users, job);
			break;
		//TODO Schedule Everyday at 7AM after 15th of month
		case DONATION_REMINDER_EMAIL:
			donationDO.sendDonationReminderEmail(job);
			break;
		//TODO Schedule this on 15st day of every month at 7AM
		case UPDATE_DONATION:
			donationDO.convertToPendingDonation(job);
			break;
		//TODO Schedule Everyday at 7AM and 7 PM
		case TASK_REMINDER_EMAIL:
			requestDO.sendTaskReminderEmail(job);
			break;
		case SYNC_USERS:
			boolean syncRole = parameters.get("sync_role") == null ? false
					: parameters.get("sync_role").equalsIgnoreCase("Y");
			String user_id = parameters.get("user_id") == null ? null : parameters.get("user_id");
			String user_email = parameters.get("user_email") == null ? null : parameters.get("user_email");
			userDO.syncUserDetail(job,syncRole, user_id, user_email);
			break;
		default:
			throw new BusinessException("Invalid Service " + trigger.getName());
		}
		job.setOutput(output);
	}
}
