package com.zihai.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter(urlPatterns = {"*.gz","*.js","*.css","*.json"}, filterName = "GzipFilter")
public class GzipFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory.getLogger(GzipFilter.class);


    /** 参数键值：头信息 */
    public static final String PARAM_KEY_HEADERS = "headers";

    /** 头信息 */
    private Map<String, String> headers;

    /**
     * <B>方法名称：</B>初始化<BR>
     * <B>概要说明：</B>初始化过滤器<BR>
     * 
     * @param fConfig 过滤器配置
     * @throws ServletException Servlet异常
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOGGER.info("===========init GzipFilter2============");	
	}

    /**
     * <B>方法名称：</B>过滤处理<BR>
     * <B>概要说明：</B>设定编码格式<BR>
     * 
     * @param request 请求
     * @param response 响应
     * @param chain 过滤器链
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
    	 String uri = ((HttpServletRequest)request).getRequestURI();
    	 LOGGER.info(uri+" get from server");
    	 HttpServletResponse res = (HttpServletResponse) response;
    	 if(uri.endsWith("gz")){
    		 res.addHeader("Content-Encoding", "gzip");
    	 }
         //res.addDateHeader("Expires", new Date().getTime()+20000);
         res.addHeader("Pragma", "no-cache");//Pragma:设置页面是否缓存，为Pragma则缓存，no-cache则不缓存
         chain.doFilter(request, response);
    }


    public void destroy() {
        this.headers.clear();
        this.headers = null;
    }

}