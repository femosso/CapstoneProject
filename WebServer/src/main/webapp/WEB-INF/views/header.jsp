<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="ctx">${pageContext.request.contextPath}</c:set>

<nav class="navbar navbar-default">
  <div class="container-fluid">
    <div class="navbar-header">
      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
        data-target="#navbar" aria-expanded="false" aria-controls="navbar">
        <span class="sr-only">Toggle navigation</span> <span
          class="icon-bar"></span> <span class="icon-bar"></span> <span
          class="icon-bar"></span>
      </button>
      <a class="navbar-brand" href="${ctx}"><spring:message code="label.header.projectName"/></a>
    </div>
    <div id="navbar" class="navbar-collapse collapse">
      <ul class="nav navbar-nav">
        <li id="home"><a href="${ctx}"><spring:message code="label.header.home"/></a></li>
        <c:choose>
          <c:when test="${loggedUser.type == 0}">
            <li id="question" class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                <spring:message code="label.header.question"/><span class="caret"></span>
              </a>
              <ul class="dropdown-menu" role="menu">
                <li><a href="${ctx}/question/register"><spring:message code="label.header.addQuestion"/></a></li>
                <li><a href="${ctx}/question/view"><spring:message code="label.header.viewRemoveQuestions"/></a></li>
              </ul>
            </li>
            <li id="device" class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                <spring:message code="label.header.device"/><span class="caret"></span>
              </a>
              <ul class="dropdown-menu" role="menu">
                <li><a href="${ctx}/device/view"><spring:message code="label.header.viewDevices"/></a></li>
              </ul>
            </li>
          </c:when>
        </c:choose>
      </ul>
      <ul class="nav navbar-nav navbar-right">
        <c:choose>
          <c:when test="${loggedUser == null}">
            <li id="register"><a href="${ctx}/login/register"><spring:message code="label.header.signUp"/></a></li>
          </c:when>
        </c:choose>
        <li class="dropdown">
          <c:choose>
            <c:when test="${loggedUser == null}">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                <spring:message code="label.header.signIn"/> <b class="caret"></b>
              </a>
              <jsp:include page="login/box.jsp" />
            </c:when>
            <c:otherwise>
              <a href="#" id="drop3" role="button" class="dropdown-toggle navbar-link"
                data-toggle="dropdown">${loggedUser.email}<b class="caret"></b></a>
              <ul class="dropdown-menu" aria-labelledby="drop3">
                <li>
                  <a tabindex="-1" href="${ctx}/login/logout" onclick="logout();">
                    <spring:message code="label.header.logout"/>
                  </a>
                </li>
              </ul>
            </c:otherwise>
          </c:choose>
        </li>
        <li class="dropdown">
          <c:set var="locale">${pageContext.response.locale}</c:set>
          <c:choose>
            <c:when test="${locale == 'pt_BR'}">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                <img src="${resources}/img/br.svg" width="22px" height="14px"><span class="caret"></span>
              </a>
            </c:when>
            <c:otherwise>
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
                <img src="${resources}/img/gb.svg" width="22px" height="14px"><span class="caret"></span>
              </a>
            </c:otherwise>
          </c:choose>
          <ul class="dropdown-menu language" role="menu">
            <li><a href="?locale=en"><img src="${resources}/img/gb.svg" width="22px" height="14px"> English</a></li>
            <li><a href="?locale=pt_BR"><img src="${resources}/img/br.svg" width="22px" height="14px"> PortuguÍs</a></li>
          </ul>
        </li>
      </ul>
    </div>
  </div>
</nav>