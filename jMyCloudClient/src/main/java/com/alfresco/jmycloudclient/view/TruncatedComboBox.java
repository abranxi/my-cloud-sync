package com.alfresco.jmycloudclient.view;

import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class TruncatedComboBox extends JComboBox {

	private static final long serialVersionUID = 7247821600792644513L;

	public TruncatedComboBox(Object[] itemArray) {
		super(itemArray);
		
		setRenderer(new TruncatedComboBoxRenderer());
	}
	
	private class TruncatedComboBoxRenderer extends BasicComboBoxRenderer {
		
		private static final long serialVersionUID = -5227036659109162814L;
		
		@Override
		public void setText(String text) {
			super.setText(truncateString(text, 25));
		}
		
		private String truncateString(String string, final int maxLength) {
			
			if (string.length() > maxLength) {
				string = string.substring(0, (maxLength - 3)) + "...";
			} 
			
			return string;	
		}
	}
}


