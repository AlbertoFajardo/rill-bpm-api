/**
 * For RMS test
 */

var RMSTest = {
	
	modal : new Modal({
		
		message: "<form id='login_form'>" +
        "<label for='username'>Username</label><br />" +
        "<input type='text' id='username' name='username' value='' /><br />" +
        "<label for='password'>Password</label><br />" +
        "<input type='password' id='password' name='password' value='' />" +
        "</form>"
	})
};

$(document).ready(
	function() {
		RMSTest.modal.render().open();
	}
);