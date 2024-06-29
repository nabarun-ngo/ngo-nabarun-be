package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.RequestDTO.RequestDTOFilter;
import ngo.nabarun.app.infra.dto.WorkDTO;
import ngo.nabarun.app.infra.dto.WorkDTO.WorkListDTOFilter;

@Service
public interface IWorkflowInfraService {

	RequestDTO createWorkflow(RequestDTO workflow);
	Page<RequestDTO> getWorkflows(Integer page, Integer size, RequestDTOFilter filter);
	RequestDTO getWorkflow(String id);
	RequestDTO updateWorkflow(String id, RequestDTO workflow);
	WorkDTO createWorkList(WorkDTO worklist);
	WorkDTO updateWorkList(String id, WorkDTO worklistDTO);
	WorkDTO getWorkList(String id);
	Page<WorkDTO> getWorkList(Integer page, Integer size, WorkListDTOFilter filter);


}
