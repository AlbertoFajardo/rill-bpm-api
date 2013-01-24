/* Colorbutton.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Sep 21, 2010 6:27:01 PM , Created by Sam
}}IS_NOTE

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

*/
(function () {

zssapp.Zssapp = zk.$extends(zul.wgt.Div, {
	
	parentIframeId: null,
	
	setHeight: function (h) {
		this.$supers('setHeight', arguments);
	},
	
	resetParentHeight: function (iframeId, height) {
	     var parentIframe = window.parent.document.getElementById(iframeId);
	     parentIframe.style.height = height;
	},

	setParentIframeId: function (pid) {
		this.parentIframeId = pid;
		// Set parent iframe height.
		if (this.parentIframeId) {
			this.resetParentHeight(pid, this.getHeight());
		}
	},
	getParentIframeId: function () {
        return this.parentIframeId;
    }
});
})();