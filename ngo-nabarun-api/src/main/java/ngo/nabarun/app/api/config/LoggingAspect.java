package ngo.nabarun.app.api.config;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.dto.LogsDTO;
import ngo.nabarun.app.infra.service.ILogInfraService;

@Profile("!prod")
@Aspect
@Component
@Slf4j
public class LoggingAspect {

	/**
	 * Pointcut that matches all repositories, services and Web REST endpoints.
	 */

//	@Lazy
//	@Autowired
//	private ILogInfraService logInfraService;
	
	/**
	 * Pointcut that matches all Spring beans in the application's main packages.
	 */
	@Pointcut("execution(* *(..)) &&"
			+ "("
			+ "    within(ngo.nabarun.app.api..*) ||"
			+ "    within(ngo.nabarun.app.businesslogic..*) ||"
			+ "    within(ngo.nabarun.app.infra..*) ||"
			+ "    within(ngo.nabarun.app.ext..*) ||"
			+ "    within(ngo.nabarun.app.util..*)||"
			+ "    within(com.auth0.net.client..*)"
			+ ")"
			+ "&&"
			+ "!@annotation(ngo.nabarun.app.common.annotation.NoLogging)")
	public void applicationPackagePointcut() {
	}
	

	/**
	 * Advice that logs methods throwing exceptions.
	 *
	 * @param joinPoint join point for advice
	 * @param e         exception
	 */
	@AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
//		if(log.isDebugEnabled()) {
//			e.printStackTrace();
//		}
		log.error("Exception in {}.{}() with cause = {} Stacktrace : {}", joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL",ExceptionUtils.getStackTrace(e));
		
	}

	/**
	 * Advice that logs when a method is entered and exited.
	 *
	 * @param joinPoint join point for advice
	 * @return result
	 * @throws Throwable throws IllegalArgumentException
	 */
	@Around("applicationPackagePointcut()")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		return writeLog(joinPoint);
	}

	private Object writeLog(ProceedingJoinPoint joinPoint) throws Throwable {
		// System.out.println(log.isDebugEnabled());
		LogsDTO logsDTO= new LogsDTO();
		logsDTO.setCorelationId(MDC.get("CorrelationId"));
		
		if (log.isDebugEnabled()) {
			String args;
			try{
				args=CommonUtils.toJSONString(joinPoint.getArgs());
			}catch (Exception e) {
				args=Arrays.toString(joinPoint.getArgs());
			}
			logsDTO.setInputs(args);
			logsDTO.setStartTime(new Date());
			log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(),args );
		}
		try {
			Object result = joinPoint.proceed();
			String value;
			if (log.isDebugEnabled()) {
				if (result != null) {
					try {
//						value = ToStringBuilder.reflectionToString(result, ToStringStyle.MULTI_LINE_STYLE);
						value = CommonUtils.toJSONString(result);
					} catch (Exception e) {
//						try {
//							value = ToStringBuilder.reflectionToString(result, ToStringStyle.MULTI_LINE_STYLE);
//						} catch (Exception e1) {
//							value = String.valueOf(result);
//						}
						value =String.valueOf(result);
					}
				} else {
					value = String.valueOf(result);
				}
				logsDTO.setMethodName(joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName()+"()");
				logsDTO.setOutputs(value);
				log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), value);
			}
			return result;
		} catch (IllegalArgumentException e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
			logsDTO.setError(ExceptionUtils.getStackTrace(e));
			throw e;
		} catch (Exception e) {
			logsDTO.setError(ExceptionUtils.getStackTrace(e));
			throw e;
		}
		finally {
			if (log.isDebugEnabled()) {
				logsDTO.setEndTime(new Date());
				if(logsDTO.getCorelationId() != null) {
					//logInfraService.saveLog(logsDTO);
				}
				
			}
		}
	}
}