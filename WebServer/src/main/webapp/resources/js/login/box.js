function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
}

$(document).ready(function() {

/*	$('.dropdown').click(function() {
		if($('#invalido').text() != "") {
			$('#invalido').removeAttr('style');
			$('#invalido').text("");
		}
	});*/

	$('#saveLogin').click(function() {
/*		if($('#invalido').text() != "") {
			$('#invalido').removeAttr('style');
			$('#invalido').text("");
		}*/
		$('#saveLogin').button('loading');
		sendDataAjax();
	});

/*	$("#confirmaLogin").keypress(function(event) {
	    if (event.which == 13) {
	    	event.preventDefault();
	    	if($('#invalido').text() != "") {
				$('#invalido').removeAttr('style');
				$('#invalido').text("");
			}
	    	$('#saveLogin').button('loading');
	    	sendDataAjax();
	    }
	});*/

	function sendDataAjax() {
		var email = $('#email').val();
		var password = $('#password').val();

		var userObject = new Object();
		userObject.email = email;
		userObject.password = password;

		$.ajax({
			url : getContextPath() + '/login/send',
			type : "POST",
			dataType : 'json',
			data : JSON.stringify(userObject),
			contentType : 'application/json',
			   accept: 'application/json',

			mimeType : 'application/json',

			/*
			 * headers : { 'Accept' : 'application/json', 'Content-Type' :
			 * 'application/json' },
			 */

			success : function(data) {
				// Form submit
				$('#login-nav').submit();
			},
			error : function(data, status, er) {
				alert("error: " + data + " status: " + status + " er:" + er);
			}
		});
	}
});

