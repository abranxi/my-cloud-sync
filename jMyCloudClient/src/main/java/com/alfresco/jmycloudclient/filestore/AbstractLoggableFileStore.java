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

import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;

import com.alfresco.jmycloudclient.model.Resource;



public abstract class AbstractLoggableFileStore implements FileStore {
	
	@Override
	public boolean isValidConnection() {
		getLogger().debug(getLogMessage("Test Connection"));
		boolean canConnect = canConnect();
		if(!canConnect) {
			getLogger().error(getLogMessage("Invalid credentials"));
		} else {
			getLogger().debug(getLogMessage("Connection Successful"));
		}
		return canConnect;
	}
	
	@Override
	public Map<String, Resource> getResources(String path, boolean deep) {
		getLogger().debug(getLogMessage("Get resources for: [" + path + "] " + (deep?"DEEP":"SHALLOW")));
		return listResources(path, deep);
	}
	
	@Override
	public InputStream getFile(Resource resource) {
		getLogger().debug(getLogMessage("Get file: " + resource.getPath()));
		return getFileStreamFromResource(resource);
	}
	
	@Override
	public void putFile(Resource resource, InputStream stream) {
		getLogger().debug(getLogMessage("Put file: " + resource.getPath()));
		putFileFromStream(resource, stream);
	}
	
	@Override
	public void putDirectory(Resource resource) {
		getLogger().debug(getLogMessage("Put directory: " + resource.getPath()));
		createDirectory(resource);
	}
	
	@Override
	public void deleteResource(Resource resource) {
		getLogger().debug(getLogMessage("Delete resource: " + resource.getPath()));
		removeResource(resource);
	}
	
	protected abstract Logger getLogger();
	
	protected abstract boolean canConnect();
	
	protected abstract Map<String, Resource> listResources(String path, boolean deep);
	
	protected abstract InputStream getFileStreamFromResource(Resource resource);
	
	protected abstract void putFileFromStream(Resource resource, InputStream stream);
	
	protected abstract void createDirectory(Resource resource);
	
	protected abstract void removeResource(Resource resource);
	
	private String getLogMessage(String message) {
		return getName() + ": "+ message;
	}
}