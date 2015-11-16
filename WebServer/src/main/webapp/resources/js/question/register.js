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
                    $("#question-form")[0].reset();
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

    $("#question").addClass("active");

    $("#submit-question").click(function(e) {
        e.preventDefault();

        $('#submit-question').button('loading');

        var question = $('#question-text').val();
        var type = $('#question-type').val();
        var format = $('#question-format').val();

        var formatIndex = $("#question-format option:selected").index();

        var questionObject = new Object();
        questionObject.text = question;
        questionObject.type = type;
        questionObject.format = format;

        // if multiple-choice format, get its alternatives
        if(formatIndex == 0) {
            var alternativeObject;
            var alternativeArray = new Array();

            $(".alternative-text").each(function(e) {
                alternativeObject = new Object();
                alternativeObject.text = $(this).val();

                alternativeArray.push(alternativeObject);
            });

            questionObject.alternativeList = alternativeArray;
        }

        sendDataAjax(questionObject);
    });

    $('#question-format').on('change', function() {
        var formatIndex = $("#question-format option:selected").index();

        // if multiple-choice, show the alternatives UI
        if(formatIndex == 0) {
            $("#question-alternatives").show();
        } else {
            $("#question-alternatives").hide();
        }
    });

    $('.alternative.add').click(function(e) {
        e.preventDefault();

        var alternative = $("<div><p class='space'>&nbsp;</p>" +
                            "<div class='input-group'>" +
                              "<input type='text' class='form-control alternative-text'>" +
                              "<span class='input-group-btn'>" +
                                "<button class='btn btn-default alternative remove' type='button'>-</button>" +
                              "</span>" +
                            "</div></div>");

        $(this).parent().parent().parent().append(alternative);
    });

    $(document).on('click', '.alternative.remove', function(e) {
        e.preventDefault();

        $(this).parent().parent().parent().remove();
    });
});