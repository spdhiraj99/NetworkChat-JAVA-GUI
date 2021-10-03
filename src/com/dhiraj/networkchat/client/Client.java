package com.dhiraj.networkchat.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	private String name, ipAddr;
	private Integer port;
	private Socket socket;
	private InetAddress inetAddr;

	public Client(String name, String ipAddr, Integer port) {
		this.name = name;
		this.ipAddr = ipAddr;
		this.port = port;
	}

	public boolean openConnection(String ipAddr, Integer port) {
		try {
			inetAddr = InetAddress.getByName(ipAddr);
			socket = new Socket(inetAddr, port);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public String recv() {
		String rMsg = "";
		if ((socket == null || socket.isClosed()) && !openConnection(ipAddr, port)) {
			System.err.println("Connection failed!");
		}
		try {
			if (socket != null && socket.getInputStream().available() > 0) {
				InputStream iStream = socket.getInputStream();
				BufferedInputStream biStream = new BufferedInputStream(iStream);
				int len = Integer.parseInt(new String(biStream.readNBytes(5)));
				rMsg = new String(biStream.readNBytes(len));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rMsg;
	}

	public boolean send(String msg) {
		if ((socket == null || socket.isClosed()) && !openConnection(ipAddr, port)) {
			System.err.println("Connection failed!");
		}
		OutputStream oStream;
		try {
			oStream = socket.getOutputStream();
			BufferedOutputStream boStream = new BufferedOutputStream(oStream);
			msg = String.format("%05d", msg.length()) + msg;
			boStream.write(msg.getBytes());
			boStream.flush();
		} catch (IOException e) {
			System.err.println("Error Occured in socket connection");
			return false;
		}
		return true;
	}

	public String getName() {
		return name;
	}

	public Integer getPort() {
		return port;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public boolean isConnected() {
		return !(socket == null || socket.isClosed());
	}


}
