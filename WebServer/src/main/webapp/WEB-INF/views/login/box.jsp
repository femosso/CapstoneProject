<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="ctx">${pageContext.request.contextPath}</c:set>

<ul class="dropdown-menu" style="padding: 15px; min-width: 275px;">
  <li>
    <div class="row">
      <div class="col-md-12">
        <form class="form" role="form" method="post" action="${ctx}/login/submit" accept-charset="UTF-8" id="login-nav">
          <div class="form-group">
            <div class="input-group">
              <span class="input-group-addon"><i class="glyphicon glyphicon-user"></i></span>
              <input class="form-control" id="email" placeholder="<spring:message code="label.box.email"/>" name="nome" type="text" autofocus="">
            </div>
          </div>
          <div class="form-group">
            <div class="input-group">
              <span class="input-group-addon"><i class="glyphicon glyphicon-lock"></i></span>
              <input class="form-control" id="password" placeholder="<spring:message code="label.box.password"/>" name="password" type="password" value="">
            </div>
          </div>
          <div class="checkbox">
            <label><input type="checkbox"> <spring:message code="label.box.rememberMe"/></label>
          </div>
          <div class="form-group">
            <button type="button" class="btn btn-success btn-block"
              id="saveLogin" data-loading-text="<spring:message code="label.box.signingIn"/>">
              <spring:message code="label.box.signIn" />
            </button>
          </div>
        </form>
      </div>
    </div>
  </li>
  <li class="divider"></li>
  <li>
    <input class="btn btn-primary btn-block" type="button" id="sign-in-google" value="Sign In with Google" disabled>
    <input class="btn btn-primary btn-block" type="button" id="sign-in-twitter" value="Sign In with Twitter" disabled>
  </li>
</ul>