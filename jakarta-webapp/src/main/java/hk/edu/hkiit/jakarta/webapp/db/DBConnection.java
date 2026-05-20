package hk.edu.hkiit.jakarta.webapp.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static String url;
    private static String username;
    private static String password;

    public static void init(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl + "?useSSL=false&serverTimezone=Asia/Hong_Kong";
        username = dbUser;
        password = dbPassword;
    }

    public static Connection getConnection() throws SQLException, IOException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return DriverManager.getConnection(url, username, password);

    }

}
