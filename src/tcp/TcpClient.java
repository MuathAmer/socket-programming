package tcp;

import util.VigenereCipher;
import util.VigenereDecrypter;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TcpClient {
	public static void main(String[] args) throws IOException {
		var socket = new Socket("localhost", 1999);
		socket.setSoTimeout(100);

		System.out.println("Server says:\n");
		var inputStreamReader = new InputStreamReader(socket.getInputStream());
		var incomingText = inputStreamReader.read();
		try {
			while (incomingText != -1) {
				System.out.print((char) incomingText);
				incomingText = inputStreamReader.read();
			}
		} catch (Exception ignored) {
		}

		var scanner = new Scanner(System.in);
		var line = scanner.nextLine();
		var printWriter = new PrintWriter(socket.getOutputStream(), true);
		printWriter.println(line);

		var inStream = socket.getInputStream();
		var fileOutputStream = new FileOutputStream("output.txt");
		System.out.println("Receiving file...");
		var decrypter = new VigenereDecrypter(VigenereCipher.DEFAULT_KEY);
		byte[] buffer = new byte[4096]; // buffer of 4kb, to support very large files and increase performance
		int count;
		while ((count = inStream.read(buffer)) != -1) {
			decrypter.decryptData(buffer);
			fileOutputStream.write(buffer, 0, count);
		}

		System.out.println("File is successfully received.");
		fileOutputStream.flush();
		fileOutputStream.close();
		System.out.println("Closing socket.");
		socket.close();

		System.out.println("Opening file...");
		java.awt.Desktop.getDesktop().open(new File("output.txt"));
	}
}