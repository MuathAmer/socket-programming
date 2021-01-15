package tcp;

import util.VigenereCipher;
import util.VigenereEncrypter;

import java.io.*;
import java.net.ServerSocket;
import java.util.Arrays;

import static util.Assets.ASSETS_PATH;
import static util.Assets.menuOfFiles;

/*
 * This class works as a TcpServer and is supposed to work with its TcpClient.
 * It uses the TCP protocol using java.net.Socket, this class is not responsible
 * for dealing with packets, the socket deals with them under the hood.
 *
 * This server waits for a client to connect, once connected, it will send the
 * client a list of files that can be served, waiting for the user to pick one.
 * Once the user picks a file, it will send the file block by block until it is
 * finished. Notice that the file will be encrypted and the client will need to
 * decrypt it using the same key. Simple Vigenere cipher is used for encryption.
 *
 * All of the data is transferred on the same socket in 1 persistent connection.
 * */

public class TcpServer {

	//The names of files to be served (files in src/assets)
	private static final String[] SERVED_ASSETS = new File(ASSETS_PATH).list();

	//Template for the selection menu to be sent to the user, placeholder will be replaced with the list of served files
	private static final String SELECTION_MENU_TEMPLATE = """
			Hi from me the server :D

			Here are the files I have, which one are you requesting?
			[type the number of your selection]
			   
			%s
			....>\s""";

	public static void main(String[] args) throws IOException {
		assert SERVED_ASSETS != null;
		Arrays.sort(SERVED_ASSETS);
		//Replace placeholder with an ordered list menu of served files
		String selectionMenu = SELECTION_MENU_TEMPLATE.formatted(menuOfFiles(SERVED_ASSETS));

		var serverSocket = new ServerSocket(1999);

		System.out.println("Server started.");
		while (true) {
			System.out.println("\nWaiting for a client to connect...");
			//This function is blocking, server will wait until a client connects to it
			var socket = serverSocket.accept();
			System.out.println("Client connected, " + socket);

			//PrintWriter is used to make it easier to send strings along the socket instead of bytes.
			var printWriter = new PrintWriter(socket.getOutputStream());
			printWriter.print(selectionMenu);
			printWriter.flush();

			//After sending the selection menu to the user, expect to hear back a selection response.
			//Prepare for reading
			var inputStream = new InputStreamReader(socket.getInputStream());
			var bufferedReader = new BufferedReader(inputStream);

			String response = bufferedReader.readLine();
			int selectionIdx = Integer.parseInt(response) - 1;
			String selectedFileName = SERVED_ASSETS[selectionIdx];
			System.out.println("Client chose \"" + selectedFileName + "\"");

			//Prepare to send selected file, read from file and write to socket
			File selectedFile = new File(ASSETS_PATH + "/" + selectedFileName);
			var fileInputStream = new FileInputStream(selectedFile);
			var bufferedInputStream = new BufferedInputStream(fileInputStream);
			var outputStream = socket.getOutputStream();
			System.out.println("Sending file...");

			var encrypter = new VigenereEncrypter(VigenereCipher.DEFAULT_KEY);
			byte[] buffer = new byte[4096];
			int count;
			//Read the file block by block, encrypt it and write it to socket
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
