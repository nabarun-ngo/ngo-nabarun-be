package ngo.nabarun.domain.request.event;

import java.util.Map;

import ngo.nabarun.domain.request.enums.RequestType;

public record RequestWorkflowFulfilledEvent(String id, RequestType type,Map<String,String> data) {
}
