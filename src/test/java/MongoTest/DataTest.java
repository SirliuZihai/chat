package MongoTest;

import java.io.IOException;
import java.text.ParseException;

import com.zihai.util.FileUtil;

public class DataTest {

	public static void main(String[] args) throws ParseException, IOException {
		String hello = FileUtil.getText("introduce.txt");
		System.out.println(hello);
	}

}
