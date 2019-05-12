package com.zihai.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EnCacheUtil {
	public static CacheManager cacheManager = CacheManager.create(ClassLoader.getSystemResource("properties/ehcache.xml").getPath());
	
	@Override
	protected void finalize() throws Throwable {
		cacheManager.shutdown();
		super.finalize();
	}
	public static void main(String[] args) {
		System.out.println(ClassLoader.getSystemResource("properties/ehcache.xml").getPath());
		// 2. 获取缓存对象
        Cache cache = cacheManager.getCache("users");
        // 3. 创建元素
        Element element = new Element("key1", "value1");
        // 4. 将元素添加到缓存
        cache.put(element);
        // 5. 获取缓存
        Element value = cache.get("key1");
        System.out.println(value);
        System.out.println(value.getObjectValue());
     // 6. 删除元素
        cache.remove("key1");
        System.out.println(cache.get("key1"));
     // 7. 刷新缓存
        cache.flush();
        // 8. 关闭缓存管理器
        cacheManager.shutdown();

	}

}
