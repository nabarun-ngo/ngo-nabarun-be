package ngo.nabarun.app.businesslogic.helper;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import ngo.nabarun.app.common.enums.JobStatus;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.service.IJobsInfraService;

@Component
public class AsyncJobExecutor {

	@Autowired
	private IJobsInfraService jobInfraService;
	
	@Async("threadPoolTaskExecutor")
    public void processJob(JobDTO job, Function<JobDTO, Boolean> function) throws Exception {
		job.setStartAt(CommonUtils.getSystemDate());
		job.setStatus(JobStatus.IN_PROGRESS);
		job.setMemoryAtStart(getCurrentMemory());
		job = jobInfraService.createOrUpdateJob(job);
		Boolean completed = function.apply(job);
		job.setEndAt(CommonUtils.getSystemDate());
		job.setStatus(completed ? JobStatus.COMPLETED : JobStatus.FAILED);
		job.setMemoryAtEnd(getCurrentMemory());
		job = jobInfraService.createOrUpdateJob(job);
	}

	private String getCurrentMemory() {
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
		long freeMemory = runtime.freeMemory() / 1024;
		long totalMemory = runtime.totalMemory() / 1024;
		long maxMemory = runtime.maxMemory() / 1024;
		return "Total memory (kB): " + totalMemory + " Free memory (kB): " + freeMemory + " Used memory (kB): "
				+ usedMemory + " Max memory (kB): " + maxMemory;
	}
}