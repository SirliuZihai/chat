package com.zihai.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

public class EncrypUtil {
	//private RandomNumberGenerator randomNumberGenerator =new SecureRandomNumberGenerator();
	private static String algorithmName = "md5";
	private static final int hashIterations = 2;
	private static Cipher cipher;
	private static  SecretKey key;
	public static String encryptPassword(String password) {
		String newPassword = new SimpleHash(algorithmName,password,ByteSource.Util.bytes("zihai"),
							hashIterations).toHex();
		return newPassword;
	}
	
	public static void main(String args[]){
		/*String newPassword = new SimpleHash(algorithmName,"111111",ByteSource.Util.bytes("zihai"),
				hashIterations).toHex();
		System.out.println(newPassword);*/
		String d = encode("youyou");
		System.out.println(d);
		System.out.println(decode(d));
		
	}
	static {
		try {
			 //生成key
           /* KeyGenerator keyGenerator=KeyGenerator.getInstance("DES");
            keyGenerator.init(56);      //指定key长度，同时也是密钥长度(56位)
            SecretKey secretKey = keyGenerator.generateKey(); //生成key的材料
            byte[] key = secretKey.getEncoded();  //生成key      
*/			byte[] key_o = Hex.decodeHex("547a496d6d54c11a");
			DESKeySpec desKeySpec=new DESKeySpec(key_o);
			SecretKeyFactory factory=SecretKeyFactory.getInstance("DES");
	        key = factory.generateSecret(desKeySpec);      //转换后的密钥
			cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (DecoderException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String encode(String src){
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);   //指定为加密模式
			byte[] result=cipher.doFinal(src.getBytes());
			return Hex.encodeHexString(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	public static String decode(String src){
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);   //指定为加密模式
			byte[] result=cipher.doFinal(Hex.decodeHex(src));
			return new String(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	public static String getUserName(String token){
		String[] token_date = EncrypUtil.decode(token).split("&");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(token_date[3]));
		if(Calendar.getInstance().after(c)){
			throw new BusinessException("token 失效");
		}
		return token_date[0];
	}
	public static UsernamePasswordToken getAuthInfo(String token){
		String[] token_date = EncrypUtil.decode(token).split("&");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(token_date[3]));
		if(Calendar.getInstance().after(c)){
			throw new BusinessException("token 失效");
		}
		UsernamePasswordToken token1 = new UsernamePasswordToken(token_date[0],token_date[1],token_date[2]);		
		return token1;
	}
	public void jdkDES(String src){
        try {
            //生成key
            KeyGenerator keyGenerator=KeyGenerator.getInstance("DES");
            keyGenerator.init(56);      //指定key长度，同时也是密钥长度(56位)
            SecretKey secretKey = keyGenerator.generateKey(); //生成key的材料
            byte[] key = secretKey.getEncoded();  //生成key         
            //key转换成密钥
            DESKeySpec desKeySpec=new DESKeySpec(key);
            SecretKeyFactory factory=SecretKeyFactory.getInstance("DES");
            SecretKey key2 = factory.generateSecret(desKeySpec);      //转换后的密钥
            System.out.println("key2==="+Hex.encodeHexString(key));
            //加密
            
            Cipher cipher=Cipher.getInstance("DES/ECB/PKCS5Padding");  //算法类型/工作方式/填充方式
            cipher.init(Cipher.ENCRYPT_MODE, key2);   //指定为加密模式
            byte[] result=cipher.doFinal(src.getBytes());
            System.out.println("jdkDES加密: "+Hex.encodeHexString(result));  //转换为十六进制
            
            //解密
            cipher.init(Cipher.DECRYPT_MODE,key2);  //相同密钥，指定为解密模式
            result = cipher.doFinal(result);   //根据加密内容解密
            System.out.println("jdkDES解密: "+new String(result));  //转换字符串
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
