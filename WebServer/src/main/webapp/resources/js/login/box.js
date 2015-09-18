toastr.options = {
    "closeButton": true,
    "debug": false,
    "newestOnTop": true,
    "progressBar": false,
    "positionClass": "toast-top-right",
    "preventDuplicates": false,
    "onclick": null,
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "5000",
    "extendedTimeOut": "1000",
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut"
};

$(document).ready(
function() {
    function getContextPath() {
        return window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
    }

    // This is called with the results from from FB.getLoginStatus().
    function statusChangeCallback(response) {
        // The response object is returned with a status field that lets the
        // app know the current login status of the person.
        if (response.status === 'connected') {
            // Logged into your app and Facebook.
            FB.api('/me', function(response) {
                sendFacebookDataAjax(response.email, response.id);
            });
        } else if (response.status === 'not_authorized') {
            // The person is logged into Facebook, but not your app.
            alert('Please log into the app.');
        }
    }

    // This function is called when someone finishes with the Login
    // Button.
    function checkLoginState() {
        FB.getLoginStatus(function(response) {
            statusChangeCallback(response);
        });
    }

    function login() {
        FB.login(function(response) {
            statusChangeCallback(response);
          }, {scope: 'public_profile, email'});
    }

    function logout() {
        FB.api('/me/permissions', 'DELETE', function(response) {
        });
    }

    window.fbAsyncInit = function() {
        FB.init({
            appId : '1569471569983376',

            // enable cookies to allow the server to access the session
            cookie : true,

            // parse social plugins on this page
            xfbml : true,
            version : 'v2.2' // use version 2.2
        });
    };

    // Load the SDK asynchronously
    (function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id))
            return;
        js = d.createElement(s);
        js.id = id;
        js.src = "//connect.facebook.net/en_US/sdk.js";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));

    function sendFacebookDataAjax(email, facebookId) {
        sendDataAjax("FACEBOOK", email, null, facebookId);
    }

    function sendApplicationDataAjax(email, password) {
        sendDataAjax("APPLICATION", email, password, null, null);
    }

    function sendDataAjax(provider, email, password, facebookId) {
        var userObject = new Object();
        userObject.provider = provider;
        userObject.email = email;
        userObject.password = password;
        userObject.facebookId = facebookId;

        $.ajax({
            url : getContextPath() + '/login/send',
            type : "POST",
            dataType : 'json',
            contentType : 'application/json',
            data : JSON.stringify(userObject),

            success : function(data) {
                if(data.status == 'OK') {
                    //toastr.success(data.message, "Success!");
                    $('#login-nav').submit();
                } else {
                    toastr.error(data.message, "Opsss..");
                }
                $('#sign-in').button('reset');
                $('#sign-in-facebook').button('reset');
            },
            error : function(data, status, er) {
                toastr.error(data.status + "\n" + data.message, "Opsss..");
                $('#sign-in').button('reset');
                $('#sign-in-facebook').button('reset');
            }
        });
    }

    $('#sign-in').click(function() {
        $('#sign-in').button('loading');

        var email = $('#loginEmail').val();
        var password = CryptoJS.MD5($('#loginPassword').val()).toString();

        sendApplicationDataAjax(email, password);
    });

    $('#sign-in-facebook').click(function() {
        $('#sign-in-facebook').button('loading');
        login();
    });
});
