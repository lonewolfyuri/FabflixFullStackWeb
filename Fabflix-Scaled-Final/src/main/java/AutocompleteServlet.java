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
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

@WebServlet("/movie-suggestion")
public class AutocompleteServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    private static JsonObject generateJsonObject(String id, String title, int year) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("id", id);
        dataObject.addProperty("year", year);

        jsonObject.add("data", dataObject);
        return jsonObject;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection dbcon = null;
        long startTimeTS = System.nanoTime(), startTimeTJ = 0, endTimeTS = 0, endTimeTJ = 0, elapsedTimeTS = 0, elapsedTimeTJ = 0;
        try {
            PrintWriter out = response.getWriter();
            JsonArray jsonArray = new JsonArray();

            HttpSession sess = request.getSession();
            Map<String, JsonArray> queries = (HashMap<String, JsonArray>) sess.getAttribute("queries");
            if (queries == null || queries.size() >= 100) queries = new HashMap<>();

            String query = request.getParameter("query");

            if (query == null || query.trim().isEmpty()) {
                out.write(jsonArray.toString());
                return;
            }

            if (queries.containsKey(query)) {
                out.write(queries.get(query).toString());
                return;
            }

            String original_query = query;
            String[] terms = null;
            if (query.contains(" ")) terms = query.split(" ");
            if (terms != null) {
                query = "";
                for (String term : terms) query += "+" + term + "* ";
            } else query = "+" + query + "* ";

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");

            dbcon = dataSource.getConnection();
            PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10;");
            statement.setString(1, query);

            startTimeTJ = System.nanoTime();
            ResultSet rs = statement.executeQuery();
            endTimeTJ = System.nanoTime();
            elapsedTimeTJ = endTimeTJ - startTimeTJ;

            while (rs.next()) {
                jsonArray.add(generateJsonObject(rs.getString("id"), rs.getString("title"), rs.getInt("year")));
            }

            queries.put(original_query, jsonArray);
            sess.setAttribute("queries", queries);
            out.write(jsonArray.toString());

            rs.close();
            out.close();
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        } finally {
            try {
                dbcon.close();
            } catch (Exception e) {
                System.out.println();
            }
        }

        endTimeTS = System.nanoTime();
        elapsedTimeTS = endTimeTS - startTimeTS;
        if (elapsedTimeTJ != 0) {
            log_generator log_gen = new log_generator(getServletContext().getRealPath("/") + "log_processing.txt");
            log_gen.output_time(elapsedTimeTS, elapsedTimeTJ);
        }
    }
}
