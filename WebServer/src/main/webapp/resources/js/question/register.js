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

    function sendDataAjax(questionObject) {
        $.ajax({
            url : getContextPath() + '/question/register/submit',
            type : "POST",
            dataType : 'json',
            contentType : 'application/json',
            data : JSON.stringify(questionObject),
            success : function(data) {
                if(data.status == 'OK') {
                    toastr.success(data.message, "Success!");
                    //$('#login-nav').submit();
                } else {
                    toastr.error(data.message, "Opsss..");
                }
                $('#submit-question').button('reset');
            },
            error : function(data) {
                $('#submit-question').button('reset');
            }
        });
    }

    $("#submit-question").click(function(e) {
        e.preventDefault();

        $('#submit-question').button('loading');

        var question = $('#question-text').val();
        var type = $('#question-type').val();
        var format = $('#question-format').val();

        var questionObject = new Object();
        questionObject.text = question;
        questionObject.type = type;
        questionObject.format = format;

        sendDataAjax(questionObject);
    });
});