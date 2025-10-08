package ngo.nabarun.web.config;

import java.util.Arrays;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ngo.nabarun.common.util.CommonUtil;

@Profile("!prod")
@Aspect
@Component
@Slf4j
public class LoggingConfig {
	
	public @interface NoLogging{}

	@Value("${log.pretty.print:false}")
	private boolean prettyPrint;

	//private List<String> asyncCorrelationIds = new ArrayList<>();

	/**
	 * Point cut that matches all repositories, services and Web REST end points.
	 * Point cut that matches all Spring beans in the application's main packages.
	 */
	@Pointcut("execution(* *(..)) &&" + "(" + "    within(ngo.nabarun.web..*) ||"
			+ "    within(ngo.nabarun.domain..*) ||" + "    within(ngo.nabarun.infra..*) ||"
			+ "    within(ngo.nabarun.application..*) ||" + "    within(ngo.nabarun.commonNA..*)||"//trying to turn off logging in common
			+ "    within(com.auth0.net.client..*)" + ")" + "&&"
			+ "!@annotation(ngo.nabarun.web.config.LoggingConfig.NoLogging)")
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
		log.error("Exception in {}.{}() with cause = {} Stacktrace : {}",
				joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
				e.getCause() != null ? e.getCause() : "NULL", ExceptionUtils.getStackFrames(e));

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

	@Around("@annotation(org.springframework.scheduling.annotation.Async)")
	public Object logAsyncAround(ProceedingJoinPoint joinPoint) throws Throwable {
		Object result = joinPoint.proceed();
		return result;
	}

	private Object writeLog(ProceedingJoinPoint joinPoint) throws Throwable {
		if (log.isDebugEnabled()) {
			String args;
			try {
				args = CommonUtil.toJSONString(joinPoint.getArgs(), prettyPrint);
			} catch (Exception e) {
				args = Arrays.toString(joinPoint.getArgs());
			}

			log.debug("Enter: {}.{}() with argument[s] = {}",
					joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName(),
					args);

		}

		try {
			Object result = joinPoint.proceed();
			String value;
			if (log.isDebugEnabled()) {
				if (result != null) {
					try {
						value = CommonUtil.toJSONString(result, prettyPrint);
						// ToStringBuilder.reflectionToString(result, ToStringStyle.JSON_STYLE);
					} catch (Exception e) {
						value = String.valueOf(result);
					}
				} else {
					value = String.valueOf(result);
				}

				log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringType().getSimpleName(),
						joinPoint.getSignature().getName(), value);
				

			}
			return result;
		} catch (IllegalArgumentException e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
}