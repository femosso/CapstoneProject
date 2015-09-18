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
                var teenObject = new Object();
                teenObject.firstName = response.first_name;
                teenObject.lastName = response.last_name;
                teenObject.birthday = response.birthday;

                var userObject = new Object();
                userObject.provider = "FACEBOOK";
                userObject.email = response.email;
                userObject.facebookId = response.id;
                userObject.teen = teenObject;

                sendDataAjax(userObject, teenObject);
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
          }, {scope: 'public_profile, email, user_birthday'});
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

    function sendDataAjax(userObject, teenObject) {

        alert(JSON.stringify(userObject));
        alert(JSON.stringify(teenObject));

        $.ajax({
            url : getContextPath() + '/login/register/submit',
            type : "POST",
            dataType : 'json',
            contentType : 'application/json',
            data : JSON.stringify(userObject),
            success : function(data) {
                if(data.status == 'OK') {
                    //toastr.success(data.message, "Success!");
                    //$('#login-nav').submit();
                } else {
                    toastr.error(data.message, "Opsss..");
                }
                $('#sign-up').button('reset');
                $('#sign-up-facebook').button('reset');
            },
            error : function(data) {
                $('#sign-up').button('reset');
                $('#sign-up-facebook').button('reset');
            }
        });
    }

    $('#datepicker-container input').datepicker({
        format: "dd/MM/yyyy",
        startView: 2,
        language: "pt-BR",
        orientation: "top auto",
        autoclose: true,
        todayHighlight: true
    });

    $("#sign-up").click(function(e) {
        $('#sign-up').button('loading');

        var firstName = $('#firstname').val();
        var lastName = $('#lastname').val();
        var email = $('#email').val();
        var birthday = $('#birthday').val();
        var password = CryptoJS.MD5($('#password').val()).toString();
        var confirm = CryptoJS.MD5($('#confirm').val()).toString();

        /*if(password != confirm) {
        alert("confirm should be equals");
        return false;
        }*/

        var teenObject = new Object();
        teenObject.firstName = firstName;
        teenObject.lastName = lastName;
        teenObject.birthday = birthday;

        var userObject = new Object();
        userObject.provider = "APPLICATION";
        userObject.email = email;
        userObject.password = password;
        userObject.teen = teenObject;

        sendDataAjax(userObject, teenObject);
    });

    $("#sign-up-facebook").click(function(e) {
        $('#sign-up-facebook').button('loading');
        login();
    });


});