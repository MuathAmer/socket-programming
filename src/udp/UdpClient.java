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

public class UdpClient {
	public static void main(String[] args) throws IOException {
		var socket = new DatagramSocket();
		var address = new InetSocketAddress(InetAddress.getLocalHost(), 1999);
		var receiveBuffer = new byte[1024];
		socket.setSoTimeout(100);

		//send empty packet for initiation
		var datagramPacket = new DatagramPacket(new byte[0], 0, address);
		socket.send(datagramPacket);

		var selectionMenuPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
		socket.receive(selectionMenuPacket);

		String selectionMenu = new String(
				selectionMenuPacket.getData(),
				selectionMenuPacket.getOffset(),
				selectionMenuPacket.getLength()
		);
		System.out.println("Server says:\n");
		System.out.print(selectionMenu);

		//send selection packet
		var scanner = new Scanner(System.in);
		var line = scanner.nextLine();
		byte selection = Byte.parseByte(line);
		byte[] selectionByte = new byte[]{selection};
		var selectionPacket = new DatagramPacket(selectionByte, 1, address);
		socket.send(selectionPacket);

		//prepare for receiving file
		var fileOutputStream = new FileOutputStream("output.txt");
		var bufferedFileOut = new BufferedOutputStream(fileOutputStream);

		System.out.println("Receiving file...");
		var decrypter = new VigenereDecrypter(VigenereCipher.DEFAULT_KEY);
		byte[] fileBuffer = new byte[512]; // buffer of 0.5kb, to support very large files and increase performance
		try {
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