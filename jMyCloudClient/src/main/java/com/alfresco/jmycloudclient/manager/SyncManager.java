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

package com.alfresco.jmycloudclient.manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.filestore.FileStore;
import com.alfresco.jmycloudclient.filestore.LocalFileStore;
import com.alfresco.jmycloudclient.filestore.RemoteFileStore;
import com.alfresco.jmycloudclient.model.Resource;
import com.alfresco.jmycloudclient.view.SetupDialog;
import com.alfresco.jmycloudclient.view.SystemTrayIcon;
import com.alfresco.jmycloudclient.view.i18n.I18N;

/**
 * Main class to control synchronisation between local folder
 * and remote server
 * 
 * @author dgildeh
 *
 */
public class SyncManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncManager.class);
	
	private static final int ONE_GB = 1073741824;
	
	// SingleTon Instance
	private static SyncManager syncManager = null;

	private final Map<String, String> globalExceptions;
	
	// Remote Disk File Store
	private FileStore remoteDisk = null;
	// Local Disk File Store
	private FileStore localDisk = null;
	
	// Initialisation flag, if false class isn't properly initialised ready for sync
	private boolean isInitialised = false;

	/**
	 * Private Constructor for SingleTon pattern
	 */
	private SyncManager() {

		// Load file type exceptions to ignore during sync
		this.globalExceptions = getGlobalExceptions();
		// Initialise SyncManager
		init();
	}
	
	/**
	 * Checks all settings are set and connections are valid. If not will show Setup Dialog
	 * with error message. If they are all valid it will initialise the file stores ready for
	 * sync
	 */
	private void init() {
		
		LOGGER.info("Initialising SyncManager...");
		
		// Check that we have all necessary user preferences set - if not show Setup Dialog
		if (! UserPreferences.checkRequiredPreferencesSet()) {						
			LOGGER.info("User Properties Missing, Opening Setup Dialog...");
			SetupDialog.showWindow();
		} else {
					
			LOGGER.info("User Properties Found, Initialising File Stores with following settings: HTTP_PROTOCOL: " + AppProperties.getString(AppProperties.HTTP_PROTOCOL)
					+ ", SERVER_URL: " + AppProperties.getString(AppProperties.SERVER_URL)
					+ ", LOGIN_EMAIL: " + UserPreferences.getUserPref(UserPreferences.LOGIN_EMAIL)
					+ ", SYNC_NETWORK: " + UserPreferences.getUserPref(UserPreferences.SYNC_NETWORK)
					+ ", SYNC_SITE: " + UserPreferences.getUserPref(UserPreferences.SYNC_SITE)
					+ ", LOCAL_FOLDER_PATH: " + UserPreferences.getUserPref(UserPreferences.SYNC_LOCAL_FOLDER_PATH));
					
			
			// Initialise file stores	
			remoteDisk = new RemoteFileStore(AppProperties.getString(AppProperties.HTTP_PROTOCOL), 
					AppProperties.getString(AppProperties.SERVER_URL), UserPreferences.getUserPref(UserPreferences.SYNC_NETWORK), 
					UserPreferences.getUserPref(UserPreferences.SYNC_SITE), UserPreferences.getUserPref(UserPreferences.LOGIN_EMAIL), 
					UserPreferences.getUserPref(UserPreferences.LOGIN_PASSWORD));						
			localDisk = new LocalFileStore(UserPreferences.getUserPref(UserPreferences.SYNC_LOCAL_FOLDER_PATH));	
			
			// Check we can connect to both
			if (! remoteDisk.isValidConnection()) {
				SetupDialog.setStatusMsg(I18N.getString("error.cannotConnectRemote.html"));
				SetupDialog.showWindow();
			} else if (! localDisk.isValidConnection()) {
				SetupDialog.setStatusMsg(I18N.getString("error.cannotConnectLocal.html"));
				SetupDialog.showWindow();
			} else {
				// No problems, initialisation complete
				isInitialised = true;
				LOGGER.info("SyncManager Initialisation Complete!");
			}
		}
	}

	/**
	 * Start Syncing
	 * 
	 */
	public static void startSync() {
		
		// Create Instance of SyncManager
		if (syncManager == null)
			syncManager = new SyncManager();
		
		// Initialise and check connections are valid
		syncManager.init();
		if(syncManager.isInitialised && syncManager.remoteDisk.isValidConnection()){
			
			// Start Timer to syncronise from server periodically
			Timer timer = new Timer();
			int timerPeriod = (AppProperties.getInt(AppProperties.SYNC_TIMER_PERIOD) * 1000);
			LOGGER.info("Setting Sync Timer to " + String.valueOf(timerPeriod) + " milliseconds");
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					syncManager.sync();
					
				}
			}, 0, timerPeriod);
			
			// TODO - Add JNotify code here so that it automatically starts a sync when file events occur
			// HOWEVER need to check multi-threaded issues in case timer kicks off sync after real time 
			// event is already syncing
			
		} else {
			SetupDialog.setStatusMsg(I18N.getString("error.cannotConnectRemote.html"));
			SetupDialog.showWindow();
		}
	}
	
	private void sync() {
		if(remoteDisk == null || localDisk == null) {
			return;
		}

		sync("", remoteDisk, localDisk);
		sync("", localDisk, remoteDisk);
		SystemTrayIcon.setSyncStatus(false);
	}
	
	private void sync(String relativePath, FileStore source, FileStore destination) {
		SystemTrayIcon.setSyncStatus(true);
		LOGGER.info("Syncing....");
		Map<String, Resource> srcRoot = source.getResources(relativePath, false);
		Map<String, Resource> dstRoot = destination.getResources(relativePath, false);
		
		if(srcRoot == null || dstRoot == null) {
			return;
		}
		
		for(Resource srcResource : srcRoot.values()) {
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Checking resource on " + source.getName() + ": " + srcResource.getPath() + srcResource.getName());
			}
			
			if(isIllegal(srcResource)){
				LOGGER.debug("Ignoring: " + srcResource.getName());
				continue;
			}
			
			Resource dstResource = dstRoot.get(srcResource.getPath());
			if(srcResource.isDirectory()) {
				if(dstResource == null) {
					destination.putDirectory(srcResource);
				}
				String subPath = srcResource.getPath();
				sync(subPath, source, destination);
			} else if(dstResource == null || dstResource.getModified() < srcResource.getModified()) {
				InputStream stream = source.getFile(srcResource);
				destination.putFile(srcResource, stream);
			}
		}
		
//		for(Resource dstResource : dstRoot.values()) {
//			Resource srcResource = srcRoot.get(dstResource.getPath());
//			if(srcResource == null) {
//				dstCloud.deleteResource(dstResource);
//			}
//		}
	}
	
	/**
	 * Creates temporary instance of Remote File Store to check if
	 * it can connect and login credentials are valid
	 * 
	 * @param email		The user's email
	 * @param password	The user's password
	 * @return			True - login successful, False - login failed
	 */
	public static boolean validateLoginCredentials(String email, String password) {
		
		FileStore tmpRemoteDisk = new RemoteFileStore(AppProperties.getString(AppProperties.HTTP_PROTOCOL), 
				AppProperties.getString(AppProperties.SERVER_URL), "", "", email, password);
		
		return tmpRemoteDisk.isValidConnection();
	}
	
	/**
	 * Returns a String array of all the Networks a user has access
	 * to using their login credentials
	 * 
	 * @return	A String array of all the user's Networks
	 */
	public static String[] getNetworks() {
		
		FileStore tmpRemoteDisk = new RemoteFileStore(AppProperties.getString(AppProperties.HTTP_PROTOCOL), 
				AppProperties.getString(AppProperties.SERVER_URL), "", "", UserPreferences.getUserPref(UserPreferences.LOGIN_EMAIL), 
				UserPreferences.getUserPref(UserPreferences.LOGIN_PASSWORD));
		
		Map<String, Resource> networkDirs = tmpRemoteDisk.getResources("", false);
		
		// Get all Networks into String Array
		ArrayList<String> networks = new ArrayList<String>();
		for (String key : networkDirs.keySet()) {
			networks.add(networkDirs.get(key).getName());
		}
		
		return (String []) networks.toArray(new String[0]);
	}
	
	/**
	 * Returns a String array of all the Sites for a specific
	 * Network
	 * 
	 * @param network	The Network to get the Sites for
	 * @return			A String array of all the user's Sites in a Network
	 */
	public static String[] getSites(String network) {
		
		FileStore tmpRemoteDisk = new RemoteFileStore(AppProperties.getString(AppProperties.HTTP_PROTOCOL), 
				AppProperties.getString(AppProperties.SERVER_URL), "", "", UserPreferences.getUserPref(UserPreferences.LOGIN_EMAIL), 
				UserPreferences.getUserPref(UserPreferences.LOGIN_PASSWORD));
		
		Map<String, Resource> siteDirs = tmpRemoteDisk.getResources("/" + network + "/", false); 
		
		// Get all Networks into String Array
		ArrayList<String> sites = new ArrayList<String>();
		for (String key : siteDirs.keySet()) {		
			sites.add(siteDirs.get(key).getName());
		}
				
		return (String []) sites.toArray(new String[0]);
	}
	
	private Map<String, String> getAllExceptions() {
		Map<String, String> exceptions = getLocalExceptions();
		if(exceptions == null) {
			exceptions = new HashMap<String, String>();
		} else {
			LOGGER.debug("Local exceptions: " + exceptions.toString());
		}
		exceptions.putAll(globalExceptions);
		return exceptions;
	}
	
	private Map<String, String> getGlobalExceptions() {
		try {
			InputStream fileStream = getClass().getResourceAsStream("global.ignore");
			return readExceptions(fileStream);
		} catch (Exception e) {
			LOGGER.error(e.toString());
		}
		return null;
	}
	
	private Map<String, String> getLocalExceptions() {
		try {
			return readExceptions(new FileInputStream("configFolder" + "/" + "exceptionsFile"));
		} catch (IOException e) {
			return null;
		}
	}
	
	private Map<String, String> readExceptions(InputStream fileStream) throws IOException {
		HashMap<String, String> exceptions = new HashMap<String, String>();	
		Scanner scanner = new Scanner(fileStream);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] parts = line.split(",");
			exceptions.put(parts[0], parts[1]);
		}
		LOGGER.debug("exceptions loaded: " + exceptions.toString());
		return exceptions;
	}
	
	private boolean isIllegal(Resource resource) {
		return (isTooLarge(resource) || isException(resource));
	}
	
	private boolean isTooLarge(Resource resource) {
		return (resource.getSize() > ONE_GB);
	}
	
	private boolean isException(Resource resource) {
		
		for(String exception : getAllExceptions().keySet()) {
			if(resource.getName().matches(exception)) {
				return true;
			}
		}
		return false;
	}
}
