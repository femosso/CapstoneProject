<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bootstrap 101 Template</title>

    <link href="resources/css/bootstrap/bootstrap.min.css?ts=1" rel="stylesheet">
    <link href="resources/css/sticky-footer-navbar.css" rel="stylesheet">
    <link href="resources/css/toastr/toastr.min.css" rel="stylesheet">

  </head>
  <body>
    <div class="container">
      <c:set var="resources" scope="request">resources</c:set>
      <jsp:include page="header.jsp" />

      <!-- Main component for a primary marketing message or call to action -->
      <div class="jumbotron">
        <h1>Navbar example</h1>
        <p>This example is a quick exercise to illustrate how the
          default, static navbar and fixed to top navbar work. It includes
          the responsive CSS and HTML, so it also adapts to your viewport
          and device.</p>
      </div>
    </div>

    <jsp:include page="footer.jsp" />

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
    <script src="resources/js/bootstrap/bootstrap.min.js"></script>
    <script src="resources/js/cryptojs/md5.js"></script>
    <script src="resources/js/toastr/toastr.min.js"></script>
    <script src="resources/js/login/box.js"></script>
  </body>
</html>