package com.dhiraj.networkchat.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server implements Runnable {

	private Integer port;

	private ServerSocket serverSocket;

	private Thread serverThread, manage;

	private boolean running = false;

	private ArrayList<Socket> clients;

	private HashMap<String, String> clientDetails;

	private boolean debug = false;

	public Server(Integer port) {
		this.port = port;
		clients = new ArrayList<Socket>();
		clientDetails = new HashMap<String, String>();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		serverThread = new Thread(this, "ServerThread");
		serverThread.start();
	}

	@Override
	public void run() {
		running = true;
		System.out.println("Server Started on port : " + port);
		manageClient();
		Scanner sc = new Scanner(System.in);
		while (running) {
			String cmd = sc.nextLine();
			if (cmd.length() > 1 && !cmd.startsWith("/")) {
				String msg = "Server: " + cmd;
				sendToAll(msg, null);
				continue;
			}
			if (!cmd.isBlank()) {
				cmd = cmd.substring(1);
				if (cmd.toUpperCase().equals("DEBUG ON")) {
					debug = true;
				} else if (cmd.toUpperCase().equals("DEBUG OFF")) {
					debug = false;
				} else if (cmd.toUpperCase().equals("LIST")) {
					dispClientlist();
				} else if (cmd.toUpperCase().startsWith("KICK")) {
					String name = cmd.split(" ")[1];
					String ipPort = "";
					boolean found = false;
					for (Map.Entry<String, String> entry : clientDetails.entrySet()) {
						if (name.equals(entry.getValue())) {
							ipPort = entry.getKey();
							found = true;
							break;
						}
					}

					if (!found) {
						System.out.println("user not found, Syntax for kick is ");
						System.out.println("/kick <username>");
						System.out.println("you can get username by running /list command");
						continue;
					}

					for (int i = 0; i < clients.size(); i++) {
						String ipPortC = clients.get(i).getInetAddress().toString() + ":" + clients.get(i).getPort();
						if (ipPortC.equals(ipPort)) {
							disconnect(clients.get(i), name);
							clients.remove(i);
							clientDetails.remove(ipPortC);
						}
					}
					System.out.println("Kicking out " + name + " (" + ipPort + ") from the server");
				} else if (cmd.toUpperCase().equals("QUIT")) {
					quit();
				} else if (cmd.toUpperCase().equals("HELP") || cmd.equals("?")) {
					printHelp();
				} else {
					System.out.println("Unknown Command! ");
					printHelp();
				}
				continue;
			}
		}
		sc.close();
	}

	private void printHelp() {
		System.out.println("List of available commands: ");
		System.out.println("/DEBUG [ON/OFF] -- turn on debug statements");
		System.out.println("/list -- list connected clients");
		System.out.println("/kick [username] -- kick a user");
		System.out.println("/quit -- close server");
		System.out.println("/help or /? -- print help");
	}

	private void dispClientlist() {
		System.out.println("Connected Clients: ");
		System.out.println("----------------------------------------------------");
		for (int i = 0; i < clients.size(); i++) {
			String key = clients.get(i).getInetAddress().toString() + ":" + clients.get(i).getPort();
			System.out.println(i + 1 + ")" + key + ", User name: " + clientDetails.get(key));
		}
		System.out.println("----------------------------------------------------");
	}

	private void disconnect(Socket socket, String name) {
		try {
			sendToAll("Server: " + name + " has been kicked out", socket);
			send("Server: you have been kicked out", socket);
			if (socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void manageClient() {
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
					Socket conn;
					try {
						if (debug)
							System.out.println(clients.size());
						conn = serverSocket.accept();
						if (conn != null) {
							System.out.println("Connection Received from " + conn.getInetAddress().toString());
							clients.add(conn);
							Thread t = new Thread() {
								public void run() {
									recv(conn);
								}
							};
							Thread t1 = new Thread() {
								public void run() {
									sendStatus(conn);
								}
							};
							t.start();
							t1.start();
						}
					} catch (SocketException se) {
					} catch (IOException e) {
						e.printStackTrace();
					}
					clients.removeIf(s -> s == null || s.isClosed() || s.isInputShutdown() || s.isOutputShutdown());
				}
			}
		};
		manage.start();

	}

	private void sendStatus(Socket conn) {
		try {
			while (running) {
				if (clients.size() <= 0)
					return;
				String msg = "/u/";
				for (int i = 0; i < clients.size() - 1; i++) {
					String key = clients.get(i).getInetAddress().toString() + ":" + clients.get(i).getPort();
					msg += clientDetails.get(key) + "/n/";
				}
				if (clients.get(clients.size() - 1).isClosed())
					continue;
				String lastKey = clients.get(clients.size() - 1).getInetAddress().toString() + ":"
						+ clients.get(clients.size() - 1).getPort();
				msg += clientDetails.get(lastKey) + "/e/";
				if (!msg.equals("/u/null/e/") && !msg.isBlank())
					sendToAll(msg, null);
				Thread.sleep(500);
			}
		} catch (Exception e) {
			if (debug)
				System.err.println("Error in connection");
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			for (int i = 0; i < clients.size(); i++) {
				String ipPortC = clients.get(i).getInetAddress().toString() + ":" + clients.get(i).getPort();
				String ipPort = conn.getInetAddress().toString() + ":" + conn.getPort();
				if (ipPortC.equals(ipPort)) {
					clients.remove(i);
					clientDetails.remove(ipPortC);
				}
			}
		}
	}

	private void recv(Socket conn) {
		try {
			InputStream iStream = conn.getInputStream();
			BufferedInputStream biStream = new BufferedInputStream(iStream);
			while (running) {
				if (conn != null && !conn.isClosed() && conn.isConnected()) {
					int len = Integer.parseInt(new String(biStream.readNBytes(5)));
					String msg = new String(biStream.readNBytes(len));
					if (debug)
						System.out.println(msg);
					if (msg.contains("/c/")) {
						String key = conn.getInetAddress().toString() + ":" + conn.getPort();
						clientDetails.put(key, msg.substring(3));
						sendToAll("System: " + clientDetails.get(key) + " has joined the chat. Say Hello! ", conn);
					} else {
						sendToAll(msg, conn);
					}
				}
			}
		} catch (IOException e) {
			if (debug)
				System.err.println("Error in connection");
			String key = conn.getInetAddress().toString() + ":" + conn.getPort();
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (clients.size() > 1) {
				sendToAll("System: " + clientDetails.get(key) + " has Left the chat.", conn);
			}
			String ipPort = conn.getInetAddress().toString() + ":" + conn.getPort();
			for (int i = 0; i < clients.size(); i++) {
				String ipPortC = clients.get(i).getInetAddress().toString() + ":" + clients.get(i).getPort();
				if (ipPortC.equals(ipPort)) {
					clients.remove(i);
					clientDetails.remove(ipPortC);
				}
			}
		}
	}

	private void sendToAll(String msg, Socket origSndr) {
		for (Socket s : clients) {
			if (s != null && s != origSndr && !s.isClosed()) {
				Thread t = new Thread() {
					public void run() {
						send(msg, s);
					}
				};
				t.start();
			}
		}
	}

	private void send(String msg, Socket conn) {
		if (conn != null && !conn.isClosed()) {
			try {
				OutputStream oStream = conn.getOutputStream();
				BufferedOutputStream obStream = new BufferedOutputStream(oStream);
				msg = String.format("%05d", msg.length()) + msg;
				obStream.write(msg.getBytes());
				obStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void quit() {
		try {
			running = false;
			for (int i = 0; i < clients.size(); i++) {

				clients.get(i).close();

			}
			System.out.println("All Connections closed!");
			serverSocket.close();
			System.out.println("Server listening on port " + port + " closed!");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Quitting the Server....");

	}

}
