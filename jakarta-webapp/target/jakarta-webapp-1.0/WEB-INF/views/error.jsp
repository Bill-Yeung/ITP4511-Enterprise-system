<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true"%>
<%
    String ctx = request.getContextPath();
    String errorTitle = (String) request.getAttribute("errorTitle");
    String errorMsg = (String) request.getAttribute("errorMsg");
    String backUrl = (String) request.getAttribute("backUrl");

    if (errorTitle == null || errorTitle.isEmpty()) {
        errorTitle = "Page Error";
    }
    if (errorMsg == null || errorMsg.isEmpty()) {
        errorMsg = exception != null && exception.getMessage() != null
                ? exception.getMessage()
                : "The requested page cannot be displayed.";
    }
    if (backUrl == null || backUrl.isEmpty()) {
        backUrl = ctx + "/home";
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= errorTitle %> - CCHC Clinic System</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>

<body>

    <jsp:include page="/WEB-INF/views/header.jsp" />

    <main class="profile-main">
        <div class="card card--narrow">
            <div class="card-header"><%= errorTitle %></div>
            <div class="card-body">
                <div class="alert alert-error"><%= errorMsg %></div>
                <a class="btn btn-outline" href="<%= backUrl %>">Back</a>
            </div>
        </div>
    </main>

    <jsp:include page="/WEB-INF/views/footer.jsp" />

</body>
</html>
