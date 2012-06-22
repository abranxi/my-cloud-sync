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

package com.alfresco.jmycloudclient.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.model.Resource;


public class LocalFileStore extends AbstractLoggableFileStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileStore.class);
			
	private static final String NAME = "Local";
	private final String rootPath;

	public LocalFileStore(String basePath) {
		this.rootPath = basePath;
	}
	
	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public boolean canConnect() {
		try {
			File file = new File(rootPath);
			file.listFiles();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	@Override
	protected Map<String, Resource> listResources(String path, boolean deep) {
		HashMap<String, Resource> resources = null;
		File[] children = getFileFromRelativePath(path).listFiles();
		if(children != null) {
			resources = new HashMap<String, Resource>();
			for(int i = 0; i < children.length; i++) {
				File file = children[i];
				String relPath = getRelativePath(file.getPath());
				Resource res = new Resource(file.getName(), relPath, file.lastModified(), file.isDirectory(), file.length());
				resources.put(relPath, res);
				if(deep && file.isDirectory()) {
					Map<String, Resource> nested = listResources(file.getPath(), deep);
					if(nested != null) {
						resources.putAll(nested);
					}
				}
			}
		}
		
		return resources;
	}
	
	@Override
	protected InputStream getFileStreamFromResource(Resource resource) {
		File file = getFileFromResource(resource);
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			getLogger().error("Failed in local get: " + resource.getPath());
			getLogger().error(e.toString());
			return null;
		}
	}

	@Override
	protected void putFileFromStream(Resource resource, InputStream stream) {
		try {
			ReadableByteChannel rbc = Channels.newChannel(stream);
			FileOutputStream fos;
			fos = new FileOutputStream(getAbsolutePath(resource.getPath()));
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
			getFileFromResource(resource).setLastModified(resource.getModified());
		} catch (Exception e) {
			getLogger().error("Failed in local put: " + resource.getPath());
			getLogger().error(e.toString());
			return;
		}
	}
	
	@Override
	protected void createDirectory(Resource resource) {
		File directory = getFileFromResource(resource);
		if(!directory.isDirectory()) {
			try{ 
				directory.mkdir();
				directory.setLastModified(resource.getModified());
			} catch (Exception e) {
				getLogger().error("Failed in local create dir: " + resource.getPath());
				getLogger().error(e.toString());
				return;
			}
		}
	}

	@Override
	protected void removeResource(Resource resource) {
		delete(getAbsolutePath(resource.getPath()), true);
	}
	
	private void delete(String absolutePath, boolean recursive) {
		File file = getFile(absolutePath);
		if(recursive && file != null && file.isDirectory()) {
			File[] children = file.listFiles();
			for(int i = 0; i < children.length; i++){
				delete(children[i].getAbsolutePath(), recursive);
			}
		}
		try {
			file.delete();
		} catch (Exception e) {
			getLogger().error("Failed in local delete: " + absolutePath);
			getLogger().error(e.toString());
			return;
		}
	}
	
	private String getAbsolutePath(String relativePath) {
		String localPath = rootPath + relativePath;
		return localPath;
	}
	
	private String getRelativePath(String absolutePath) {
		return absolutePath.substring(rootPath.length());
	}
	
	private File getFileFromResource(Resource resource) {
		return getFileFromRelativePath(resource.getPath());
	}
	
	private File getFileFromRelativePath(String path) {
		return getFile(getAbsolutePath(path));
	}
	
	private File getFile(String absolutePath) {
		return new File(absolutePath);
	}
}
