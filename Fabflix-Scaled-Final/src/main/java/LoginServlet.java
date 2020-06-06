package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection dbcon = null;
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean app = Boolean.parseBoolean(request.getParameter("app"));

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        JsonObject responseJsonObject = new JsonObject();

        // Verify reCAPTCHA
        if (!app) {
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                System.out.println("Recaptcha Error");
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "reCaptcha Failed, Please Try Again.");
                response.setStatus(500);
                out.write(responseJsonObject.toString());
                out.close();
                return;
            }
        }

        System.out.println("User: " + username + " ~ Pass: " + password);

        if (username.equals("anteater@uci.edu") && password.equals("123456")) {
            request.getSession(true).setAttribute("user", new User(username));
            request.getSession().setAttribute("admin", true);

            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");

            response.setStatus(200);
            System.out.println(responseJsonObject.toString());
            out.write(responseJsonObject.toString());
        } else {
            try {
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
                dbcon = dataSource.getConnection();

                String query = "SELECT email, password FROM employees WHERE email = ?";

                PreparedStatement statement = dbcon.prepareStatement(query);

                statement.setString(1, username);

                ResultSet rs = statement.executeQuery();

                boolean success = false;
                boolean isValid = false;
                while (rs.next()) {
                    String encryptedPassword = rs.getString("password");
                    success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                    if (success) isValid = true;
                }

                if (isValid) {
                    request.getSession(true).setAttribute("user", new User(username));
                    request.getSession().setAttribute("admin", true);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                    response.setStatus(200);
                } else {
                    query = "SELECT email, password FROM customers WHERE email = ?";
                    statement = dbcon.prepareStatement(query);
                    statement.setString(1, username);
                    rs = statement.executeQuery();

                    success = false;
                    isValid = false;
                    while (rs.next()) {
                        String encryptedPassword = rs.getString("password");
                        success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                        if (success) isValid = true;
                    }

                    if (isValid) {
                        request.getSession(true).setAttribute("user", new User(username));
                        request.getSession().setAttribute("admin", false);
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                        response.setStatus(200);
                    } else {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Invalid Password, Please Try Again.");
                    }

                }

                out.write(responseJsonObject.toString());

                rs.close();
                statement.close();
            } catch (Exception e) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid Password, Please Try Again.");
                out.write(responseJsonObject.toString());
                response.setStatus(500);
            } finally {
                try {
                    dbcon.close();
                } catch (Exception e) {
                    System.out.println();
                }

            }

            out.close();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("status", "fail");
        responseJsonObject.addProperty("message", "Made a GET Request to a POST only Servlet.");
        response.getWriter().write(responseJsonObject.toString());
        response.getWriter().close();
    }
}
