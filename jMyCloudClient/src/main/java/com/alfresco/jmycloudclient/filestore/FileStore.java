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

import com.alfresco.jmycloudclient.model.Resource;

public interface FileStore {

	String getName();
	
	boolean isValidConnection();
	
	Map<String, Resource> getResources(String path, boolean deep);
	
	InputStream getFile(Resource resource);
	
	void putFile(Resource resource, InputStream stream);
	
	void putDirectory(Resource resource);
	
	void deleteResource(Resource resource);
}
