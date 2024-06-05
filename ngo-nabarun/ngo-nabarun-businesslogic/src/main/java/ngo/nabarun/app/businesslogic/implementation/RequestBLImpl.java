package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.IRequestBL;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.domain.RequestDO;
import ngo.nabarun.app.businesslogic.helper.BusinessObjectConverter;
import ngo.nabarun.app.common.util.SecurityUtils;
import ngo.nabarun.app.infra.dto.RequestDTO;
import ngo.nabarun.app.infra.dto.WorkDTO;

@Service
public class RequestBLImpl extends BaseBLImpl implements IRequestBL {

	@Autowired
	private RequestDO requestDO;

	@Override
	public Paginate<RequestDetail> getRequests(Integer index, Integer size, RequestDetailFilter filter) {
		return requestDO.retrieveAllRequests(index, size, filter).map(BusinessObjectConverter::toRequestDetail);
	}
	
	@Override
	public RequestDetail getRequest(String id) {
		return BusinessObjectConverter.toRequestDetail(requestDO.retrieveRequestDetails(id));
	}

	@Override
	public Paginate<RequestDetail> getMyRequests(Integer index, Integer size, boolean isDelegated) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return requestDO.retrieveUserRequests(index, size, userId, isDelegated).map(BusinessObjectConverter::toRequestDetail);
	}

	@Override
	public RequestDetail createRequest(RequestDetail createRequest) throws Exception {
		String creatorUserId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		RequestDTO request= requestDO.createRequest(createRequest, false, creatorUserId, (workflowAction, workflow) -> {
			return performWorkflowAction(workflowAction, workflow);
		});
		return BusinessObjectConverter.toRequestDetail(request); 
	}

	@Override
	public RequestDetail updateRequest(String id, RequestDetail request) throws Exception {
		return BusinessObjectConverter.toRequestDetail(requestDO.updateRequest(id, request));
	}

	@Override
	public Paginate<WorkDetail> getMyWorkList(Integer index, Integer size, boolean isCompleted) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return requestDO.retrieveUserWorkList(index, size, userId, isCompleted).map(m -> BusinessObjectConverter.toWorkItem(m));

	}

	@Override
	public List<WorkDetail> getWorkLists(String workflowId) throws Exception {
		return requestDO.retrieveWorkflowWorkList(workflowId).stream().map(BusinessObjectConverter::toWorkItem).collect(Collectors.toList());
	}

	@Override
	public WorkDetail updateWorkList(String id, WorkDetail request) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		WorkDTO requestDTO= requestDO.updateWorkItem(id, request, userId, ((t, u) -> {
			return performWorkflowAction(t, u);
		}));
		return BusinessObjectConverter.toWorkItem(requestDTO); 

	}

}
