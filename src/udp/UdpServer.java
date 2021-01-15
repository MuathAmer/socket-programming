package udp;

import util.VigenereCipher;
import util.VigenereEncrypter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import static util.Files.FILES_PATH;
import static util.Files.menuOfFiles;

public class UdpServer {

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

		var socket = new DatagramSocket(1999);
		var receiveBuffer = new byte[1024];

		System.out.println("Server started.");
		while (true) {
			System.out.println("\nWaiting to receive a packet...");
			var initPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			socket.receive(initPacket);
			System.out.println("Received initiation packet, packetSize=" + initPacket.getLength());

			//send selection menu
			byte[] menuBytes = selectionMenu.getBytes();
			var menuPacket = new DatagramPacket(menuBytes, menuBytes.length, initPacket.getSocketAddress());
			socket.send(menuPacket);

			//read response
			var selectionResponse = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			socket.receive(selectionResponse);
			int selectionIdx = receiveBuffer[0] - 1;
			String selectedFileName = SERVED_FILES[selectionIdx];
			System.out.println("Client chose \"" + selectedFileName + "\"");

			File selectedFile = new File(FILES_PATH + "/" + selectedFileName);
			var fileInputStream = new FileInputStream(selectedFile);
			var bufferedInputStream = new BufferedInputStream(fileInputStream);
			System.out.println("Sending file...");

			var encrypter = new VigenereEncrypter(VigenereCipher.DEFAULT_KEY);
			byte[] fileBuffer = new byte[512];
			int count;
			while ((count = bufferedInputStream.read(fileBuffer)) != -1) {
				encrypter.encryptData(fileBuffer);
				var partOfFile = new DatagramPacket(fileBuffer, count, initPacket.getSocketAddress());
				socket.send(partOfFile);
			}

			System.out.println("File successfully sent.");
			System.out.println("Done with this client.");
		}
	}

}