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

package com.alfresco.jmycloudclient;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.manager.SyncManager;
import com.alfresco.jmycloudclient.manager.UserPreferences;
import com.alfresco.jmycloudclient.view.SetupDialog;
import com.alfresco.jmycloudclient.view.SystemTrayIcon;

/**
 * Main entry point for application
 * 
 * @author dgildeh
 *
 */
public class AlfrescoSyncClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoSyncClient.class);
	
	/**
	 * Main Method initialises and starts main application. This application does
	 * not use any passed in arguments
	 * 
	 * @param args			Any arguments passed into application on start
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		
		// Launch UI
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				LOGGER.info("----------Alfresco Sync Started----------");	
				
				// Create System Tray Icon with Popup Menu
				SystemTrayIcon tray = new SystemTrayIcon();	
				
				// Show Setup Dialog Window if user settings haven't been saved and validated
				if (! UserPreferences.checkRequiredPreferencesSet()) {
					SetupDialog.showWindow();
				} else {
					SyncManager.startSync();
				}	
			}
		});
		
		// Start Sync Watches
		//SystemListener listener = new SystemListener();
	}
}
