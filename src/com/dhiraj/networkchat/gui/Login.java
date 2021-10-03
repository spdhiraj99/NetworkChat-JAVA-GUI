package com.dhiraj.networkchat.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField nameInput;
	private JTextField ipAddr;
	private JLabel ipAddrLabel;
	private JTextField port;
	private JLabel portLabel;
	private JLabel lbPortDesc;
	private JLabel lbIpDesc;

	public Login() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(Login.class.getResource("/com/dhiraj/networkchat/gui/icon.png")));

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setResizable(false);
		setTitle("Login to chat!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 380);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		nameInput = new JTextField();
		nameInput.setBounds(52, 64, 191, 20);
		contentPane.add(nameInput);
		nameInput.setColumns(10);

		JLabel nameLabel = new JLabel("Name:");
		nameLabel.setFont(new Font("Courier New", Font.PLAIN, 14));
		nameLabel.setBounds(126, 45, 44, 20);
		contentPane.add(nameLabel);

		ipAddr = new JTextField();
		ipAddr.setColumns(10);
		ipAddr.setBounds(52, 109, 191, 20);
		contentPane.add(ipAddr);

		ipAddrLabel = new JLabel("IP Address:");
		ipAddrLabel.setFont(new Font("Courier New", Font.PLAIN, 14));
		ipAddrLabel.setBounds(99, 92, 97, 20);
		contentPane.add(ipAddrLabel);

		port = new JTextField();
		port.setColumns(10);
		port.setBounds(52, 181, 191, 20);
		contentPane.add(port);

		portLabel = new JLabel("Port:");
		portLabel.setFont(new Font("Courier New", Font.PLAIN, 14));
		portLabel.setBounds(122, 163, 51, 20);
		contentPane.add(portLabel);

		lbPortDesc = new JLabel("(eg: 9090)");
		lbPortDesc.setFont(new Font("Courier New", Font.PLAIN, 14));
		lbPortDesc.setBounds(104, 198, 88, 20);
		contentPane.add(lbPortDesc);

		lbIpDesc = new JLabel("(eg: 192.168.1.1)");
		lbIpDesc.setFont(new Font("Courier New", Font.PLAIN, 14));
		lbIpDesc.setBounds(73, 133, 150, 20);
		contentPane.add(lbIpDesc);

		JButton loginBtn = new JButton("Login");
		loginBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = nameInput.getText();
				String ipString = ipAddr.getText();
				Integer p = Integer.parseInt(port.getText());
				login(name, ipString, p);
			}
		});
		loginBtn.setBounds(105, 260, 85, 21);
		contentPane.add(loginBtn);
	}

	private void login(String name, String ip, Integer p) {
		dispose();
		new ClientWindow(name,ip,p);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
