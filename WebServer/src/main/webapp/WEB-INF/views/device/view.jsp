<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="resources" scope="request">../resources</c:set>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><spring:message code="label.device.view.title"/></title>

    <link href="${resources}/css/bootstrap/bootstrap.min.css?ts=1" rel="stylesheet">
    <link href="${resources}/css/sticky-footer-navbar.css" rel="stylesheet">
    <link href="${resources}/css/toastr/toastr.min.css" rel="stylesheet">
    <link href="${resources}/css/select/bootstrap-select.min.css" rel="stylesheet">

  </head>
  <body>
    <div class="container">
      <jsp:include page="../header.jsp" />

      <div class="row">
        <div class="col-md-12">
          <div class="table-responsive">
            <table id="devices-table" class="table table-striped">
              <thead>
                <tr>
                  <th><input type="checkbox" id="check-all" /></th>
                  <th><spring:message code="label.device.view.emailColumn"/></th>
                  <th><spring:message code="label.device.view.tokenColumn"/></th>
                </tr>
              </thead>
              <tbody>
                <c:forEach items="${users}" var="user">
                  <tr>
                    <td>
                      <input type="checkbox" class="check-this">
                    </td>
                    <td>${user.email}</td>
                    <td>${user.device.token}</td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="row" style="float: right;">
        <div class="col-xs-6"><br>
          <p data-placement="top" data-toggle="tooltip" title="Send">
            <button class="btn btn-primary btn-xs" data-title="Send" data-toggle="modal"
                data-target="#send" id="send-button" disabled>
              Send Message <span class="glyphicon glyphicon-pencil"></span>
            </button>
          </p>
        </div>
      </div>
    </div>

    <div class="modal fade" id="send" tabindex="-1" role="dialog" aria-labelledby="send" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
              <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
            </button>
            <h4 class="modal-title custom_align" id="Heading">
              <spring:message code="label.device.view.sendMessageTitle"/>
            </h4>
          </div>
          <div class="modal-body">
            <form id="device-form" class="form-horizontal" role="form">
              <div class="form-group">
                <div class="col-xs-12">
                  <label class="control-label" for="device-message">
                    <spring:message code="label.device.view.sendMessageEditText"/>
                  </label>
                  <input type="text" class="form-control" id="device-message" name="device-message">
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer ">
            <button class="btn btn-warning btn-lg" id="send-message" style="width: 100%;"
                data-loading-text="<spring:message code="label.device.view.sending"/>" >
              <span class="glyphicon glyphicon-ok-sign"></span>
              <spring:message code="label.device.view.sendMessage"/>
            </button>
          </div>
        </div>
      </div>
    </div>

    <jsp:include page="../footer.jsp" />

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="${resources}/js/bootstrap/bootstrap.min.js"></script>
    <script src="${resources}/js/toastr/toastr.min.js"></script>
    <script src="${resources}/js/select/bootstrap-select.min.js"></script>
    <script src="${resources}/js/device/view.js"></script>
  </body>
</html>