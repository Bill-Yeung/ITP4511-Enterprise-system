package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;

import hk.edu.hkiit.jakarta.webapp.bean.UserBean;
import hk.edu.hkiit.jakarta.webapp.dao.OtpDAO;
import hk.edu.hkiit.jakarta.webapp.dao.UserDAO;
import hk.edu.hkiit.jakarta.webapp.util.OtpUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();
    private OtpDAO otpDAO = new OtpDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If already logged in, redirect away
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            String reactUrl = getServletContext().getInitParameter("reactUrl");
            response.sendRedirect(reactUrl + "/profile");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMsg", "Please enter your email address.");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            return;
        }

        email = email.trim();

        UserBean user = userDAO.getUserByEmail(email);
        if (user == null) {
            request.setAttribute("errorMsg", "No account found with that email address.");
            request.getRequestDispatcher("/WEB-INF/views/forgot-password.jsp").forward(request, response);
            return;
        }

        String otp = OtpUtil.generateOtp();
        otpDAO.saveOtp(user.getUserId(), otp);
        OtpUtil.sendOtpEmail(email, otp, "Password Reset");

        HttpSession session = request.getSession(true);
        session.setAttribute("otpUserId", user.getUserId());
        session.setAttribute("otpEmail",  email);
        session.setAttribute("otpAction", "forgot-password");

        response.sendRedirect(request.getContextPath() + "/otp-verify");
    }
}
