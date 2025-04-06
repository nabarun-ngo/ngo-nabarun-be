package ngo.nabarun.app.infra.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.core.entity.JobEntity;
import ngo.nabarun.app.infra.core.entity.QJobEntity;
import ngo.nabarun.app.infra.core.repo.JobsRepository;
import ngo.nabarun.app.infra.dto.JobDTO;
import ngo.nabarun.app.infra.dto.JobDTO.JobDTOFilter;
import ngo.nabarun.app.infra.misc.InfraDTOHelper;
import ngo.nabarun.app.infra.misc.InfraFieldHelper;
import ngo.nabarun.app.infra.misc.WhereClause;
import ngo.nabarun.app.infra.service.IJobsInfraService;

@Service
public class JobInfraServiceImpl implements IJobsInfraService{
	
	@Autowired
	private JobsRepository jobsRepo;

	@Override
	public JobDTO createOrUpdateJob(JobDTO jobDTO) throws Exception {
		
		JobEntity jobEntity = jobDTO.getId() != null ? jobsRepo.findById(jobDTO.getId()).orElseThrow() : new JobEntity();
		if(jobDTO.getId() == null) {
			jobEntity.setId(UUID.randomUUID().toString());
			jobEntity.setCreatedOn(CommonUtils.getSystemDate());
			jobEntity.setTriggerId(jobDTO.getTriggerId());
		}
		JobEntity jobEntityUpdate = new JobEntity();
		jobEntityUpdate.setName(jobDTO.getName());
		if(jobDTO.getInput() != null) {
			jobEntityUpdate.setInput(CommonUtils.getObjectMapper().writeValueAsString(jobDTO.getInput()));
		}
		jobEntityUpdate.setMemoryAtStart(jobDTO.getMemoryAtStart());
		jobEntityUpdate.setStart(jobDTO.getStartAt());
		jobEntityUpdate.setStatus(jobDTO.getStatus() == null ? null : jobDTO.getStatus().name());
		jobEntityUpdate.setEnd(jobDTO.getEndAt());
		if(jobDTO.getOutput() != null) {
			jobEntityUpdate.setOutput(CommonUtils.getObjectMapper().writeValueAsString(jobDTO.getOutput()));
		}
		jobEntityUpdate.setLog(InfraFieldHelper.stringListToString(jobDTO.getLogs()));
		jobEntityUpdate.setMemoryAtEnd(jobDTO.getMemoryAtEnd());
		if(jobDTO.getError() != null) {
			jobEntityUpdate.setErrorCause(ExceptionUtils.getRootCauseMessage(jobDTO.getError()));
			jobEntityUpdate.setErrorMessage(ExceptionUtils.getMessage(jobDTO.getError()));
			jobEntityUpdate.setStackTrace(ExceptionUtils.getStackTrace(jobDTO.getError()));
		}
		CommonUtils.copyNonNullProperties(jobEntityUpdate, jobEntity);

		jobEntity = jobsRepo.save(jobEntity);
		return InfraDTOHelper.convertToJobDTO(jobEntity);
	}


	
	@Override
	public JobDTO getJobInfo(String id) throws Exception {
		JobEntity jobEntity = jobsRepo.findById(id).orElseThrow();
		return InfraDTOHelper.convertToJobDTO(jobEntity);
	}

	@Override
	public Page<JobDTO> getJobList(Integer page, Integer size, JobDTOFilter filter) {
		Page<JobEntity> pagination = null;
		Sort sort = Sort.by(Sort.Direction.DESC, "createdOn");
		if (filter != null) {

			/*
			 * Query building and filter logic
			 */
			QJobEntity q = QJobEntity.jobEntity;
			BooleanBuilder query = WhereClause.builder()
					.optionalAnd(filter.getId() != null, () -> q.id.eq(filter.getId()))
					.optionalAnd(filter.getTriggerId() != null, () -> q.triggerId.eq(filter.getTriggerId()))
					.optionalAnd(filter.getName() != null, () -> q.name.contains(filter.getName()))
					.optionalAnd(filter.getStatus() != null,
							() -> q.status.in(filter.getStatus().stream().map(m -> m.name()).toList()))
					.optionalAnd(filter.getStart() != null && filter.getEnd() != null,
							() -> q.createdOn.between(filter.getStart(), filter.getEnd()))
					.build();

			if (page == null || size == null) {
				List<JobEntity> result = new ArrayList<>();
				jobsRepo.findAll(query, sort).iterator().forEachRemaining(result::add);
				pagination = new PageImpl<>(result);
			} else {
				pagination = jobsRepo.findAll(query, PageRequest.of(page, size, sort));
			}
		} else if (page != null && size != null) {
			pagination = jobsRepo.findAll(PageRequest.of(page, size, sort));
		} else {
			pagination = new PageImpl<>(jobsRepo.findAll(sort));
		}
		return pagination.map(InfraDTOHelper::convertToJobDTO);
	}

}
