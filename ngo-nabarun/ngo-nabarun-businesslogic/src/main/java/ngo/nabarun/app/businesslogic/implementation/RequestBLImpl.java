package ngo.nabarun.app.businesslogic.implementation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.IRequestBL;
import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.domain.RequestDO;
import ngo.nabarun.app.common.util.SecurityUtils;

@Service
public class RequestBLImpl extends BaseBLImpl implements IRequestBL {

	@Autowired
	private RequestDO requestDO;

	@Override
	public Paginate<RequestDetail> getRequests(Integer index, Integer size, RequestDetailFilter filter) {
		return requestDO.retrieveAllRequests(index, size, filter);
	}
	
	@Override
	public RequestDetail getRequest(String id) {
		return requestDO.retrieveRequestDetails(id);
	}

	@Override
	public Paginate<RequestDetail> getMyRequests(Integer index, Integer size, boolean isDelegated) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return requestDO.retrieveUserRequests(index, size, userId, isDelegated);
	}

	@Override
	public RequestDetail createRequest(RequestDetail createRequest) throws Exception {
		String creatorUserId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return requestDO.createRequest(createRequest, false, creatorUserId, (workflowAction, workflow) -> {
			return performWorkflowAction(workflowAction, workflow);
		});
	}

	@Override
	public RequestDetail updateRequest(String id, RequestDetail request) throws Exception {
		return requestDO.updateRequest(id, request);
	}

	@Override
	public Paginate<WorkDetail> getMyWorkList(Integer index, Integer size, boolean isCompleted) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return requestDO.retrieveUserWorkList(index, size, userId, isCompleted);

	}

	@Override
	public List<WorkDetail> getWorkLists(String workflowId) throws Exception {
		return requestDO.retrieveWorkflowWorkList(workflowId);
	}

	@Override
	public WorkDetail updateWorkList(String id, WorkDetail request) throws Exception {
		String userId = propertyHelper.isTokenMockingEnabledForTest() ? propertyHelper.getMockedTokenUserId()
				: SecurityUtils.getAuthUserId();
		return requestDO.updateWorkItem(id, request, userId, ((t, u) -> {
			return performWorkflowAction(t, u);
		}));
	}

}
