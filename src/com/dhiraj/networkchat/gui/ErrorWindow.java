package com.dhiraj.networkchat.gui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ErrorWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public ErrorWindow() {
		setResizable(false);
		setTitle("Error!");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ErrorWindow.class.getResource("/com/dhiraj/networkchat/gui/icon.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400,350);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Error Occured while connecting to server !");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblNewLabel.setBounds(75, 75, 292, 13);
		contentPane.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("Login Again");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Login login = new Login();
				login.setVisible(true);
				dispose();
				return;
			}
		});
		btnNewButton.setBounds(162, 162, 85, 21);
		contentPane.add(btnNewButton);
	}
}
