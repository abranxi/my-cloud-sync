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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppProperties {

	public static final String HTTP_PROTOCOL = "protocol";
	public static final String SERVER_URL = "server";
	public static final String SYNC_TIMER_PERIOD = "syncPeriodSeconds";
	public static final String HELP_URL = "helpUrl";
	public static final String SIGNUP_URL = "signupUrl";
	public static final String FORGOT_PASSWORD_URL = "forgotPasswordUrl";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);
	
	// Reference Config Properties File in Resources folder
	private static final String CONFIG_FILE_PATH = "com.alfresco.jmycloudclient.application";
	private static final ResourceBundle config = ResourceBundle.getBundle(CONFIG_FILE_PATH);
	
	/**
	 * Get a string value for a property stored in the 
	 * config.properties file
	 * 
	 * @param key	The key for the property
	 * @return		The String value, or NULL if not found or issue
	 */
	public static String getString(String key) {
		try {
			return config.getString(key);
		} catch (NullPointerException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (MissingResourceException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (ClassCastException e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		// Return null if any of above errors
		return null;
		
	}
	
	/**
	 * Get an Integer value for a property stored in the
	 * config.properties file
	 * 
	 * @param key	The key for the property
	 * @return		The Integer value, or -1 if not found or issue
	 */
	public static int getInt(String key) {
		String strValue = getString(key);
		
		if (strValue != null) {
			return Integer.parseInt(strValue);
		} else {
			return -1;
		}
	}
}
