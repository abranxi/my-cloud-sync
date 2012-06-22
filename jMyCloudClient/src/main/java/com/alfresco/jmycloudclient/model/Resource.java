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

package com.alfresco.jmycloudclient.model;

public class Resource {
	
	
	private final String name;
	private final String path;
	private final long modified;
	private final boolean directory;
	private final long size;

	public Resource(String name, String path, long modified, boolean directory, long size) {
		this.size = size;
		if(directory && path.charAt(0) != '/'){
			throw new IllegalArgumentException("Resource path must relative");
		}
		
		this.name = name;
		this.path = path;
		this.modified = modified;
		this.directory = directory;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public long getModified() {
		return modified;
	}

	public boolean isDirectory() {
		return directory;
	}

	public long getSize() {
		return size;
	}
}
