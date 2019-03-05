package com.zihai.shiro.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

public class appFilter extends AuthenticatingFilter {
    private static final Logger log = LoggerFactory.getLogger(appFilter.class);

	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
    	if("application/json".equals(request.getContentType())){
    		String s ="";
    		BufferedReader r_buf;
			try {
				r_buf = request.getReader();
				StringBuilder s_build = new StringBuilder();
	    		while((s=r_buf.readLine())!=null){
	    			s_build.append(s);
	    		}
	    		r_buf.reset();
	    		r_buf.close();
	    		Map<String,String> map = JSON.parseObject(s_build.toString(), Map.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    	log.info("always true now");
    	return true;
    }
}
