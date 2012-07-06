package com.alfresco.jmycloudclient.filestore;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

public class SystemListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemListener.class);
	private static int CURRENT_WATCH_ID;
	
	public SystemListener() throws JNotifyException {
		String path = System.getProperty("user.home") + "/Documents";
//		path += "/myCloud";
		
		int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED
				| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;

		boolean watchSubtree = true;

		CURRENT_WATCH_ID = JNotify.addWatch(path, mask, watchSubtree, new JNotifyListener() {

					@Override
					public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
						LOGGER.info(wd + "renamed " + rootPath + " : " + oldName + " -> " + newName);
					}

					@Override
					public void fileModified(int wd, String rootPath,
							String name) {
						LOGGER.info(wd + "modified " + rootPath + " : " + name);
					}

					@Override
					public void fileDeleted(int wd, String rootPath, String name) {
						LOGGER.info(wd + "deleted " + rootPath + " : " + name);
					}

					@Override
					public void fileCreated(int wd, String rootPath, String name) {
						LOGGER.info(wd + "created " + rootPath + " : " + name);
					}

					void print(String msg) {
						System.err.println(msg);
					}
				});

		LOGGER.info("Monitoring: " + path);
	}
}
