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

@WebServlet(name = "StarPageServlet", urlPatterns="/api/singleStar")
public class StarPageServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection dbcon = null;
        response.setContentType("application/json");

        String id = request.getParameter("id");
        PrintWriter out = response.getWriter();

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
            dbcon = dataSource.getConnection();

            String query = "SELECT stars.id, stars.name, stars.birthYear, movies.title, movies.id as movieId, movies.year\n" +
                    "FROM stars, stars_in_movies, movies\n" +
                    "WHERE stars.id = stars_in_movies.starId\n" +
                    "\tAND movies.id = stars_in_movies.movieId\n" +
                    "\tAND stars.id = ?\n" +
                    "ORDER BY movies.year DESC, movies.title ASC";

            PreparedStatement statement = dbcon.prepareStatement(query);

            statement.setString(1, id);

            ResultSet rs = statement.executeQuery();

            JsonObject result = new JsonObject();

            JsonArray jsonArray = new JsonArray();

            boolean first = true;
            while(rs.next()) {
                if (first) {
                    result.addProperty("name", rs.getString("name"));
                    result.addProperty("birth", rs.getString("birthYear"));
                    first = false;
                }

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("title", rs.getString("title"));
                jsonObject.addProperty("id", rs.getString("movieId"));

                jsonArray.add(jsonObject);
            }

            result.add("movies", jsonArray);

            out.write(result.toString());
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