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

import static util.Assets.ASSETS_PATH;
import static util.Assets.menuOfFiles;

/* ALERT: This server serves files and sends them through UDP, which is not safe
 * and doesn't guarantee packets delivery or order. This use case of UDP is for
 * educational purposes only.
 *
 * This class works as a UdpServer and is supposed to work with its UdpClient.
 * It uses the UDP protocol using java.net.DatagramSocket and java.net.DatagramPacket,
 * this class is responsible for dealing with the packets itself.
 *
 * This server waits for a client to send an initiation packet to connect to it,
 * once connected, it will send the client a list of files that can be served,
 * waiting for the user to pick one.
 * Once the user picks a file, it will send the file block by block, each block
 * is of size 500B and is sent over 1 packet, blocks are sent until all of the
 * file is sent. Notice that the file will be encrypted and the client will need to
 * decrypt it using the same key. Simple Vigenere cipher is used for encryption.
 * */

public class UdpServer {

	//The names of files to be served (files in src/assets)
	private static final String[] SERVED_FILES = new File(ASSETS_PATH).list();

	//Template for the selection menu to be sent to the user, placeholder will be replaced with the list of served files
	private static final String SELECTION_MENU_TEMPLATE = """
			Hi from me the server :D

			Here are the files I have, which one are you requesting?
			[type the number of your selection]
						
			%s
			....>\s""";

	public static void main(String[] args) throws IOException {
		assert SERVED_FILES != null;
		Arrays.sort(SERVED_FILES);
		//Replace placeholder with an ordered list menu of served files
		String selectionMenu = SELECTION_MENU_TEMPLATE.formatted(menuOfFiles(SERVED_FILES));

		var socket = new DatagramSocket(1999);
		//As input will never be more than 1K, a buffer of 1K is used
		//whenever receiving data from client.
		var receiveBuffer = new byte[1024];

		System.out.println("Server started.");
		while (true) {
			System.out.println("\nWaiting to receive a packet...");
			var initPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			//receive() function is blocking, server will wait until a client sends any packet to it
			//The first packet from the client is considered as initiation packet and has no body
			socket.receive(initPacket);
			System.out.println("Received initiation packet, packetSize=" + initPacket.getLength());

			//Send selection menu as 1 packet
			byte[] menuBytes = selectionMenu.getBytes();
			var menuPacket = new DatagramPacket(menuBytes, menuBytes.length, initPacket.getSocketAddress());
			socket.send(menuPacket);

			//After sending the selection menu to the user, expect to hear back a selection response.
			//Prepare for reading

			//Read selection response from client
			var selectionResponse = new DatagramPacket(receiveBuffer, receiveBuffer.length);
			socket.receive(selectionResponse);
			//Response is a number, which client uses just 1 byte for.
			//Response value exists at first index of receiveBuffer.
			int selectionIdx = receiveBuffer[0] - 1; // Minus 1 because file list was 1-based
			String selectedFileName = SERVED_FILES[selectionIdx];
			System.out.println("Client chose \"" + selectedFileName + "\"");

			//Prepare to send selected file
			File selectedFile = new File(ASSETS_PATH + "/" + selectedFileName);
			var fileInputStream = new FileInputStream(selectedFile);
			var bufferedInputStream = new BufferedInputStream(fileInputStream);
			System.out.println("Sending file...");

			var encrypter = new VigenereEncrypter(VigenereCipher.DEFAULT_KEY);
			byte[] fileBuffer = new byte[512];
			int count;
			//Read the file block by block, encrypt it and send the packet to client
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