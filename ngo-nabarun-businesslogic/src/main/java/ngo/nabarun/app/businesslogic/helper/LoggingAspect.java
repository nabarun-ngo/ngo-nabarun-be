package ngo.nabarun.app.businesslogic.helper;

import java.util.Arrays;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.app.common.util.CommonUtils;

@Profile("!prod")
@Aspect
@Component
@Slf4j
public class LoggingAspect {

	/**
	 * Pointcut that matches all repositories, services and Web REST endpoints.
	 */

	
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
		System.err.println(joinPoint.getSourceLocation());
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
		
		if (log.isDebugEnabled()) {
			String args;
			try{
				args=CommonUtils.toJSONString(joinPoint.getArgs());
			}catch (Exception e) {
				args=Arrays.toString(joinPoint.getArgs());
			}
			
			log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(),args);
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
						value =String.valueOf(result);
					}
				} else {
					value = String.valueOf(result);
				}
				log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), value);
			}
			return result;
		} catch (IllegalArgumentException e) {
			System.err.println(joinPoint.getSourceLocation());
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
			throw e;
		} catch (Exception e) {
			throw e;
		}
		finally {
			if (log.isDebugEnabled()) {
				
				
			}
		}
	}
}