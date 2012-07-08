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

import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores and reads user preferences stored in on the user's local desktop. The way the
 * preferences are stored is dependent on the OS being used. A local encryption mechanism
 * is provided to securely store user passwords on the local machine so the user doesn't 
 * need to login each time they use the application
 * 
 * TODO - Add Encryption/Decryption of String values
 * 
 * @author dgildeh
 *
 */
public class UserPreferences {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserPreferences.class);
	
	// Constants for application keys
	public static final String LOGIN_EMAIL = "email";
	public static final String LOGIN_PASSWORD = "password";
	public static final String LOGIN_VALIDATED = "login_validated";
	public static final String SYNC_NETWORK = "network";
	public static final String SYNC_SITE = "site";
	public static final String SYNC_LOCAL_FOLDER_PATH = "local_folder";
	
	// Holds local preferences for application
	private Preferences prefs;
	
	// Holds Singleton reference to class
	private static UserPreferences userPrefs = null;
	
	/**
	 * Private Constructor
	 */
	private UserPreferences() {
		// Get the preferences for this app
		prefs = Preferences.userRoot().node(this.getClass().getName());
	}
	
	/**
	 * Singleton implementation to get instance of class
	 * 
	 * @return	The User Preferences Object
	 */
	private static Preferences getPreferences() {
		if (userPrefs == null) {
			userPrefs = new UserPreferences();
		}
		return userPrefs.prefs;
	}
	
	/**
	 * Save a String user preference
	 * 
	 * @param key		The key of the preference
	 * @param value		The value of the preference
	 */
	public static void saveUserPref(String key, String value) {
		// If null don't save anything or it will throw a NullPointerException
		if (value != null) {
			LOGGER.info("Saving User Preference '" + key + "': " + value);
			getPreferences().put(key, value);
		} else {
			LOGGER.error("The value for " + key + " is NULL");
		}	
	}

	/**
	 * Get a String user preference. This method will return NULL if the key
	 * is not found
	 * 
	 * @param key			The key of the preference
	 * @return				The value for the user preference, NULL if not found
	 */
	public static String getUserPref(String key) {
		return getPreferences().get(key, null);
	}
	
	/**
	 * Get a String user preference and specify a default value to be returned
	 * if no value is found
	 * 
	 * @param key			The key of the preference
	 * @param defaultVal	The default value to return if preference not found
	 * @return				The value for the user preference, default value if not found
	 */
	public static String getUserPref(String key, String defaultVal) {
		return getPreferences().get(key, defaultVal);
	}
	
	/**
	 * Save a user boolean preference
	 * 
	 * @param key		The key of the preference
	 * @param value		The value of the preference
	 */
	public static void saveUserPref(String key, boolean value) {
		LOGGER.info("Saving User Preference '" + key + "': " + String.valueOf(value));
		getPreferences().putBoolean(key, value);
	}
	
	/**
	 * Get a boolean user preference and specify a default value to be returned
	 * if no value is found
	 * 
	 * @param key			The key of the preference
	 * @param defaultVal	The default value to return if preference not found
	 * @return				The value for the user preference, default value if not found
	 */
	public static boolean getUserPref(String key, boolean defaultVal) {
		return getPreferences().getBoolean(key, defaultVal);
	}
	
	/**
	 * Remove a user preference value
	 * 
	 * @param key			The key of the preference to remove
	 */
	public static void removeUserPref(String key) {
		LOGGER.info("Removing User Preference '" + key + "'");
		getPreferences().remove(key);
	}	
}
