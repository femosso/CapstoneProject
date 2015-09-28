<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <link href="${resources}/css/datepicker/datepicker3.css" rel="stylesheet">

  </head>
  <body>
    <div class="container">
      <jsp:include page="../header.jsp" />

      <div id="signupbox" class="mainbox col-md-6 col-md-offset-3 col-sm-8 col-sm-offset-2">
        <div class="panel panel-info">
          <div class="panel-heading">
            <div class="panel-title">Sign Up</div>
          </div>
          <div class="panel-body">
            <form id="signupform" class="form-horizontal" role="form">
              <div class="form-group">
                <div class="col-xs-6">
                  <label for="firstname" class="control-label">
                    <spring:message code="label.register.firstname"/>
                  </label>
                  <input type="text" class="form-control" id="firstname" name="firstname"
                      placeholder="<spring:message code="label.register.firstname"/>"/>
                </div>
                <div class="col-xs-6">
                  <label for="lastname" class="control-label">
                    <spring:message code="label.register.lastname"/>
                  </label>
                  <input type="text" class="form-control" id=lastname name="lastname"
                      placeholder="<spring:message code="label.register.lastname"/>"/>
                </div>
              </div>
              <div class="form-group">
                <div class="col-xs-8">
                  <label for="email" class="control-label">
                    <spring:message code="label.register.email"/>
                  </label>
                  <input type="email" class="form-control" id="email" name="email"
                      placeholder="<spring:message code="label.register.email"/>"/>
                </div>
                <div class="col-xs-4">
                  <label for="birthday" class="control-label">
                    <spring:message code="label.register.birthday"/>
                  </label>
                  <div id="datepicker-container">
                    <input type="text" class="form-control" id="birthday" name="birthday"
                        placeholder="<spring:message code="label.register.birthday"/>">
                  </div>
                </div>
              </div>
              <div class="form-group">
                <div class="col-xs-6">
                  <label for="password" class="control-label">
                    <spring:message code="label.register.password"/>
                  </label>
                  <input type="password" class="form-control" name="password" id="password"
                      placeholder="<spring:message code="label.register.password"/>">
                </div>
                <div class="col-xs-6">
                  <label for="confirm" class="control-label">
                    <spring:message code="label.register.confirm"/>
                  </label>
                  <input type="password" class="form-control" name="confirm" id="confirm"
                      placeholder="<spring:message code="label.register.confirm"/>"/>
                </div>
              </div>
              <div class="form-group" style="border-top: 1px solid #999; padding-top: 20px">
                <div class="col-xs-6">
                  <div class="checkbox">
                    <label><input type="checkbox" id="check-monitor" checked>Option 1</label>
                  </div>
                </div>
                <div class="col-xs-6">
                  <label for="medicalNumber" class="control-label">
                    <spring:message code="label.register.medicalNumber"/>
                  </label>
                  <input type="text" class="form-control" id="medical-number" name="medical-number"
                      placeholder="<spring:message code="label.register.medicalNumber"/>"/>
                </div>
              </div>
              <div class="form-group">
                <div class="col-xs-6">
                  <button id="sign-up" type="button" class="btn btn-info btn-block">
                    <i class="icon-hand-right"></i> <spring:message code="label.register.signUp"/>
                  </button>
                </div>
                <div class="col-xs-6">
                  <input class="btn btn-primary btn-block" type="button" id="sign-up-facebook"
                      value="<spring:message code="label.register.signUpFacebook"/>"
                      data-loading-text="<spring:message code="label.register.signingUp"/>">
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <jsp:include page="../footer.jsp" />

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="${resources}/js/bootstrap/bootstrap.min.js"></script>
    <script src="${resources}/js/cryptojs/md5.js"></script>
    <script src="${resources}/js/toastr/toastr.min.js"></script>
    <script src="${resources}/js/datepicker/bootstrap-datepicker.js"></script>
    <script src="${resources}/js/datepicker/locales/bootstrap-datepicker.pt-BR.js"></script>
    <script src="${resources}/js/login/register.js"></script>
    <script src="${resources}/js/login/box.js"></script>
  </body>
</html>