package com.zihai.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class EncrypUtil {
	//private RandomNumberGenerator randomNumberGenerator =new SecureRandomNumberGenerator();
	private static String algorithmName = "md5";
	private static final int hashIterations = 2;
	public static String encryptPassword(String password) {
		String newPassword = new SimpleHash(algorithmName,password,ByteSource.Util.bytes("zihai"),
							hashIterations).toHex();
		return newPassword;
	}
	
	public static void main(String args[]){
		String newPassword = new SimpleHash(algorithmName,"111111",ByteSource.Util.bytes("zihai"),
				hashIterations).toHex();
		System.out.println(newPassword);
	}
}
