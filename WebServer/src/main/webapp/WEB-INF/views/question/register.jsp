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
    <title><spring:message code="label.question.register.title"/></title>

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

      <div class="row clearfix">
        <div class="col-md-2 column"></div>
        <div class="col-md-8 column">
          <div class="panel panel-info">
            <div class="panel-heading">
              <h3 class="panel-title">
                <spring:message code="label.question.register.panelTitle"/>
              </h3>
            </div>
            <div class="panel-body">
              <form id="question-form" class="form-horizontal" role="form">
                <div class="form-group">
                  <div class="col-xs-12">
                    <label class="control-label" for="question-text">
                      <spring:message code="label.question.register.questionField"/>
                    </label>
                    <input type="text" class="form-control" id="question-text" name="question-text">
                  </div>
                </div>
                <div class="form-group">
                  <div class="col-xs-6 selectContainer">
                    <label class="control-label">
                      <spring:message code="label.question.register.typeField"/>
                    </label>
                    <select class="form-control selectpicker" id="question-type" data-size="${fn:length(types)}">
                      <c:forEach items="${types}" var="type">
                       <option>${type}</option>
                      </c:forEach>
                    </select>
                  </div>
                  <div class="col-xs-6 selectContainer">
                    <label class="control-label">
                      <spring:message code="label.question.register.formatField"/>
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
                        <spring:message code="label.question.register.alternativesField"/>
                      </label>
                      <span class="input-group-btn">
                        <button class="btn btn-default alternative add" type="button">+</button>
                      </span>
                    </div>
                    <div>
                      <p class='space'>&nbsp;</p>
                      <div class='input-group'>
                        <input type='text' class='form-control alternative-text'>
                        <span class='input-group-btn'>
                          <button class='btn btn-default alternative remove' type='button'>-</button>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                <button class="btn btn-default" id="submit-question" style="float:right"
                    data-loading-text="<spring:message code="label.question.register.submitting"/>" >
                  <i class="icon-hand-right"></i>
                  <spring:message code="label.question.register.submitButton"/>
                </button>
              </form>
            </div>
          </div>
        </div>
        <div class="col-md-2 column"></div>
      </div>
    </div>

    <jsp:include page="../footer.jsp" />

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="${resources}/js/bootstrap/bootstrap.min.js"></script>
    <script src="${resources}/js/toastr/toastr.min.js"></script>
    <script src="${resources}/js/select/bootstrap-select.min.js"></script>
    <script src="${resources}/js/question/register.js"></script>
  </body>
</html>