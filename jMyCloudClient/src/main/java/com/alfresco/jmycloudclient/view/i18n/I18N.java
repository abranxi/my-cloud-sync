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

package com.alfresco.jmycloudclient.view.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to get language strings from language bundles
 * in the resources directory.
 * 
 * All language files are under src/main/resources/view/i18n package
 * and use the following file name format:
 * 
 * <bundle>_<language>_<country>.properties
 * 
 * So for US-English in the default Messages file:
 * 
 * Messages_en_US.properties
 * 
 * The default locale assumed (if not given) is US-English and the default
 * bundle is assumed to be 'Messages' if not given
 * 
 * @author dgildeh
 *
 */
public class I18N {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(I18N.class);
	
	private static final String DEFAULT_BUNDLE = "Messages";
	private static final String BUNDLE_CLASSPATH = "com.alfresco.jmycloudclient.view.i18n.";
	
	/**
	 * Quick helper method to get String assuming locale is default or set in 
	 * user preferences (todo) and default 'Messages' bundle is being called
	 * 
	 * @param key	The key to the string you wish to use, i.e. login.email
	 * @return		Returns the localised String, or null if not found
	 */
	public static String getString(String key) {	
		return getString(getLocale(), DEFAULT_BUNDLE, key);
	}
	
	/**
	 * Quick helper method to get String assuming locale is default or set in 
	 * user preferences (todo)
	 * 
	 * @param key	The key to the string you wish to use, i.e. login.email
	 * @return		Returns the localised String, or null if not found
	 */
	public static String getString(String bundle, String key) {
		return getString(getLocale(), bundle, key);
	}
	
	
	/**
	 * Get a localised string passing all required arguments. 
	 * 
	 * @param locale	The locale you wish to use
	 * @param bundle	The properties file bundle the string is in, i.e. Messages
	 * @param key		The key to the string you wish to use, i.e. login.email
	 * @return			Returns the localised String, or null if not found
	 */
	public static String getString(Locale locale, String bundle, String key) {
		try{
			ResourceBundle rb = ResourceBundle.getBundle(getBundlePath(bundle), locale);
		    return rb.getString(key);
		} catch (MissingResourceException e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		return null;	    
	}
	
	/**
	 * Gets the default locale or (TODO) the locale the user has selected in
	 * their user preferences
	 * 
	 * @return		Returns the required locale for the current user
	 */
	private static Locale getLocale() {
		return Locale.US;
	}
	
	/**
	 * Returns full path to resource bundle in /com/alfresco/jmycloudclient/view/i18N package
	 * 
	 * @param bundle	The properties file bundle the string is in, i.e. Messages
	 * @return			The full resource path to the message bundle
	 */
	private static String getBundlePath(String bundle) {
		return BUNDLE_CLASSPATH + bundle;
	}

}
