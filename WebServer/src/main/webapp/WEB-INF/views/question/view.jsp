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
    <title>Bootstrap 101 Template</title>

    <link href="${resources}/css/bootstrap/bootstrap.min.css?ts=1" rel="stylesheet">
    <link href="${resources}/css/sticky-footer-navbar.css" rel="stylesheet">
    <link href="${resources}/css/toastr/toastr.min.css" rel="stylesheet">
    <link href="${resources}/css/select/bootstrap-select.min.css" rel="stylesheet">

    <style type="text/css">
        button.alternative {
            width: 2.5em;
        }

        .space {
            line-height: 0em;
        }
    </style>
  </head>
  <body>
    <div class="container">
      <jsp:include page="../header.jsp" />

      <div class="row">
        <div class="col-md-12">
          <div class="table-responsive">
            <table id="questions-table" class="table table-striped">
              <thead>
                <tr>
                  <th><spring:message code="label.question.view.questionColumn"/></th>
                  <th><spring:message code="label.question.view.typeColumn"/></th>
                  <th><spring:message code="label.question.view.formatColumn"/></th>
                  <th><spring:message code="label.question.view.localeColumn"/></th>
                  <th><spring:message code="label.question.view.editColumn"/></th>
                  <th><spring:message code="label.question.view.removeColumn"/></th>
                </tr>
              </thead>
              <tbody>
                <c:forEach items="${questions}" var="question">
                  <tr id="${question.id}">
                    <td>${question.text}</td>
                    <td>${question.type}</td>
                    <td>${question.format}
                      <c:forEach items="${question.alternativeList}" var="alternative">
                        <input type="hidden" value="${alternative.text}" />
                      </c:forEach>
                    </td>
                    <td>${question.locale}</td>
                    <td>
                      <p data-placement="top" data-toggle="tooltip" title="Edit">
                        <button class="btn btn-primary btn-xs" data-title="Edit"
                            data-toggle="modal" data-target="#edit">
                          <span class="glyphicon glyphicon-pencil"></span>
                        </button>
                      </p>
                    </td>
                    <td>
                      <p data-placement="top" data-toggle="tooltip" title="Delete">
                        <button class="btn btn-danger btn-xs" data-title="Delete" data-toggle="modal" data-target="#delete">
                          <span class="glyphicon glyphicon-trash"></span>
                        </button>
                      </p>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="edit" tabindex="-1" role="dialog" aria-labelledby="edit" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
              <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
            </button>
            <h4 class="modal-title custom_align" id="Heading">
              <spring:message code="label.question.view.editQuestion"/>
            </h4>
          </div>
          <div class="modal-body">
            <form id="question-form" class="form-horizontal" role="form">
              <input type="hidden" id="question-id">
              <div class="form-group">
                <div class="col-xs-12">
                  <label class="control-label" for="question-text">
                    <spring:message code="label.question.view.questionColumn"/>
                  </label>
                  <input type="text" class="form-control" id="question-text" name="question-text">
                </div>
              </div>
              <div class="form-group">
                <div class="col-xs-6 selectContainer">
                  <label class="control-label">
                    <spring:message code="label.question.view.typeColumn"/>
                  </label>
                  <select class="form-control selectpicker" id="question-type" data-size="${fn:length(types)}">
                    <c:forEach items="${types}" var="type">
                     <option>${type}</option>
                    </c:forEach>
                  </select>
                </div>
                <div class="col-xs-6 selectContainer">
                  <label class="control-label">
                    <spring:message code="label.question.view.formatColumn"/>
                  </label>
                  <select class="form-control selectpicker" id="question-format" data-size="${fn:length(formats)}">
                    <c:forEach items="${formats}" var="format">
                     <option>${format}</option>
                    </c:forEach>
                  </select>
                </div>
              </div>
              <div class="form-group" id="question-alternatives">
                <div class="col-xs-6">
                  <div class="input-group">
                    <label class="control-label">
                      <spring:message code="label.question.view.alternatives"/>
                    </label>
                    <span class="input-group-btn">
                      <button class="btn btn-default alternative add" type="button">+</button>
                    </span>
                  </div>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer ">
            <button class="btn btn-warning btn-lg" id="submit-question" style="width: 100%;"
                data-loading-text="<spring:message code="label.question.view.updating"/>" >
              <span class="glyphicon glyphicon-ok-sign"></span>
              <spring:message code="label.question.view.update"/>
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="delete" tabindex="-1" role="dialog" aria-labelledby="edit" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">
              <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
            </button>
            <h4 class="modal-title custom_align" id="Heading">
              <spring:message code="label.question.view.removeQuestion"/>
            </h4>
          </div>
          <div class="modal-body">
            <div class="alert alert-danger">
              <span class="glyphicon glyphicon-warning-sign"></span>
              <spring:message code="label.question.view.removeConfirmation"/>
            </div>
          </div>
          <div class="modal-footer ">
            <button value="Submit" class="btn btn-success" id="delete-question">
              <span class="glyphicon glyphicon-ok-sign"></span>
              <spring:message code="label.question.view.yes"/>
            </button>
            <button type="button" class="btn btn-default" data-dismiss="modal">
              <span class="glyphicon glyphicon-remove"></span>
              <spring:message code="label.question.view.no"/>
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
    <script src="${resources}/js/question/view.js"></script>
  </body>
</html>