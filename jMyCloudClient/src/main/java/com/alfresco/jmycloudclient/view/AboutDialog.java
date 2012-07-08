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
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.alfresco.jmycloudclient.view.i18n.I18N;

/**
 * About Dialog for client with details on Alfresco/Authors
 * 
 * @author dgildeh
 *
 */
public class AboutDialog extends JFrame {

	private static final long serialVersionUID = 8652893875376728019L;
	
	// Default dialog sizes
	private static final int frameSizeX = 350;
	private static final int frameSizeY = 200;
	
	private static AboutDialog window;
	
	/**
	 * Private constructor uses SingleTon pattern to display window
	 */
	private AboutDialog() {
		
		// Setup Dialog Settings
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - frameSizeX) / 2,
				(screenSize.height - frameSizeY) / 2);
		this.setSize(frameSizeX, frameSizeY);
		this.setResizable(false);
		this.setLayout(new BorderLayout());
		
		// Add Alfresco Logo
		ImageIcon logo = new ImageIcon(getClass().getResource("/com/alfresco/jmycloudclient/view/logo.png"));
		this.add(new JLabel(logo), BorderLayout.NORTH);
		
		// Add About Text
		JLabel aboutText = new JLabel("<html>" + I18N.getString("about.html") + "</html>");
		aboutText.setBorder(new EmptyBorder( 5, 5, 5, 5 ));
		this.add(aboutText, BorderLayout.CENTER);
	}
	
	/**
	 * Show the About Dialog, will create it if it doesn't exist
	 */
	public static void showWindow(){
		if (window == null)
			window = new AboutDialog();
		
		window.setVisible(true);
		window.toFront();
	}
}
