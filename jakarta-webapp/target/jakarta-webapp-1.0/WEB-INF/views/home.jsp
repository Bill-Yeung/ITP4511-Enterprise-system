<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CCHC &mdash; Community Care Health Consortium</title>
    <link rel="icon" type="image/svg+xml" href="<%= ctx %>/cchc.svg">
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

    <jsp:include page="/WEB-INF/views/header.jsp"/>

    <div class="landing-hero">
        <div class="landing-logo">CCHC</div>
        <h1>Community Care Health Consortium</h1>
        <p class="landing-hero-sub">Appointment &amp; Queue System</p>
        <p>Book appointments, join walk-in queues, and manage your healthcare across 5 community clinics in Hong Kong.</p>
        <div class="landing-actions">
            <a href="<%= ctx %>/login" class="btn btn-white">Login</a>
            <a href="<%= ctx %>/register" class="btn btn-outline-white">Register</a>
        </div>
    </div>

    <div id="services" class="landing-services">
        <h2>Our Services</h2>
        <p>Quality healthcare services available at all our community clinics.</p>
        <div class="service-grid">
            <div class="service-card">
                <div class="service-icon">+</div>
                <h3>General Consultation</h3>
                <p>Outpatient consultation with a general practitioner for common illnesses and follow-ups.</p>
            </div>
            <div class="service-card">
                <div class="service-icon">V</div>
                <h3>Vaccination</h3>
                <p>Flu, Hepatitis B, and COVID-19 booster vaccinations available.</p>
            </div>
            <div class="service-card">
                <div class="service-icon">H</div>
                <h3>Health Screening</h3>
                <p>Blood pressure, BMI, blood glucose, and cholesterol checks.</p>
            </div>
            <div class="service-card">
                <div class="service-icon">B</div>
                <h3>Blood Test</h3>
                <p>Fasting or non-fasting blood draw for laboratory analysis.</p>
            </div>
        </div>
    </div>

    <div id="clinics" class="landing-clinics">
        <h2>Our Clinics</h2>
        <div class="clinic-list">
            <div class="clinic-item">
                <h3>Chai Wan</h3>
                <p>Mon-Fri 08:00-18:00<br>Sat 08:00-13:00</p>
            </div>
            <div class="clinic-item">
                <h3>Tseung Kwan O</h3>
                <p>Mon-Fri 08:00-18:00<br>Sat 08:00-13:00</p>
            </div>
            <div class="clinic-item">
                <h3>Sha Tin</h3>
                <p>Mon-Fri 08:00-17:00<br>Sat 08:00-13:00</p>
            </div>
            <div class="clinic-item">
                <h3>Tuen Mun</h3>
                <p>Mon-Fri 09:00-18:00</p>
            </div>
            <div class="clinic-item">
                <h3>Tsing Yi</h3>
                <p>Mon-Fri 09:00-17:00</p>
            </div>
        </div>
    </div>

    <jsp:include page="/WEB-INF/views/footer.jsp"/>

</body>
</html>
