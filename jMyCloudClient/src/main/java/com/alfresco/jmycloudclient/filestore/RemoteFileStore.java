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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.model.Resource;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

public class RemoteFileStore extends AbstractLoggableFileStore {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteFileStore.class);
	
	private static final String NAME = "Cloud";
	private final String protocol;
	private final String server;
	private final Sardine sardine;
	private final String rootPath;

	public RemoteFileStore(String protocol, String server, String tenant, String site, String username, String password) {
		super();
		this.protocol = protocol;
		this.server = server;
		this.rootPath = "/" + tenant + "/" + site;
		
		sardine = SardineFactory.begin(username, password);
		sardine.enableCompression();
		
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
	protected boolean canConnect() {		
		try {
			sardine.list(getURI("/").toString());
		} catch (IOException e) {
			getLogger().error("Cannot connect to webdav [" + getURI("/").toString() + "]");
			getLogger().error(e.toString());
			return false;
		}
		return true;
	}
	
	@Override
	protected Map<String, Resource> listResources(String path, boolean deep) {
		HashMap<String, Resource> resources = null;
		URI pathUri = getURI(path);
		String container = getFolderName(pathUri.getPath());
		
		List<DavResource> davResources = null;
		try {
			davResources = sardine.list(pathUri.toString());
		} catch (IOException e) {
			getLogger().error("Failed in webdav list: " + pathUri.toString());
			getLogger().error(e.toString());
			return null;
		}
		if(davResources != null) { 	
			resources = new HashMap<String, Resource>();
			for (DavResource davResource : davResources) {
				if(!isResourceContainer(davResource, container)) {
					String combinedPath = path + "/" + davResource.getName();
					Resource res = new Resource(davResource.getName(), combinedPath, (davResource.getModified() != null) ? davResource.getModified().getTime() : 0, davResource.isDirectory(), davResource.getContentLength());
					resources.put(combinedPath, res);
					if(deep && davResource.isDirectory()){
						Map<String, Resource> nested = listResources(combinedPath, deep);
						if(nested != null) {
							resources.putAll(nested);
						}
					}
				}
			}
		}
		
		return resources;
	}

	@Override
	protected InputStream getFileStreamFromResource(Resource resource) {
		try {
			String url = getURI(resource.getPath()).toString();
			return sardine.get(url);
		} catch (Exception e) {
			getLogger().error("Failed in webdav put: " + resource.getPath());
			getLogger().error(e.toString());
			return null;
		}
	}
	
	@Override
	protected void putFileFromStream(Resource resource, InputStream stream) {
		try {
			String url = getURI(resource.getPath()).toString();
			sardine.put(url.toString(), stream);
		} catch (Exception e) {
			getLogger().error("Failed in webdav put: " + resource.getPath());
			getLogger().error(e.toString());
			return;
		}
	}

	@Override
	protected void createDirectory(Resource resource) {
		String url = getURI(resource.getPath()).toString();
		try {
			sardine.createDirectory(url);
		} catch (IOException e) {
			getLogger().error("Failed webdav create dir: " + resource.getPath());
			getLogger().error(e.toString());
			return;
		}
	}

	@Override
	protected void removeResource(Resource resource) {
		try {
			String url = getURI(resource.getPath()).toString();
			sardine.delete(url);
		} catch (Exception e) {
			getLogger().error("Failed webdav delete: " + resource.getPath());
			getLogger().error(e.toString());
			return;
		}
	}
	
	private URI getURI(String path)  {
		URI pathUri = null;
		try {
			pathUri = new URI(protocol, server, rootPath + path, null);
			return pathUri;
		} catch (URISyntaxException e) {
			getLogger().error(e.toString());
			return null;
		}
	}

	private String getFolderName(String queryPath) {
		String path = (queryPath.charAt(queryPath.length() - 1) == '/') ? queryPath.substring(0, queryPath.length() - 1) : queryPath;
		int baseStart = path.lastIndexOf("/") + 1;
		return path.substring(baseStart);
	}
	
	private static boolean isResourceContainer(DavResource davResource, String resourceName) {
		return (davResource.isDirectory() && davResource.getName().equals(resourceName));
	}
}