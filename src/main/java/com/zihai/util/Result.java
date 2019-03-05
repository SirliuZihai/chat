package com.zihai.util;

import java.io.Serializable;

public class Result implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean success;
	private String message;
	private Object data;
	
	public static Result success(String message){
		Result result = new Result();
		result.success = true;
		result.message = message;
		return result;
	};
	public static Result success(String message,Object data){
		Result result = new Result();
		result.success = true;
		result.message = message;
		result.data = data;
		return result;
	};
	public static Result failure(String message){
		Result result = new Result();
		result.success = false;
		result.message = message;
		return result;
	};
	public static Result failure(String message,Object data){
		Result result = new Result();
		result.success = false;
		result.message = message;
		result.data = data;
		return result;
	};
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
}
