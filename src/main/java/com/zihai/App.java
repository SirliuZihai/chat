package com.zihai;

import org.apache.shiro.io.ResourceUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@ServletComponentScan  //使WebFilter生效 不然全部拦截（同时去掉@Conponent）
@SpringBootApplication
//(exclude= {DataSourceAutoConfiguration.class})extends WebMvcConfigurationSupport
public class App {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}
	//这里配置静态资源文件的路径导包都是默认的直接导入就可以
  /*  @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations(ResourceUtils.CLASSPATH_PREFIX + "/static/");
        super.addResourceHandlers(registry);
    }*/
}