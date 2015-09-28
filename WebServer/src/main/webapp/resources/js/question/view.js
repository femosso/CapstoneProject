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

        // go to modal-body div and search for question-id hidden input
        var id = $(this).parent().parent().find('#question-id').val();
        var question = $('#question-text').val();
        var type = $('#question-type').val();
        var format = $('#question-format').val();

        var questionObject = new Object();
        questionObject.id = id;
        questionObject.text = question;
        questionObject.type = type;
        questionObject.format = format;

        sendDataAjax(questionObject);
    });

    $('#edit').on('show.bs.modal', function(e) {
        // button that triggered the modal
        button = $(e.relatedTarget); 

        // get the closest "tr" parent
        var tr = button.closest('tr');

        // set question-id to a hidden input inside modal-body
        $(this).find('#question-id').val(tr.attr("id"));

        // get all elements from this "tr"
        var kids = tr.children();

        $(this).find('#question-text').val(kids.eq(0).text());
        $(this).find('#question-type').selectpicker('val', kids.eq(1).text());
        $(this).find('#question-format').selectpicker('val', kids.eq(2).text());
    });
});