package ngo.nabarun.app.web.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;


@ControllerAdvice(annotations = Controller.class)
@Order(2)
public class HandleMVCException {

	private static final String DEFAULT_ERROR_MESSAGE = "Something went wrong. Please try again.";
	
	@Value("${INCLUDE_ERROR_DETAILS}")
	private boolean includeErrorDetails;

	@ExceptionHandler(value = { Exception.class })
	public ModelAndView handleError(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        ModelAndView mav = new ModelAndView("error");
        //ex.printStackTrace();
        mav.addObject("message", DEFAULT_ERROR_MESSAGE);
		if(includeErrorDetails) {
	        mav.addObject("description", ex.getCause() != null ? "Error Notification: "+ ex.getMessage() +"<br> Error Cause: "+ ex.getCause().getMessage() : "Error Notification: "+ ex.getMessage());
		}
        mav.addObject("code", response.getStatus());
        mav.addObject("pageName", "Error");
        return mav;
    }

}