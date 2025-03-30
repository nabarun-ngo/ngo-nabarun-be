package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;

import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.JobDTO.JobDTOFilter;

public interface IJobsInfraService {
	JobDTO createOrUpdateJob(JobDTO job) throws Exception;
	JobDTO getJobInfo(String id) throws Exception;
	Page<JobDTO> getJobList(Integer page,Integer size, JobDTOFilter filter);

}
