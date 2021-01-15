package util;

public class Assets {
	//Path of folder of served files
	public static final String ASSETS_PATH = "src/assets";

	/*
	Takes an array of fileName strings, and returns an ordered list in 1 string.

	in: {"first.txt", "second.txt"}

	out: """
			1) first.txt
			2) second.txt
		"""
	*/
	public static String menuOfFiles(String[] fileNames) {
		var stringBuilder = new StringBuilder();
		int i = 1;
		for (String fileName : fileNames) {
			stringBuilder.append(i).append(") ").append(fileName).append("\n");
			i++;
		}
		return stringBuilder.toString();
	}

}
