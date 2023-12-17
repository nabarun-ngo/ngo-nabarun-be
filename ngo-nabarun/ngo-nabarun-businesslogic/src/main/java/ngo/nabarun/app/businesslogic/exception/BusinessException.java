package ngo.nabarun.app.businesslogic.exception;

import org.springframework.stereotype.Component;


public class BusinessException extends Exception {

	public static final String s="";
	private static final long serialVersionUID = 1L;

	public BusinessException(String message) {
		super(message);
		
	}
	
//	public BusinessException(BusinessExceptionMessage key) {
//		super(inactiveDonor);
//	}
//	
}
