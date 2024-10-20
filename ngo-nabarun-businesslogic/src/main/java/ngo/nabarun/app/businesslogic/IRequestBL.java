package ngo.nabarun.app.businesslogic;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.businesslogic.businessobjects.Paginate;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail;
import ngo.nabarun.app.businesslogic.businessobjects.RequestDetail.RequestDetailFilter;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail;
import ngo.nabarun.app.businesslogic.businessobjects.WorkDetail.WorkDetailFilter;

@Service
public interface IRequestBL {

	Paginate<RequestDetail> getRequests(Integer index, Integer size, RequestDetailFilter filter);
	RequestDetail getRequest(String id);
	RequestDetail createRequest(RequestDetail createRequest) throws Exception;
	RequestDetail updateRequest(String id, RequestDetail request) throws Exception;
	Paginate<RequestDetail> getMyRequests(Integer index, Integer size,RequestDetailFilter filter) throws Exception;
	Paginate<WorkDetail> getMyWorkList(Integer index, Integer size, WorkDetailFilter filter) throws Exception;
	List<WorkDetail> getWorkLists(String workflowId) throws Exception;
	WorkDetail updateWorkList(String id, WorkDetail request) throws Exception;
	

}
