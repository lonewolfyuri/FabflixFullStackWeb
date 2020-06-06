package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.annotation.Resource;
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

@WebServlet(name = "LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        request.getSession().removeAttribute("user");
        request.getSession().removeAttribute("admin");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", "success");
        jsonObject.addProperty("message", "success");
        response.setStatus(200);
        out.write(jsonObject.toString());
    }

}
