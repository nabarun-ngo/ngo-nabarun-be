package ngo.nabarun.app.api.config;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.RestController;

import ngo.nabarun.app.api.response.ErrorResponse;
import ngo.nabarun.app.businesslogic.exception.BusinessException;

@ControllerAdvice(annotations = RestController.class)
@Order(1)
public class HandleRestException {

	@ExceptionHandler(value = { Exception.class })
	//@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ErrorResponse> handleServerExceptions(Exception ex) {
		ex.printStackTrace();
		if (ex instanceof BusinessException) {
			return new ErrorResponse(ex).get(HttpStatus.BAD_REQUEST);
		}
		return new ErrorResponse(ex).get(HttpStatus.INTERNAL_SERVER_ERROR);
	}


	@ExceptionHandler({ HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class,
			HttpMediaTypeNotAcceptableException.class, MissingPathVariableException.class,
			MissingServletRequestParameterException.class, ServletRequestBindingException.class,
			ConversionNotSupportedException.class, TypeMismatchException.class, HttpMessageNotReadableException.class,
			HttpMessageNotWritableException.class, MethodArgumentNotValidException.class,
			MissingServletRequestPartException.class, BindException.class, NoHandlerFoundException.class,
			AsyncRequestTimeoutException.class, MaxUploadSizeExceededException.class, IllegalArgumentException.class })

	public final ResponseEntity<Object> handleException2(Exception ex, WebRequest request) throws Exception {
		HttpStatus status = null;
		if (ex instanceof HttpRequestMethodNotSupportedException) {
			status = HttpStatus.METHOD_NOT_ALLOWED;
		} else if (ex instanceof HttpMediaTypeNotSupportedException) {
			status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
		} else if (ex instanceof HttpMediaTypeNotAcceptableException) {
			status = HttpStatus.NOT_ACCEPTABLE;
		} else if (ex instanceof MissingPathVariableException) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		} else if (ex instanceof MissingServletRequestParameterException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof ServletRequestBindingException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof ConversionNotSupportedException) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		} else if (ex instanceof TypeMismatchException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof HttpMessageNotReadableException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof HttpMessageNotWritableException) {
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		} else if (ex instanceof MethodArgumentNotValidException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof MissingServletRequestPartException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof BindException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof NoHandlerFoundException) {
			status = HttpStatus.NOT_FOUND;
		} else if (ex instanceof AsyncRequestTimeoutException) {
			status = HttpStatus.SERVICE_UNAVAILABLE;
		} else if (ex instanceof MaxUploadSizeExceededException) {
			status = HttpStatus.BAD_REQUEST;
		} else if (ex instanceof IllegalArgumentException) {
			status = HttpStatus.BAD_REQUEST;
		}
System.out.println("hello");
		return new ResponseEntity<Object>(new ErrorResponse(ex), status);
	}

}