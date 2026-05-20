<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String loggedInUser = (String) session.getAttribute("fullName");
    String userRole = (String) session.getAttribute("role");
    String ctx = request.getContextPath();
%>

<header class="site-header">

    <a href="<%= ctx %>/home" class="header-brand">
        <span class="header-logo">CCHC</span>
        <span class="header-brand-text">
            <span class="header-brand-name">CCHC Clinic System</span>
            <span class="header-brand-sub">Appointment &amp; Queue System</span>
        </span>
    </a>

    <nav class="header-nav">
        <% if (loggedInUser == null) { %>
            <a href="<%= ctx %>/home" class="header-nav-link">Home</a>
            <a href="<%= ctx %>/home#services" class="header-nav-link">Services</a>
            <a href="<%= ctx %>/home#clinics" class="header-nav-link">Clinics</a>
        <% } %>
    </nav>

    <div class="header-actions">
        <% if (loggedInUser != null) { %>
            <span class="header-user-name"><%= loggedInUser %></span>
            <span class="header-user-role"><%= userRole %></span>
            <a href="<%= ctx %>/logout" class="header-logout-btn">Logout</a>
        <% } else { %>
            <a href="<%= ctx %>/login" class="header-nav-link header-nav-link--accent">Log in</a>
            <a href="<%= ctx %>/register" class="btn btn-sm btn-primary">Register</a>
        <% } %>
    </div>

</header>
