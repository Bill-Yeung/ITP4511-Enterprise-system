package hk.edu.hkiit.jakarta.webapp.controller;

import java.io.IOException;

import hk.edu.hkiit.jakarta.webapp.db.DBConnection;
import hk.edu.hkiit.jakarta.webapp.util.EncryptionUtil;
import hk.edu.hkiit.jakarta.webapp.util.OtpUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {

        ServletContext ctx = getServletContext();

        DBConnection.init(
            ctx.getInitParameter("dbUrl"),
            ctx.getInitParameter("dbUser"),
            ctx.getInitParameter("dbPassword")
        );

        EncryptionUtil.init(ctx.getInitParameter("aesKey"));
        initOtp(ctx);

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/views/home.jsp")
               .forward(request, response);

    }

    private void initOtp(ServletContext ctx) {

        try {

            String smtpHost = ctx.getInitParameter("smtpHost");
            String smtpPortStr = ctx.getInitParameter("smtpPort");
            String smtpUser = ctx.getInitParameter("smtpUser");
            String smtpPassword = ctx.getInitParameter("smtpPassword");

            if (smtpHost != null && smtpPortStr != null && smtpUser != null && smtpPassword != null) {
                int smtpPort = Integer.parseInt(smtpPortStr);
                OtpUtil.init(smtpHost, smtpPort, smtpUser, smtpPassword);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
