package com.zihai.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cglib.beans.BeanCopier;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BeanUtil {
	
	/**
	 * copy source to target
	 * */
	public void beancopy(Object source,Object target){
		 BeanCopier copier = BeanCopier.create(source.getClass(), target.getClass(), false);
	     copier.copy(source, target, null);
	}
	public  List<Map<String, Object>> convertListBean2ListMap(List<?> beanList)
    {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (int i = 0, n = beanList.size(); i < n; i++)
        {
            Object bean = beanList.get(i);
            Map<String,Object> map = convertBean2Map(bean);
            mapList.add(map);
        }
        return mapList;
    }
    public  Map<String, Object> convertBean2Map(Object bean) {
		SimpleDateFormat sf2 = new SimpleDateFormat("yyyy/MM/dd");
            Class<? extends Object> type = bean.getClass();
            Map<String, Object> returnMap = new HashMap<String, Object>();
            BeanInfo beanInfo;
			try {
				beanInfo = Introspector.getBeanInfo(type);
			
            
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++)
            {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                String propertyName = descriptor.getName();
                if (!propertyName.equals("class"))
                {
                    Method readMethod = descriptor.getReadMethod();
                    if(readMethod ==null)continue;
                    Object result = readMethod.invoke(bean, new Object[0]);
                   
                    if (result != null)
                    {
                    	if(descriptor.getPropertyType()==Date.class)result = sf2.format(result);
                        returnMap.put(propertyName, result);
                    }
                    else
                    {
                        returnMap.put(propertyName, null);
                    }
                }
            }
            return returnMap;
			} catch (Exception e) {
				e.printStackTrace();
			}
			 return null;
        }
    /**
     * json
     * */
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException{
		Bean b = new ObjectMapper().readValue("{'name':'liu','age':'23'}",Bean.class);
		//Bean b = JSON.parseObject("{'name':'liu','age':23}", Bean.class);
		System.out.println(b.getAge());
	}
	
	public class Bean{
		private String name;
		private int age;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		
	}
}
