/* ZssappComponent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Nov 11, 2010 3:38:40 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.zul;

import org.zkoss.zss.ui.Spreadsheet;

/**
 * @author Sam
 *
 */
public interface ZssappComponent {
	
	//TODO: remove this mechanism
	public void bindSpreadsheet(Spreadsheet spreadsheet);
	//TODO: remove this mechanism
	public void unbindSpreadsheet();
}
