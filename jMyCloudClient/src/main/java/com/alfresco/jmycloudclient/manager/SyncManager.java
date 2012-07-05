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

import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.filestore.FileStore;
import com.alfresco.jmycloudclient.filestore.LocalFileStore;
import com.alfresco.jmycloudclient.filestore.RemoteFileStore;
import com.alfresco.jmycloudclient.model.Resource;
import com.alfresco.jmycloudclient.view.ClickHandler;
import com.alfresco.jmycloudclient.view.SyncView;

public class SyncManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(SyncManager.class);
	private static final int ONE_GB = 1073741824;
	private final SyncView view;
	private final String protocol;
	private final String server;
	private final String localCloud;
	private final String configFolder;
	private final String exceptionsFile;
	private final Properties settings;

	private final Map<String, String> globalExceptions;
	
	private FileStore remoteDisk;
	private FileStore localDisk;
	private boolean uiDisabled;

	public SyncManager(ResourceBundle resources, final SyncView view) {
		this.view = view;
		this.globalExceptions = getGlobalExceptions();
		
		configFolder = System.getProperty("user.home") + resources.getString("configfolder");
		settings = loadSettingsFile(resources.getString("settingsfile"));
		protocol = resources.getString("protocol");
		server = resources.getString("server");
		localCloud = getLocalCloud(resources);
		exceptionsFile = resources.getString("exceptionsfile");
		
		view.addSyncButtonHandler(new ClickHandler() {			
			@Override
			public void onClick(MouseEvent e) {
				if(uiDisabled) {
					return;
				}
				
				LOGGER.debug("Start sync clicked");
				loginUpdateView(true, "");
				startSync();
			}
		});
	}
	
	private void init(String tenant, String site, String username, String password) {
		remoteDisk = new RemoteFileStore(protocol, server, tenant, site, username, password);
		localDisk = new LocalFileStore(localCloud);
	}
	
	private void sync() {
		if(remoteDisk == null || localDisk == null) {
			return;
		}

		sync("", remoteDisk, localDisk);
		sync("", localDisk, remoteDisk);
		view.setStatusMessage("Sync complete. waiting....");
	}
	
	private void sync(String relativePath, FileStore source, FileStore destination) {
		Map<String, Resource> srcRoot = source.getResources(relativePath, false);
		Map<String, Resource> dstRoot = destination.getResources(relativePath, false);
		
		if(srcRoot == null || dstRoot == null) {
			return;
		}
		
		for(Resource srcResource : srcRoot.values()) {
			if(isIllegal(srcResource)){
				LOGGER.debug("Ignoring: " + srcResource.getName());
				continue;
			}
			view.setStatusMessage(source.getName() + " - " + srcResource.getName());
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
	
	private void startSync() {
		init(getTenant(view.getEmail()), getDefaultSite(view.getEmail()), view.getEmail(), view.getPassword());
		if(remoteDisk.isValidConnection()){
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					sync();
					
				}
			}, 0, 300000);
		} else {
			loginUpdateView(false, "Invalid credentials");
		}
		
	}

	private void loginUpdateView(boolean success, String status) {
		view.enableLogin(!success);
		view.setStatusMessage(status);
		uiDisabled = true;
	}
	
	private String getTenant(String email) {
		String tenant = settings.getProperty("cloud-tenant");
		if(tenant == null || tenant.isEmpty()){
			tenant = email.split("@")[1];
		}
		
		return tenant;
	}
	
	private String getDefaultSite(String email) {
		String site = settings.getProperty("cloud-site");
		if(site == null || site.isEmpty()) {
			try {
				String[] emailSplit = email.split("@");
				String[] user = emailSplit[0].split("\\.");
				String[] domain = emailSplit[1].split("\\.");
				site = user[0] + "-" + user[1] + "-" + domain[0] + "-" + domain[1];
			} catch (Exception e) {
				return "";
			}
		}
		
		return site;
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
			return readExceptions(new FileInputStream(configFolder + "/" + exceptionsFile));
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
	
	private Properties loadSettingsFile(String path) {
		Properties settings = new Properties();
		try {
			settings.load(new FileInputStream(configFolder + "/" + path));
		} catch (Exception e) {
			LOGGER.error("Failed to load settings file");
		}
		
		return settings;
	}
	
	private String getLocalCloud(ResourceBundle resources) {
		String localCloud = settings.getProperty("local-folder");
		if(localCloud == null || localCloud.isEmpty()) {
			localCloud = System.getProperty("user.home") + resources.getString("localcloudfolder");
		}
		return localCloud;
	}
}
