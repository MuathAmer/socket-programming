package udp;

import util.VigenereCipher;
import util.VigenereDecrypter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/* ALERT: This client requests files and receives them through UDP,
 * which is not safe and doesn't guarantee packets delivery or order.
 * This use case of UDP is for educational purposes only.
 *
 * This class works as a UdpClient and is supposed to work with its UdpServer.
 * It uses the UDP protocol using java.net.DatagramSocket and java.net.DatagramPacket,
 * this class receives data packet by packet from the server and deals with them.
 *
 * This client assumes a server is already running and waiting for it,
 * as UDP is connectionless, the client sends an initiation packet to setup the connection,
 * it then receives from the server a list of files that can be requested, and will wait
 * for the user to write their selection through standard input (console).
 * Once the user writes a selection, the client will send the selection number to
 * the server, and start receiving this requested file.
 * This client will receive the file block by block, each in a DatagramPacket
 * until the file is totally received. Notice that the file will be encrypted
 * and this client will need to decrypt it using the same key.
 * Simple Vigenere cipher is used for encryption.
 * */

public class UdpClient {
	public static void main(String[] args) throws IOException {
		var socket = new DatagramSocket();
		//The same address is used for all outgoing DatagramPackets.
		var address = new InetSocketAddress(InetAddress.getLocalHost(), 1999);
		var receiveBuffer = new byte[1024];
		socket.setSoTimeout(100); //If server stops sending for 100ms, then issue a signal (Exception)

		//Send empty packet for initiation
		var datagramPacket = new DatagramPacket(new byte[0], 0, address);
		socket.send(datagramPacket);

		//Receive the selection menu in 1 packet.
		var selectionMenuPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		socket.receive(selectionMenuPacket);

		String selectionMenu = new String(
				selectionMenuPacket.getData(),
				selectionMenuPacket.getOffset(),
				selectionMenuPacket.getLength()
		);
		System.out.println("Server says:\n");
		System.out.print(selectionMenu);

		//Ask user to input their selection
		var scanner = new Scanner(System.in);
		var line = scanner.nextLine();
		//Send user's selection to server
		byte selection = Byte.parseByte(line);
		byte[] selectionByte = new byte[]{selection};
		var selectionPacket = new DatagramPacket(selectionByte, 1, address);
		socket.send(selectionPacket);

		//Prepare for receiving file
		var fileOutputStream = new FileOutputStream("output.txt");

		System.out.println("Receiving file...");
		var decrypter = new VigenereDecrypter(VigenereCipher.DEFAULT_KEY);
		byte[] fileBuffer = new byte[512]; // buffer, 500B per 1 packet when sending file
		//Try to read from the server until SoTimeout exception (~Server stopped sending packets)
		try {
			//Receive 1 block per packet, decrypt it and write it to output file.
			while (true) {
				var partOfFile = new DatagramPacket(fileBuffer, fileBuffer.length);
				socket.receive(partOfFile);
				decrypter.decryptData(fileBuffer);
				fileOutputStream.write(
						fileBuffer,
						partOfFile.getOffset(),
						partOfFile.getLength()
				);
			}
		} catch (Exception ignored) {
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