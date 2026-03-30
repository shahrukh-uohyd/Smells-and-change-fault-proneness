package mlssdd.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Directory {

	public static List<String> getFolder(String filePath) {
		File file = new File(filePath);
		String[] names = file.list();
		List<String> list = new ArrayList<>();
		for (String name : names) {
			if (new File(filePath+name).isDirectory()) {
				list.add(filePath+name);
			}
		}
		//System.out.println(list);
		return list;
	}
}
