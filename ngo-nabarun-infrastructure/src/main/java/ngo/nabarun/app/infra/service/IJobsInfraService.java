package ngo.nabarun.app.infra.service;

import ngo.nabarun.app.infra.dto.JobDTO;

public interface IJobsInfraService {
	<Input, Output> JobDTO<Input, Output> createOrUpdateJob(JobDTO<Input, Output> job) throws Exception;
}
