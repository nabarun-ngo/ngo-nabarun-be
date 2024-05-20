package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.WorkFlowDTO;
import ngo.nabarun.app.infra.dto.WorkFlowDTO.WorkFlowDTOFilter;
import ngo.nabarun.app.infra.dto.WorkListDTO;
import ngo.nabarun.app.infra.dto.WorkListDTO.WorkListDTOFilter;

@Service
public interface IWorkflowInfraService {

	WorkFlowDTO createWorkflow(WorkFlowDTO workflow);
	Page<WorkFlowDTO> getWorkflows(Integer page, Integer size, WorkFlowDTOFilter filter);
	WorkFlowDTO getWorkflow(String id);
	WorkFlowDTO updateWorkflow(String id, WorkFlowDTO workflow);
	WorkListDTO createWorkList(WorkListDTO worklist);
	WorkListDTO updateWorkList(String id, WorkListDTO worklistDTO);
	WorkListDTO getWorkList(String id);
	Page<WorkListDTO> getWorkList(Integer page, Integer size, WorkListDTOFilter filter);


}
