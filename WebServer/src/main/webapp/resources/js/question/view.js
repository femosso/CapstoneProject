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

// button that latest triggered the modal
var button = null;

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

        var formatIndex = $("#question-format option:selected").index();

        var questionObject = new Object();
        questionObject.id = id;
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

    $('#edit').on('show.bs.modal', function(e) {
        // button that triggered the modal
        button = $(e.relatedTarget); 

        var modal = $(this);

        // get the closest "tr" parent
        var tr = button.closest('tr');

        // set question-id to a hidden input inside modal-body
        modal.find('#question-id').val(tr.attr("id"));

        // get all elements from this "tr"
        var kids = tr.children();

        modal.find('#question-text').val(kids.eq(0).text());
        modal.find('#question-type').selectpicker('val', kids.eq(1).text());
        modal.find('#question-format').selectpicker('val', kids.eq(2).text().trim());

        var formatIndex = $("#question-format option:selected").index();

        // if multiple-choice format, fill up its alternatives
        if(formatIndex == 0) {
            // get all alternatives that are hidden inside question-format td
            kids.eq(2).find("input").each(function(e) {
                var alternative = $("<div><p class='space'>&nbsp;</p>" +
                        "<div class='input-group'>" +
                          "<input type='text' class='form-control alternative-text' value='" + $(this).val() + "'>" +
                          "<span class='input-group-btn'>" +
                            "<button class='btn btn-default alternative remove' type='button'>-</button>" +
                          "</span>" +
                        "</div></div>");

                modal.find('#question-alternatives').children().first().append(alternative);
            });
        }
    });

    $('#edit').on('hidden.bs.modal', function (e) {
        $(this).find('#question-alternatives').children().children().each(function(e) {
            if($(this).attr("class") != "input-group") {
                $(this).remove();
            }
        });
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

    $('#delete').on('show.bs.modal', function(e) {
        // button that triggered the modal
        button = $(e.relatedTarget);
    });

    $('#delete-question').click(function(e) {
        e.preventDefault();

        // get the closest "tr" parent
        var tr = button.closest('tr');

        // set question-id to a hidden input inside modal-body
        var id = tr.attr("id");

        alert("id " + id);
        $.ajax({
            url : getContextPath() + '/question/delete/' + id,
            type : "DELETE",
            success : function(data) {
                if(data.status == 'OK') {
                    toastr.success(data.message, "Success!");
                    //$('#login-nav').submit();
                } else {
                    toastr.error(data.message, "Opsss..");
                }
                $('#delete').modal('hide');
            },
            error : function(data) {
                $('#delete').modal('hide');
            }
        });
    });
});