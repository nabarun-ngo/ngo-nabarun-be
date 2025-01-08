package ngo.nabarun.app.infra.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.RequestDTO.RequestDTOFilter;
import ngo.nabarun.app.infra.dto.WorkDTO;
import ngo.nabarun.app.infra.dto.WorkDTO.WorkDTOFilter;

@Service
public interface IWorkflowInfraService {

	RequestDTO createRequest(RequestDTO workflow);
	Page<RequestDTO> getRequests(Integer page, Integer size, RequestDTOFilter filter);
	RequestDTO getRequest(String id);
	RequestDTO updateRequest(String id, RequestDTO workflow);
	WorkDTO createWorkItem(WorkDTO worklist);
	WorkDTO updateWorkItem(String id, WorkDTO worklistDTO);
	WorkDTO getWorkItem(String id);
	Page<WorkDTO> getWorkItems(Integer page, Integer size, WorkDTOFilter filter);


}
