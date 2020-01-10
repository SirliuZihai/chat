package com.zihai.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {
	 /**
     * 功能:压缩多个文件成一个zip文件
     * @param srcfile：源文件列表
     * @param zipfile：压缩后的文件
     */
    public static void zipFiles(File[] srcfile,File zipfile){
    	  byte[] buf=new byte[1024];
          ZipOutputStream out = null;
          FileInputStream in = null;
          try {
              //ZipOutputStream类：完成文件或文件夹的压缩
             out =new ZipOutputStream(new FileOutputStream(zipfile));
              for(int i=0;i<srcfile.length;i++){
                  if(!srcfile[i].exists())continue;
                  in =new FileInputStream(srcfile[i]);
                  out.putNextEntry(new ZipEntry(srcfile[i].getName()));
                  int len;
                  while((len=in.read(buf))>0){
                      out.write(buf,0,len);
                  }
                  out.closeEntry();
              }
              System.out.println("压缩完成.");
          } catch (Exception e) {
              e.printStackTrace();
          } finally {
          		try {
          			if(in!=null)in.close();
          			if(out!=null)out.close();
  				} catch (IOException e) {
  					e.printStackTrace();
  				}
          	
          }
      }
    /**
     * Thread.currentThread().getContextClassLoader().path + name
     * */	
    public static String getText(String filename) throws IOException{
    	InputStream stream =Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    	InputStreamReader  r = new InputStreamReader(stream);
    	BufferedReader read = new BufferedReader(r);
    	StringBuilder builder = new StringBuilder();
    	String s =null;
    	while((s = read.readLine())!=null){
    		builder.append(s);
    	}
    	return builder.toString();
    }
    
    public static void main(String[] args) {
        //2个源文件
        File f1=new File("C:\\test.txt");
        File f2=new File("C:\\data.txt");
        File[] srcfile={f1,f2};
        //压缩后的文件
        File zipfile=new File("C:\\TEST.zip");
        zipFiles(srcfile, zipfile);

    }
}
