package com.dhiraj.networkchat.server;

public class ServerMain {

	private Integer port;
	private Server server;

	public ServerMain(Integer port) {
		this.port = port;
		server = new Server(this.port);
	}

	public static void main(String[] args) {
		Integer port;
		if (args.length > 1) {
			System.out.println("Usage: java -jar ChatServer.jar [port]");
			return;
		}
		if (args.length == 0) {
			port = 9090;
		} else {
			port = Integer.parseInt(args[0]);
		}
		new ServerMain(port);
	}

}
