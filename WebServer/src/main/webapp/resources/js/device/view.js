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

    function sendDataAjax(deviceMessageObject) {
        alert(JSON.stringify(deviceMessageObject));

        $.ajax({
            url : getContextPath() + '/device/send',
            type : "POST",
            dataType : 'json',
            contentType : 'application/json',
            data : JSON.stringify(deviceMessageObject),
            success : function(data) {
                if(data.status == 'OK') {
                    toastr.success(data.message, "Success!");
                    //$('#login-nav').submit();
                } else {
                    toastr.error(data.message, "Opsss..");
                }
                $('#submit-device').button('reset');
            },
            error : function(data) {
                $('#submit-device').button('reset');
            }
        });
    }

    $("#send-message").click(function(e) {
        e.preventDefault();

        $('#send-message').button('loading');

        var message = $('#device-message').val();

        var deviceArray = new Array();
        // run through all checked checkboxes
        $("#devices-table .check-this:checkbox:checked").each(function () {
            // get all columns of this checked checkbox row
            var kids = $(this).closest('tr').children();

            var deviceObject = new Object();
            deviceObject.token = kids.eq(0).text();

            deviceArray.push(deviceObject);
        });

        var deviceMessageObject = new Object();
        deviceMessageObject.deviceList = deviceArray;
        deviceMessageObject.message = message

        sendDataAjax(deviceMessageObject);
    });

    $("#devices-table #check-all").click(function () {
        if ($("#devices-table #check-all").is(':checked')) {
            $("#devices-table input[type=checkbox]").each(function () {
                $(this).prop("checked", true);
            });

        } else {
            $("#devices-table input[type=checkbox]").each(function () {
                $(this).prop("checked", false);
            });
        }
    });

    $("#devices-table .check-this").click(function () {
        if ($("#devices-table .check-this:checkbox:checked").length == 0) {
            $("#check-all").prop("checked", false);
        }

    });

    // FIXME - this is to gray out the send/remove buttons when no checkbox is checked
    /*$("#devices-table input[type=checkbox]").click(function () {
        if ($("#devices-table input:checkbox:checked").length > 0) {
            alert("one checked");
        } else {
            alert("no one checked");
        }
    });*/

});