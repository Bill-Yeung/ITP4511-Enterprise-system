<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
%>

<footer class="site-footer">

    <div class="footer-inner">

        <div class="footer-brand">
            <div class="footer-logo">CCHC</div>
            <p class="footer-brand-name">Community Care Health Consortium</p>
            <p class="footer-brand-desc">Quality healthcare services across 5 community clinics in Hong Kong.</p>
        </div>

        <div class="footer-col">
            <h4>Services</h4>
            <ul>
                <li><a href="<%= ctx %>/home#services">General Consultation</a></li>
                <li><a href="<%= ctx %>/home#services">Vaccination</a></li>
                <li><a href="<%= ctx %>/home#services">Health Screening</a></li>
                <li><a href="<%= ctx %>/home#services">Blood Test</a></li>
            </ul>
        </div>

        <div class="footer-col">
            <h4>Our Clinics</h4>
            <ul>
                <li><a href="<%= ctx %>/home#clinics">Chai Wan</a></li>
                <li><a href="<%= ctx %>/home#clinics">Tseung Kwan O</a></li>
                <li><a href="<%= ctx %>/home#clinics">Sha Tin</a></li>
                <li><a href="<%= ctx %>/home#clinics">Tuen Mun</a></li>
                <li><a href="<%= ctx %>/home#clinics">Tsing Yi</a></li>
            </ul>
        </div>

        <div class="footer-col">
            <h4>Quick Links</h4>
            <ul>
                <li><a href="<%= ctx %>/login">Login</a></li>
                <li><a href="<%= ctx %>/register">Register</a></li>
                <li><a href="<%= ctx %>/forgot-password">Reset Password</a></li>
                <li><a href="<%= ctx %>/home">Home</a></li>
            </ul>
        </div>
        
    </div>

    <div class="footer-bottom">
        <p>&copy; 2026 Community Care Health Consortium (CCHC). All rights reserved.</p>
        <p>ITP4511 Enterprise System Development &mdash; HKIIT</p>
    </div>

</footer>
