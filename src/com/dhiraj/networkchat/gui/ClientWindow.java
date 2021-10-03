package com.dhiraj.networkchat.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import com.dhiraj.networkchat.client.Client;

public class ClientWindow extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField msgToSend;
	private JTextArea hist;
	private DefaultCaret caret;

	private Client client;
	private boolean running = false;
	private JMenuBar menuBar;
	private JMenu optionMenu;
	private JMenuItem optionsOnline;
	private JMenuItem optionExit;

	private OnlineUsers onlineUsers;

	public ClientWindow(String name, String ipAddr, Integer port) {
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(ClientWindow.class.getResource("/com/dhiraj/networkchat/gui/icon.png")));
		setTitle("Lets Chat !");
		client = new Client(name, ipAddr, port);
		createWindow();
		console("Attempting to connect to " + ipAddr + ":" + port + ", user: " + name);
		client.openConnection(ipAddr, port);
		if (client.isConnected()) {
			console("Connected to " + ipAddr + ": " + port);
			sendMsg("/c/" + name);
		} else {
			System.err.println("Couldn't connect!");
			dispose();
			ErrorWindow eW = new ErrorWindow();
			eW.setVisible(true);
			dispose();
			return;
		}
		onlineUsers = new OnlineUsers();
		running = true;
		new Thread(this, "Listner Runner Thread").start();
	}

	private void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(880, 550);
		setLocationRelativeTo(null);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		optionMenu = new JMenu("Options");
		menuBar.add(optionMenu);

		optionsOnline = new JMenuItem("Online users");
		optionsOnline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onlineUsers.setVisible(true);
			}
		});
		optionMenu.add(optionsOnline);

		optionExit = new JMenuItem("Exit");
		optionExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		optionMenu.add(optionExit);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 28, 815, 30, 7 };
		gbl_contentPane.rowHeights = new int[] { 25, 485, 40 };
		contentPane.setLayout(gbl_contentPane);

		hist = new JTextArea();
		hist.setEditable(false);
		JScrollPane scroll = new JScrollPane(hist);
		caret = (DefaultCaret) hist.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		GridBagConstraints scrollConstraint = new GridBagConstraints();
		scrollConstraint.insets = new Insets(0, 0, 5, 5);
		scrollConstraint.fill = GridBagConstraints.BOTH;
		scrollConstraint.gridx = 0;
		scrollConstraint.gridy = 0;
		scrollConstraint.gridwidth = 3;
		scrollConstraint.gridheight = 2;
		scrollConstraint.weightx = 1.0;
		scrollConstraint.weighty = 1.0;
		scrollConstraint.insets = new Insets(0, 5, 0, 0);
		contentPane.add(scroll, scrollConstraint);

		msgToSend = new JTextField();
		msgToSend.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMsg(msgToSend.getText());
				}
			}
		});
		GridBagConstraints gbc_msgToSend = new GridBagConstraints();
		gbc_msgToSend.insets = new Insets(0, 0, 0, 5);
		gbc_msgToSend.fill = GridBagConstraints.HORIZONTAL;
		gbc_msgToSend.gridx = 0;
		gbc_msgToSend.gridy = 2;
		gbc_msgToSend.gridwidth = 2;
		gbc_msgToSend.weightx = 1.0;
		contentPane.add(msgToSend, gbc_msgToSend);
		msgToSend.setColumns(10);

		JButton sendBtn = new JButton("Send");
		sendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMsg(msgToSend.getText());
			}
		});
		GridBagConstraints gbc_sendBtn = new GridBagConstraints();
		gbc_sendBtn.insets = new Insets(0, 0, 0, 5);
		gbc_sendBtn.gridx = 2;
		gbc_sendBtn.gridy = 2;
		gbc_sendBtn.weightx = 0.0;
		gbc_sendBtn.weighty = 0.0;
		contentPane.add(sendBtn, gbc_sendBtn);

		setVisible(true);
		msgToSend.requestFocusInWindow();
	}

	public void console(String txt) {
		hist.append(txt + "\n");
		hist.setCaretPosition(hist.getDocument().getLength());
	}

	private void sendMsg(String msg) {
		if (!client.isConnected()) {
			console("Disconnected from server!!!");
			return;
		}
		String name = "";
		if (msg == null || msg.isBlank()) {
			return;
		}
		if (client != null) {
			name = client.getName();
		}

		if (!msg.contains("/c/")) {
			msg = name + ": " + msg;
			console(msg);
		}
		if (!client.send(msg)) {

			console("Disconnected from server!");
		}
		if (!msg.contains("/c"))
			msgToSend.setText("");
	}

	public void listen() {
		new Thread("Listner Thread") {
			@Override
			public void run() {
				while (running) {
					String msg = client.recv();
					if (!msg.isBlank()) {
						if (msg.startsWith("/u/")) {
							String[] users = msg.split("/u/|/n/|/e/");
							onlineUsers.update(Arrays.copyOfRange(users, 1, users.length));
						} else {
							console(msg);
						}
					}
				}
			}
		}.start();
	}

	@Override
	public void run() {
		listen();
	}
}
