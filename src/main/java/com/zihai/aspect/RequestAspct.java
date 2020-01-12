package com.zihai.aspect;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
public class RequestAspct {
	private Logger log =LoggerFactory.getLogger(getClass());
	/**
	 * execution(* com.sample.service.impl..*.*(..))
	 * 整个表达式可以分为五个部分：
	 1、execution(): 表达式主体。
	 2、第一个*号：表示返回类型，*号表示所有的类型。	
	 3、包名：表示需要拦截的包名，后面的两个句点表示当前包和当前包的所有子包，com.sample.service.impl包、子孙包下所有类的方法。	
	 4、第二个*号：表示类名，*号表示所有的类。	
	 5、*(..):最后这个星号表示方法名，*号表示所有的方法，后面括弧里面表示方法的参数，两个句点表示任何参数。
	 * */
	@Pointcut("execution(public * com.zihai.*.controller..*.*(..))")
	public void weblog(){
	}
	
	@Before("weblog()")
	public void doBefore(JoinPoint joinPoint){
		ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attr.getRequest();
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if(principal != null)
		log.info(principal.toString() +"request ————"+request.getRequestURL().toString());
	
	}
}
