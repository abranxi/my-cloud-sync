/*
 * Copyright 2012 Alfresco Software Limited.
 * [OTHER COPYRIGHT NOTICES]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file is part of an unsupported extension to Alfresco.
 */

package com.alfresco.jmycloudclient.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SyncViewImpl extends JFrame implements SyncView {

	private static final String PASSWORD_LABEL_TEXT = "Password";
	private static final String EMAIL_LABEL_TEXT = "Email";
	private static final String SYNC_BUTTON_TEXT = "Sync";
	private static final int WINDOW_WIDTH = 350;
	private static final int WINDOW_HEIGTH = 200;
	private static final long serialVersionUID = -9099249695102885588L;
	
	private final JPanel mainPanel;
	
	private final JPanel syncPanel;
	private final JTextField userNameField;
	private final JPasswordField passwordField;
	private final JButton syncButton;
	private final JLabel statusLabel;
	
	public SyncViewImpl() {
		syncPanel = new JPanel();
		userNameField = new JTextField(20);
		passwordField = new JPasswordField(20);
		syncButton = new JButton(SYNC_BUTTON_TEXT);
		statusLabel = new JLabel();
		
		setupSyncPanel();
		
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		ImageIcon logo = new ImageIcon(getClass().getResource("/com/alfresco/jmycloudclient/view/logo.png"));
		mainPanel.add(new JLabel(logo), BorderLayout.NORTH);
		mainPanel.add(syncPanel, BorderLayout.CENTER);
		setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGTH);
        setResizable(false);
        setVisible(true);
	}

	private void setupSyncPanel() {
		syncPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		syncPanel.add(new JLabel(EMAIL_LABEL_TEXT));
		syncPanel.add(userNameField);
		syncPanel.add(new JLabel(PASSWORD_LABEL_TEXT));
		syncPanel.add(passwordField);
		syncPanel.add(statusLabel);
		syncPanel.add(syncButton);
	}

	@Override
	public void addSyncButtonHandler(final ClickHandler handler) {
		syncButton.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			
			@Override
			public void mousePressed(MouseEvent arg0) {}
			
			@Override
			public void mouseExited(MouseEvent arg0) {}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				handler.onClick(arg0);
			}
		});
	}

	@Override
	public String getEmail() {
		return userNameField.getText();
	}

	@Override
	public String getPassword() {
		return new String(passwordField.getPassword());
	}

	@Override
	public void enableLogin(boolean enable) {
		syncButton.setEnabled(enable);
		userNameField.setEnabled(enable);
		passwordField.setEnabled(enable);
	}

	@Override
	public void setStatusMessage(String status) {
		if(status.length() > 35) {
			status = status.substring(0, 29) + "...";
		}
		statusLabel.setText(status);
	}
}
