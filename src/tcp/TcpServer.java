package tcp;

import util.VigenereCipher;
import util.VigenereEncrypter;

import java.io.*;
import java.net.ServerSocket;
import java.util.Arrays;

import static util.Files.FILES_PATH;
import static util.Files.menuOfFiles;

public class TcpServer {


	// {"file1.txt", "file2.txt"}
	// "1) file1.txt
	//  2) file2.txt"
	private static final String[] SERVED_FILES = new File(FILES_PATH).list();
	private static final String SELECTION_MENU_TEMPLATE = """
			Hi from me server :D

			Here are the files I have, which one are you requesting?
			[type the number of your selection]
			   
			%s
			....>\s""";

	public static void main(String[] args) throws IOException {
		assert SERVED_FILES != null;
		Arrays.sort(SERVED_FILES);
		String selectionMenu = SELECTION_MENU_TEMPLATE.formatted(menuOfFiles(SERVED_FILES));

		var serverSocket = new ServerSocket(1999);

		System.out.println("Server started.");
		while (true) {
			System.out.println("\nWaiting for a client to connect...");
			var socket = serverSocket.accept();
			System.out.println("Client connected, " + socket);

			var printWriter = new PrintWriter(socket.getOutputStream());
			printWriter.print(selectionMenu);
			printWriter.flush();

			var inputStream = new InputStreamReader(socket.getInputStream());
			var bufferedReader = new BufferedReader(inputStream);

			String response = bufferedReader.readLine();
			int selectionIdx = Integer.parseInt(response) - 1;
			String selectedFileName = SERVED_FILES[selectionIdx];
			System.out.println("Client chose \"" + selectedFileName + "\"");

			File selectedFile = new File(FILES_PATH + "/" + selectedFileName);
			var fileInputStream = new FileInputStream(selectedFile);
			var bufferedInputStream = new BufferedInputStream(fileInputStream);
			var outputStream = socket.getOutputStream();
			System.out.println("Sending file...");

			var encrypter = new VigenereEncrypter(VigenereCipher.DEFAULT_KEY);
			byte[] buffer = new byte[4096];
			int count;
			while ((count = bufferedInputStream.read(buffer)) != -1) {
				encrypter.encryptData(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.flush();
			System.out.println("File successfully sent.");
			System.out.println("Closing socket.");
			socket.close();
		}
	}

}
