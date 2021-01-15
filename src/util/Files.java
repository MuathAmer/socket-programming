package util;

public class Files {
	public static final String FILES_PATH = "src/files";

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
