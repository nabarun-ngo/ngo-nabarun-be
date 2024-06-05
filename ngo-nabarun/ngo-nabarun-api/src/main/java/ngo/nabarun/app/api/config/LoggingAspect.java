package ngo.nabarun.app.api.config;

import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

	/**
	 * Pointcut that matches all repositories, services and Web REST endpoints.
	 */
//	@Pointcut("within(@org.springframework.stereotype.Repository *)"
//			+ " || within(@org.springframework.stereotype.Service *)"
//			+ " || within(@org.springframework.web.bind.annotation.RestController *)")
//	public void springBeanPointcut() {
//	}

	/**
	 * Pointcut that matches all Spring beans in the application's main packages.
	 */
	//@Pointcut("execution(* ngo.nabarun.app..*(..))")
	@Pointcut("execution(* *(..)) &&"
			+ "("
			+ "    within(ngo.nabarun.app.api..*) ||"
			+ "    within(ngo.nabarun.app.businesslogic..*) ||"
			+ "    within(ngo.nabarun.app.infra..*) ||"
			+ "    within(ngo.nabarun.app.ext..*) ||"
			+ "    within(ngo.nabarun.app.util..*)"
			+ ")")
	public void applicationPackagePointcut() {
	}
	

//    @Pointcut("@annotation(ngo.nabarun.spring.server.helper.NoLogging)")
//    public void noLogging()  {
//    	//return writeLog(joinPoint);
//        
//    }
	/**
	 * Advice that logs methods throwing exceptions.
	 *
	 * @param joinPoint join point for advice
	 * @param e         exception
	 */
	@AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
		if(log.isDebugEnabled()) {
			//e.printStackTrace();
		}
		log.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL");
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
			log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
		}
		try {
			Object result = joinPoint.proceed();
			String value;
			if (log.isDebugEnabled()) {
				if (result != null) {
					try {
						value = ToStringBuilder.reflectionToString(result, ToStringStyle.MULTI_LINE_STYLE);
					} catch (Exception e) {
						try {
							value = ToStringBuilder.reflectionToString(result, ToStringStyle.MULTI_LINE_STYLE);
						} catch (Exception e1) {
							value = String.valueOf(result);
						}
					}
				} else {
					value = String.valueOf(result);
				}
				log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), value);
			}
			return result;
		} catch (IllegalArgumentException e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
			throw e;
		}
	}
}