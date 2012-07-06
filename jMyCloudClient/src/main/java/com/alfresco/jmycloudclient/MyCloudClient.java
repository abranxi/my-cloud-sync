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

import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.filestore.SystemListener;
import com.alfresco.jmycloudclient.manager.SyncManager;
import com.alfresco.jmycloudclient.view.SyncView;
import com.alfresco.jmycloudclient.view.SyncViewImpl;

public class MyCloudClient {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MyCloudClient.class);

	public static void main(String[] args) throws Exception {
		final ResourceBundle resources = ResourceBundle
				.getBundle("com.alfresco.jmycloudclient.config");

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				LOGGER.info("----------myCloudSync started----------");
				SyncView view = new SyncViewImpl();
				new SyncManager(resources, view);
			}
		});
		
		SystemListener listener = new SystemListener();
	}
}
