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

package com.alfresco.jmycloudclient.view;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alfresco.jmycloudclient.manager.AppProperties;
import com.alfresco.jmycloudclient.view.i18n.I18N;

/**
 * Add's System Tray Icon with menu to OS if supported
 * 
 * @author dgildeh
 *
 */
public class SystemTrayIcon {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SystemTrayIcon.class);
	
	private static SystemTray systemTray = null;
	private static TrayIcon trayIcon = null;
	
	public SystemTrayIcon() {
			
		//Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            LOGGER.error("SystemTray is not supported"); 
            return;
        }
        
        // Create Popup Menu for System Tray Icon       
        trayIcon = new TrayIcon(createImage("/com/alfresco/jmycloudclient/view/trayIcon.png", "Tray Icon")); 
        systemTray = SystemTray.getSystemTray();
       
        // Create a pop-up menu components
        final PopupMenu popupMenu = new PopupMenu();
        
        // Add Application Preferences Dialog
        MenuItem preferencesItem = new MenuItem(I18N.getString("systemtray.menu.preferences")); 
        preferencesItem.addActionListener(new ActionListener()
        {
        	  public void actionPerformed(ActionEvent e)
        	  {
        		  if (LOGGER.isDebugEnabled()) {
        			  LOGGER.debug("Preferences Dialog Opened");  
        		  }
        		  SetupDialog.showWindow();
        	  }
        });
        popupMenu.add(preferencesItem);
        popupMenu.addSeparator();
        
        // Add Application About Dialog
        MenuItem aboutItem = new MenuItem(I18N.getString("systemtray.menu.about")); 
        aboutItem.addActionListener(new ActionListener()
        {
        	  public void actionPerformed(ActionEvent e)
        	  {
        		  if (LOGGER.isDebugEnabled()) {
        			  LOGGER.debug("About Dialog Opened");  
        		  }
        		  
        		  AboutDialog.showWindow();
        	  } 	  
        });
        popupMenu.add(aboutItem);
        
        // Add Application Help Link
        MenuItem helpItem = new MenuItem(I18N.getString("systemtray.menu.help")); 
        helpItem.addActionListener(new ActionListener()
        {
        	  public void actionPerformed(ActionEvent e)
        	  {
        		  if (LOGGER.isDebugEnabled()) {
        			  LOGGER.debug("Help URL Opened");  
        		  }
        		  openURL(AppProperties.HELP_URL); 
        	  }
        });
        popupMenu.add(helpItem);
        
        // Add Application Quit Menu Item - this quits the program completely if clicked
        MenuItem quitItem = new MenuItem(I18N.getString("systemtray.menu.quit")); 
        quitItem.addActionListener(new ActionListener()
        {
        	  public void actionPerformed(ActionEvent e)
        	  {
        		  LOGGER.info("----------Alfresco Sync Quit----------"); 
        		  System.exit(0);
        	  }
        });
        popupMenu.addSeparator();
        popupMenu.add(quitItem);
        
        // Set Menu
        trayIcon.setPopupMenu(popupMenu);
        trayIcon.setImageAutoSize(true);
       
        try {
        	systemTray.add(trayIcon);
        } catch (AWTException e) {
            LOGGER.error("TrayIcon could not be added."); //$NON-NLS-1$
        }
		
	}
	
	/**
	 * Set the Sync Status which will affect the icon of the TrayIcon
	 * 
	 * @param syncStatus	True - Currently Syncing, False - Inactive
	 */
	public static void setSyncStatus(boolean syncStatus) {
		
		if (syncStatus) {
			trayIcon.setImage(createImage("/com/alfresco/jmycloudclient/view/trayIcon_spinning.png", "Spinning Tray Icon")); 
			trayIcon.setToolTip(I18N.getString("systemtray.tooltip.syncing")); 
		} else {
			trayIcon.setImage(createImage("/com/alfresco/jmycloudclient/view/trayIcon.png", "Tray Icon")); 
			trayIcon.setToolTip(I18N.getString("systemtray.tooltip.inactive")); 
		}	
	}
	
	/**
	 * Get the icon to show in the System Tray
	 * 
	 * @param path			The Resource Path to the Image
	 * @param description	A Description for the Image
	 * @return				An ImageIcon to the Image
	 */
    protected static Image createImage(String path, String description) {
        
    	URL imageURL = SystemTrayIcon.class.getResource(path);
         
        if (imageURL == null) {
            LOGGER.error("Resource not found: " + path); 
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
    
    /**
     * Open's a Browser with a specific URL from its property key. URLs should be stored in the 
     * app.properties file for easy maintenance
     * 
     * @param urlKey	The URL Key to open from App Properties file
     */
    protected static void openURL(String urlKey) {
    	
    	Desktop desktop = Desktop.getDesktop();

        if( !desktop.isSupported( Desktop.Action.BROWSE ) ) {
            LOGGER.error("Desktop BROWSE Action is not supported, cannot open URL"); 
        } else {
      	
        	try {
        		URI uri = new URI( AppProperties.getString(urlKey) );
				desktop.browse(uri);
			} catch (IOException e) {
				LOGGER.error("Could not open default browser for URL key '" + urlKey +  "':", e); 
			} catch (URISyntaxException e) {
				LOGGER.error("Could not open default browser for URL key '" + urlKey +  "':", e); 
			}
        }
    }
}
