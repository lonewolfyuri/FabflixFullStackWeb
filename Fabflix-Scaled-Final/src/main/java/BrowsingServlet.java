package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "BrowsingServlet", urlPatterns="/api/browse")
public class BrowsingServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection dbcon = null;
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
            dbcon = dataSource.getConnection();

            String query = "SELECT DISTINCT name FROM genres ORDER BY name ASC";

            PreparedStatement statement = dbcon.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while(rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre", rs.getString("name"));
                jsonArray.add(jsonObject);
            }

            out.write(jsonArray.toString());
            response.setStatus(200);

            rs.close();
            statement.close();
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

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
