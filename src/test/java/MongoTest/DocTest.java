package MongoTest;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;

import com.alibaba.fastjson.JSON;

public class DocTest {

	public static void main(String[] args) {
		Set<String> set = new HashSet<String>();
		set.add("liu");
		set.add("jia");
		System.out.println(JSON.toJSONString(set));
		String s =String.format("{'$match':{username:%s}}", JSON.toJSONString(set));
		
		Document d = Document.parse(s);
		System.out.println(d);
		System.out.println(d.toJson());
	}

}
