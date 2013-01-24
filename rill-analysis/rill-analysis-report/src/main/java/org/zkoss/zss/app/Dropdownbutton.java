/* Dropdownbutton.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 9, 2010 7:01:58 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zul.impl.LabelImageElement;

/**
 * @author Sam
 *
 */
public class Dropdownbutton extends LabelImageElement {

	public final static String ON_DROPDOWN = "onDropdown";
	static {
		addClientEvent(Dropdownbutton.class, ON_DROPDOWN, 0);
	}
	
	
	@Override
	public void service(AuRequest req, boolean arg1) {
		super.service(req, arg1);
	}
}