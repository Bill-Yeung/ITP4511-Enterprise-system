<%-- Redirect root URL to home page --%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<% response.sendRedirect(request.getContextPath() + "/home"); %>
