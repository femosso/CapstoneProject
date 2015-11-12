<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="resources" scope="request">resources</c:set>

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

  </head>
  <body>
    <div class="container">
      <jsp:include page="header.jsp" />

      <div class="jumbotron">
        <c:choose>
          <c:when test="${loggedUser.type == 0}">
            <h1><spring:message code="label.home.welcome"/></h1>
            <p><spring:message code="label.home.welcomeDescription"/></p>
          </c:when>
          <c:otherwise>
            <h1><spring:message code="label.home.projectFullName"/></h1>
            <p><spring:message code="label.home.welcomeDescription"/></p>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <jsp:include page="footer.jsp" />

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="${resources}/js/bootstrap/bootstrap.min.js"></script>
    <script src="${resources}/js/cryptojs/md5.js"></script>
    <script src="${resources}/js/toastr/toastr.min.js"></script>
    <script src="${resources}/js/login/box.js"></script>
    <script>
      $("#home").addClass("active");
    </script>
  </body>
</html>