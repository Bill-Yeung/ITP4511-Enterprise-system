package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;

import hk.edu.hkiit.jakarta.webapp.dao.OtpDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "OtpServlet", urlPatterns = {"/otp-verify"})
public class OtpServlet extends HttpServlet {

    private OtpDAO otpDAO = new OtpDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (!hasOtpSession(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/otp-verify.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (!hasOtpSession(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action  = (String) session.getAttribute("otpAction");
        String otpCode = request.getParameter("otpCode");

        if (otpCode == null || otpCode.trim().isEmpty()) {
            request.setAttribute("errorMsg", "Please enter the OTP code.");
            request.getRequestDispatcher("/WEB-INF/views/otp-verify.jsp").forward(request, response);
            return;
        }

        Integer userId = (Integer) session.getAttribute("otpUserId");

        if (!otpDAO.verifyOtp(userId, otpCode.trim())) {
            request.setAttribute("errorMsg", "Invalid or expired OTP. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/otp-verify.jsp").forward(request, response);
            return;
        }

        if ("register".equals(action)) {
            session.removeAttribute("otpUserId");
            session.removeAttribute("otpEmail");
            session.removeAttribute("otpAction");
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
            return;
        }

        if ("forgot-password".equals(action)) {
            session.removeAttribute("otpEmail");
            session.removeAttribute("otpAction");
            session.setAttribute("resetUserId", userId);
            session.removeAttribute("otpUserId");
            response.sendRedirect(request.getContextPath() + "/reset-password");
            return;
        }

        // Unknown action - redirect to login
        session.invalidate();
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private boolean hasOtpSession(HttpSession session) {
        if (session == null) return false;

        Object userId = session.getAttribute("otpUserId");
        String email = (String) session.getAttribute("otpEmail");
        String action = (String) session.getAttribute("otpAction");

        return userId != null
                && email != null && !email.isEmpty()
                && action != null && !action.isEmpty();
    }
}
