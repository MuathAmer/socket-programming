package tcp;

import util.VigenereCipher;
import util.VigenereDecrypter;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/*
 * This class works as a TcpClient and is supposed to work with its TcpServer.
 * It uses the TCP protocol using java.net.Socket, the socket is responsible for
 * dealing with packets under the hood.
 *
 * This client assumes a server is already running and waiting for it, once connected,
 * it will receive from the server a list of files that can be requested, and will wait
 * for the user to write their selection through standard input (console).
 * Once the user writes a selection, the client will send the selection number to
 * the server, and start receiving this requested file.
 * This client will receive the file block by block until the file is totally received. Notice
 * that the file will be encrypted and this client will need to decrypt it using the
 * same key. Simple Vigenere cipher is used for encryption.
 * */

public class TcpClient {
	public static void main(String[] args) throws IOException {
		var socket = new Socket("localhost", 1999);
		socket.setSoTimeout(100); //If server stops sending for 100ms, then issue a signal (Exception)

		System.out.println("Server says:\n");
		var inputStreamReader = new InputStreamReader(socket.getInputStream());
		var incomingText = inputStreamReader.read();
		//Try to read from the inputStream until SoTimeout exception (~Server stopped sending)
		try {
			while (incomingText != -1) {
				System.out.print((char) incomingText);
				incomingText = inputStreamReader.read();
			}
		} catch (Exception ignored) {
		}

		//Server finished talking. Ask user to input their selection
		var scanner = new Scanner(System.in);
		var line = scanner.nextLine();
		//Send user's selection to server
		var printWriter = new PrintWriter(socket.getOutputStream(), true);
		printWriter.println(line);

		//Prepare socket as input and file as output
		var inStream = socket.getInputStream();
		var fileOutputStream = new FileOutputStream("output.txt");
		System.out.println("Receiving file...");
		var decrypter = new VigenereDecrypter(VigenereCipher.DEFAULT_KEY); //Encryption key can also be input by user
		byte[] buffer = new byte[4096]; // buffer of 4kb, to support very large files and increase performance
		int count;
		//Receive block by block, decrypt it and write it to output file.
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