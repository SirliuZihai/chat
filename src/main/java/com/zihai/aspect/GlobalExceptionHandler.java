package com.zihai.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zihai.util.BusinessException;
import com.zihai.util.Result;

@ControllerAdvice
public class GlobalExceptionHandler {
	private Logger log =LoggerFactory.getLogger(getClass());

	
	@ExceptionHandler(RuntimeException.class)
	@ResponseBody
	public Result exceptionHandler(Exception e){
		log.error(e.getMessage(), e);;
		if(e instanceof BusinessException){
			return Result.failure(e.getMessage());
		}else{
			return Result.failure("系统错误");
		}		
	}
}
