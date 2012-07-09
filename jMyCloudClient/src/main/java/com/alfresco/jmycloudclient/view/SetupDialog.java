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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.manager.AppProperties;
import com.alfresco.jmycloudclient.manager.SyncManager;
import com.alfresco.jmycloudclient.manager.UserPreferences;
import com.alfresco.jmycloudclient.view.i18n.I18N;

/**
 * About Dialog for client with details on Alfresco/Authors
 * 
 * @author dgildeh
 *
 */
public class SetupDialog extends JFrame {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SetupDialog.class);

	private static final long serialVersionUID = 2532336564610002030L;
	
	// Default dialog sizes
	private static final int frameSizeX = 350;
	private static final int frameSizeY = 500;
	
	// SingleTon
	private static SetupDialog window;
	
	// CardPanel IDs
	private static final String GUEST_OPTIONS = "GuestOptions";
	private static final String SETUP_OPTIONS = "SetupOptions";
	
	// Dialog Components
	private final JLabel statusMsgLabel;
	private final JTextField emailField;
	private final JPasswordField passwordField;
	private final JButton loginButton;
	private final JPanel cardPanel;
	private final JComboBox selectNetworkField;
	private final JComboBox selectSiteField;
	private final JTextField localFolderPathTextField;
	
	/**
	 * Private constructor uses SingleTon pattern to display window
	 */
	private SetupDialog() {
		
		// Setup Dialog Settings
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - frameSizeX) / 2,
				(screenSize.height - frameSizeY) / 2);
		this.setSize(frameSizeX, frameSizeY);
		this.setResizable(false);
		this.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		this.setAlwaysOnTop(true);
		
		// Add Alfresco Logo at top of form
		ImageIcon logo = new ImageIcon(getClass().getResource("/com/alfresco/jmycloudclient/view/logo.png"));
		JLabel topLogo = new JLabel(logo);
		topLogo.setBorder(new EmptyBorder( 5, 5, 5, 5 ));
		topLogo.setAlignmentX(CENTER_ALIGNMENT);
		this.add(topLogo);
		
		// Add help text
		JLabel loginText = new JLabel("<html>" + I18N.getString("setup.help.html") + "</html>");	
		loginText.setBorder(new EmptyBorder( 5, 5, 5, 5 ));
		loginText.setAlignmentX(CENTER_ALIGNMENT);
		this.add(loginText);
		
		// Add status message area, initially hidden but will display red text
		// if set and set to visible
		statusMsgLabel = new JLabel("");
		statusMsgLabel.setForeground(Color.RED);
		statusMsgLabel.setVisible(false);
		statusMsgLabel.setBorder(new EmptyBorder( 5, 5, 5, 5 ));
		statusMsgLabel.setAlignmentX(CENTER_ALIGNMENT);
		this.add(statusMsgLabel);
		
		/***************** Login Form Layout *****************/
		
		// Add Email Field
		JPanel emailPanel = new JPanel();
		emailPanel.setMaximumSize(new Dimension(frameSizeX, 20));
		emailPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));	
		JLabel emailFieldLabel = new JLabel(I18N.getString("setup.email.label"));
		emailFieldLabel.setBorder(new EmptyBorder( 5, 5, 5, 5 ));
		emailPanel.add(emailFieldLabel);
		emailField = new JTextField(20);
		emailPanel.add(emailField);
		this.add(emailPanel);
		
		// Add Password Field
		JPanel passwordPanel = new JPanel();
		passwordPanel.setMaximumSize(new Dimension(frameSizeX, 20));
		passwordPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JLabel passwordFieldLabel = new JLabel(I18N.getString("setup.password.label"));
		passwordFieldLabel.setBorder(new EmptyBorder( 5, 5, 5, 5 ));
		passwordPanel.add(passwordFieldLabel);
		passwordField = new JPasswordField(20);
		passwordPanel.add(passwordField);
		this.add(passwordPanel);
		
		// Add Login Button
		JPanel loginPanel = new JPanel();
		loginPanel.setMaximumSize(new Dimension(frameSizeX, 20));
		loginPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));	
		loginButton = new JButton(I18N.getString("setup.loginButton.label"));
		loginPanel.add(loginButton);
		this.add(loginPanel);
		
		/***************** Card Panel Layout *****************/
		
		// Add CardPanel which will switch between Guest Options & Setup Options forms
		// Depending on state of form
		cardPanel = new JPanel(new CardLayout());
		
		/***************** Guest Options Form Layout *****************/
		
		// Create Forgotten Password / Registration Button shown before user successfully logs in
		JPanel guestOptions = new JPanel();
		guestOptions.setLayout(new BoxLayout(guestOptions, BoxLayout.Y_AXIS));
		JButton forgottenPasswordButton = new JButton(I18N.getString("setup.forgotPasswordButton.label"));
		forgottenPasswordButton.setAlignmentX(CENTER_ALIGNMENT);
		forgottenPasswordButton.setMaximumSize(new Dimension(frameSizeX, 30));
		forgottenPasswordButton.addActionListener(new ActionListener() {	  
			public void actionPerformed(ActionEvent evt) {
				SystemTrayIcon.openURL(AppProperties.FORGOT_PASSWORD_URL);
			}		
		});
		guestOptions.add(forgottenPasswordButton);
		JButton registerButton = new JButton(I18N.getString("setup.registerButton.label"));
		registerButton.setAlignmentX(CENTER_ALIGNMENT);
		registerButton.setMaximumSize(new Dimension(frameSizeX, 30));
		registerButton.addActionListener(new ActionListener() {	  
			public void actionPerformed(ActionEvent evt) {
				SystemTrayIcon.openURL(AppProperties.SIGNUP_URL);
			}		
		});
		guestOptions.add(registerButton);
		
		cardPanel.add(guestOptions, GUEST_OPTIONS);
		
		/***************** Setup Options Form Layout *****************/
		
		// Create Setup Options shown after successful login to select Network/Site to sync
		JPanel setupOptions = new JPanel();
		setupOptions.setLayout(new BoxLayout(setupOptions, BoxLayout.Y_AXIS));
		
		// Select Network field
		JPanel selectNetworkPanel = new JPanel();
		selectNetworkPanel.setMaximumSize(new Dimension(frameSizeX, 30));
		selectNetworkPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectNetworkPanel.add(new JLabel(I18N.getString("setup.selectnetwork.label")));
		String[] networkOptions = { I18N.getString("setup.comboBox.default") };
		selectNetworkField = new TruncatedComboBox(networkOptions);
		selectNetworkField.setEnabled(false);
		selectNetworkField.setEditable(false);
		selectNetworkPanel.add(selectNetworkField);
		setupOptions.add(selectNetworkPanel);
		
		// Select Site field
		JPanel selectSitePanel = new JPanel();
		selectSitePanel.setMaximumSize(new Dimension(frameSizeX, 30));
		selectSitePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectSitePanel.add(new JLabel(I18N.getString("setup.selectsite.label")));
		String[] siteOptions = { I18N.getString("setup.comboBox.default") };
		selectSiteField = new TruncatedComboBox(siteOptions);
		selectSiteField.setEnabled(false);
		selectSiteField.setEditable(false);
		selectSitePanel.add(selectSiteField);
		setupOptions.add(selectSitePanel);
		
		// Select local sync folder setup
		JPanel selectFolderPanel = new JPanel();
		selectFolderPanel.setMaximumSize(new Dimension(frameSizeX, 40));
		selectFolderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		selectFolderPanel.add(new JLabel(I18N.getString("setup.selectLocalFolderPath.label")));
		localFolderPathTextField = new JTextField(13);
		localFolderPathTextField.setEditable(false);
		// Setup Default Folder Path of chooser to selected path if set
		File defaultDir;
		final JFileChooser localFolderPathField;
		if (UserPreferences.getUserPref(UserPreferences.SYNC_LOCAL_FOLDER_PATH) != null) {
			defaultDir = new File(UserPreferences.getUserPref(UserPreferences.SYNC_LOCAL_FOLDER_PATH));
			localFolderPathField = new JFileChooser(defaultDir);
		} else {
			localFolderPathField = new JFileChooser();
		}	
		localFolderPathField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		localFolderPathField.setMultiSelectionEnabled(false);
		final JButton browseButton = new JButton(I18N.getString("setup.browseButton.label"));
		browseButton.addActionListener(new ActionListener() {	  
			public void actionPerformed(ActionEvent evt) {
				localFolderPathField.showDialog(browseButton, I18N.getString("setup.fileChooser.select"));
			}		
		});
		selectFolderPanel.add(localFolderPathTextField);
		selectFolderPanel.add(browseButton);
		setupOptions.add(selectFolderPanel);
		
		// Add Save Button to start syncing
		JButton saveButton = new JButton(I18N.getString("setup.saveButton.label"));
		saveButton.setAlignmentX(CENTER_ALIGNMENT);
		saveButton.setMaximumSize(new Dimension(frameSizeX, 50));
		setupOptions.add(saveButton);
		
		cardPanel.add(setupOptions, SETUP_OPTIONS);
		
		// Add the cardPanel 
		this.add(cardPanel);
		
		// Form Layout Complete //
		
		/***************** Setup Form Behaviour *****************/
		
		// Add Login Button Click Handling
		loginButton.addActionListener(new ActionListener() {	  
			public void actionPerformed(ActionEvent evt) {
				
				if (loginButton.getText().equals(I18N.getString("setup.loginButton.label"))) {
					
					if (SyncManager.validateLoginCredentials(emailField.getText(), 
							new String(passwordField.getPassword()))) {
						setLoginValidated(true);
						setStatusMsg(null);
					} else {
						setStatusMsg(I18N.getString("error.cannotConnectRemote.html"));
					}
				} else {
					resetLoginForm(true);
				}
			}		
		});
		
		// Add File Chooser Select Action Handling
		localFolderPathField.addActionListener(new ActionListener() {	  
			public void actionPerformed(ActionEvent evt) {		
				if (evt.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
					
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Folder Selected: " + localFolderPathField.getSelectedFile().getAbsolutePath());
					}
					String folderPath = localFolderPathField.getSelectedFile().getAbsolutePath();
					localFolderPathTextField.setText(folderPath);
				}
			}		
		});
		
		// Add Save Button Click Handling
		saveButton.addActionListener(new ActionListener() {	  
			public void actionPerformed(ActionEvent evt) {
				
				// Validate Fields
				if ((selectNetworkField.getSelectedItem() != null) && 
						(! ((String)selectNetworkField.getSelectedItem()).equals(I18N.getString("setup.comboBox.default"))) &&
						(selectSiteField.getSelectedItem() != null) && 
						(! ((String)selectSiteField.getSelectedItem()).equals(I18N.getString("setup.comboBox.default"))) &&
						(localFolderPathTextField.getText() != null) && (! localFolderPathTextField.getText().isEmpty())) {
					
					// Save the selected Network to the User Preferences
					UserPreferences.saveUserPref(UserPreferences.SYNC_NETWORK, (String)selectNetworkField.getSelectedItem());
					// Save the selected Site to the User Preferences
					UserPreferences.saveUserPref(UserPreferences.SYNC_SITE, (String)selectSiteField.getSelectedItem());
					// Save the selected Site to the User Preferences
					UserPreferences.saveUserPref(UserPreferences.SYNC_LOCAL_FOLDER_PATH, (String)localFolderPathTextField.getText());
					
					// Start Sync & Close Window
					if (SyncManager.validateAllUserPrefs()) {
						SyncManager.startSync();
						window.setVisible(false);
					} else {
						LOGGER.error("Cannot Start Sync: Not all user preferences have been set!");
					}
				} else {
					setStatusMsgLabel(I18N.getString("setup.validationFailed.status"));
				}
			}		
		});
		
		// If User Preferences Set, setup field values - we do this before adding ActionListeners to 
		// combo boxes or when we set the field options on the combo boxes it will fire change events
		// causing errors
		if (SyncManager.validateAllUserPrefs()) {
			
			// Set text field values
			emailField.setText(UserPreferences.getUserPref(UserPreferences.LOGIN_EMAIL,""));
			passwordField.setText(UserPreferences.getUserPref(UserPreferences.LOGIN_PASSWORD,""));
			localFolderPathTextField.setText(UserPreferences.getUserPref(UserPreferences.SYNC_LOCAL_FOLDER_PATH, ""));
			
			// Show Setup Options form and set Login button to 'Reset'
			setLoginValidated(false);
						
			// Setup Combo Boxes
			selectNetworkField.setSelectedItem(UserPreferences.getUserPref(UserPreferences.SYNC_NETWORK));
			clearComboBox(selectSiteField, SyncManager.getSites(UserPreferences.getUserPref(UserPreferences.SYNC_NETWORK)));
			selectSiteField.setSelectedItem(UserPreferences.getUserPref(UserPreferences.SYNC_SITE));
			selectSiteField.setEnabled(true);
			
		}
		
		// Add Select Network Field Change Handler
		selectNetworkField.addActionListener(new ActionListener() {	  
			public void actionPerformed(ActionEvent evt) {

				if ((selectNetworkField.getSelectedItem() != null) && 
						(! ((String)selectNetworkField.getSelectedItem()).equals(I18N.getString("setup.comboBox.default"))) &&
						selectNetworkField.isEnabled()) {
					
					String selectedNetwork = (String) selectNetworkField.getSelectedItem();
					
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Network Selected: " + selectedNetwork);
					}
					
					// Load Select Site Combo List
					selectSiteField.setEnabled(false);
					clearComboBox(selectSiteField, SyncManager.getSites(selectedNetwork));
					selectSiteField.setEnabled(true);
				}	
			}		
		});
	}
	
	/**
	 * Setups form with disabled login and reset button as login
	 * has been initialised. If save is true it will save credentials
	 * to User Properties on local machine
	 * 
	 * @param save	true - save credentials to user properties, false - just change UI
	 */
	private void setLoginValidated(boolean save) {
		
		if (save) {
			// Save user credentials and set Login_Validated to true
			UserPreferences.saveUserPref(UserPreferences.LOGIN_EMAIL, emailField.getText());
			UserPreferences.saveUserPref(UserPreferences.LOGIN_PASSWORD, new String(passwordField.getPassword()));
			UserPreferences.saveUserPref(UserPreferences.LOGIN_VALIDATED, true);
		}
		
		// Make Login fields non-editable
		emailField.setEditable(false);
		passwordField.setEditable(false);
		
		// Change Login Button to 'Reset'
		loginButton.setText(I18N.getString("setup.resetButton.label"));
		
		// Load Select Network Combo List
		clearComboBox(selectNetworkField, SyncManager.getNetworks());
		selectNetworkField.setEnabled(true);
		
		// Switch bottom form to Setup Options
		switchCards(SETUP_OPTIONS);
	}
	
	/**
	 * Resets fields and clears all login credentials from the user properties
	 * if clear is set to true
	 * 
	 * @param clear		true - delete credentials from user properties, false - just change UI
	 */
	private void resetLoginForm(boolean clear) {
		
		if (clear) {
			// Delete all user credentials and set Login_Validated to false
			SyncManager.clearSettings();
		}
		
		// Make sure Login fields are editable and clear text
		emailField.setEditable(true);
		emailField.setText("");
		passwordField.setEditable(true);
		passwordField.setText("");
		
		// Change Login Button back to 'Login'
		loginButton.setText(I18N.getString("setup.loginButton.label"));
		
		// Reset Setup Options Form
		clearComboBox(selectNetworkField, null);
		clearComboBox(selectSiteField, null);
		selectSiteField.setEnabled(false);
		localFolderPathTextField.setText("");
		
		// Remove any status message set
		setStatusMsgLabel(null);
		
		// Switch bottom form to Guest Options
		switchCards(GUEST_OPTIONS);
	}
	
	/**
	 * Switches bottom cards between panels using card name
	 * specified with panel
	 * 
	 * @param cardName	Card name set when panel was added to layout, i.e. SetupPanel
	 */
	private void switchCards(String cardName) {
		CardLayout layout = (CardLayout)cardPanel.getLayout();
		layout.show(cardPanel, cardName);
	}
	
	/**
	 * Set the error status message at top of the form if required. Pass NULL
	 * for statusMsg to hide label completely
	 * 
	 * @param statusMsg		HTML status message, or NULL to hide message label completely
	 */
	public static void setStatusMsg(String statusMsg) {
		if (window == null)
			window = new SetupDialog();
		
		window.setStatusMsgLabel(statusMsg);
	}
	
	/**
	 * Set the error status message at top of the form if required. Pass NULL
	 * for statusMsg to hide label completely
	 * 
	 * @param statusMsgLabel	HTML status message, or NULL to hide message label completely
	 */
	private void setStatusMsgLabel(String statusMsg) {
		
		if (statusMsg == null) {
			statusMsgLabel.setText("");
			statusMsgLabel.setVisible(false);
		} else {
			statusMsgLabel.setText("<html>" + statusMsg + "</html>");
			statusMsgLabel.setVisible(true);
		}
	}
	
	/**
	 * Clears all items in ComboBox so previous options are not
	 * shown and adds new items if given. If new options are null it will
	 * not do anything
	 * 
	 * @param comboBox		The comboBox to clear
	 * @param newOptions	The new options to add, set NULL to not add any
	 */
	private void clearComboBox(JComboBox comboBox, String[] newOptions) {
		DefaultComboBoxModel model = (DefaultComboBoxModel)comboBox.getModel();
		model.removeAllElements();
		comboBox.addItem(I18N.getString("setup.comboBox.default"));
		
		if (newOptions != null) {		
			for (String item : newOptions) {
				comboBox.addItem(item);
			}
		}
	}
	
	/**
	 * Show the About Dialog, will create it if it doesn't exist
	 */
	public static void showWindow(){
		if (window == null)
			window = new SetupDialog();	
		
		window.setVisible(true);
	}
}
