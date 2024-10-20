package ngo.nabarun.app.ext.helpers;

import java.util.List;

import lombok.Data;

@Data
public class ObjectFilter {
	
	private String key;
	private Operator operator;
	private Object value;
	private List<Object> values;

	public ObjectFilter(String key,Operator operator,Object value) {
		this.key=key;
		this.operator=operator;
		this.value=value;
	}
	
//	public ObjectFilter(String key,List<Object> values,Operator operator) {
//		this.key=key;
//		this.operator=operator;
//		this.values=values;
//	}
	
	public enum Operator{EQUAL,CONTAIN,IN, ARRAY_CONTAIN}
}
