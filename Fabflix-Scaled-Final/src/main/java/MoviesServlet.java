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


@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private String[] search_terms = null;
    private boolean first;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    protected PreparedStatement makeStatement(String query, String input, String title, String director, String star, String browse, String year, String page, String limit, Connection dbcon) throws Exception {
        PreparedStatement result = dbcon.prepareStatement(query);
        int ndx = 1;

        if (input != null) {
            // handle regular search bar
            if (search_terms != null) {
                String terms = "";
                for (String term : search_terms) terms += "+" + term + "* ";
                result.setString(ndx++, terms);
            } else result.setString(ndx++, "+" + input + "*");
        } else if (browse != null) {
            // figure out if browse by genre or browse by title
            if (browse.length() == 1) {
                // handle browse by title
                if (!browse.equals("*")) result.setString(ndx++, browse.toLowerCase() + "%");
            } else {
                // handle browse by genre
                result.setString(ndx++, "%" + browse.toLowerCase() + "%");
            }
        } else {
            // check and handle title
            if (title != null) result.setString(ndx++, "%" + title + "%");
            // check and handle director
            if (director != null) result.setString(ndx++, "%" + director + "%");
            // check and handle star
            if (star != null) result.setString(ndx++, "%" + star + "%");
            // check and handle year
            if (year != null) result.setInt(ndx++, Integer.parseInt(year));
        }

        int lim = 25;
        if (limit != null) lim = Integer.parseInt(limit);
        result.setInt(ndx++, lim);

        if (page != null) result.setInt(ndx++, Integer.parseInt(page) * lim); // query += " OFFSET ?"; // + Integer.parseInt(page) * lim;

        search_terms = null;
        return result;
    }

    // sort = 0 -> Title ASC | Rating ASC
    // sort = 1 -> Title ASC | Rating DESC
    // sort = 2 -> Title DESC | Rating ASC
    // sort = 3 -> Title DESC | Rating DESC
    // sort = 4 -> Rating ASC | Title ASC
    // sort = 5 -> Rating ASC | Title DESC
    // sort = 6 -> Rating DESC | Title ASC
    // sort = 7 -> Rating DESC | Title DESC
    protected String buildQuery(int sort, String input, String title, String director, String star, String browse, String year, String page, String limit) {
        String query = null;

        if (first) {
            query = "SELECT movies.id, movies.title, movies.year, movies.director, movies.price, genreNames as genres, genreIds, starNames as stars, starIds, rating\n" +
                    "FROM movies, ratings, \n" +
                    "\t(SELECT movieId, GROUP_CONCAT(starId) as starIds, GROUP_CONCAT(name) as starNames\n" +
                    "\t\tFROM stars, stars_in_movies\n" +
                    "\t\tWHERE stars.id = stars_in_movies.starId\n" +
                    "\t\tGROUP BY movieId\n" +
                    "    ) as movies_with_stars,\n" +
                    "    (SELECT movieId, GROUP_CONCAT(genreId) as genreIds, GROUP_CONCAT(name) genreNames\n" +
                    "\t\tFROM genres, genres_in_movies\n" +
                    "\t\tWHERE genres.id = genres_in_movies.genreId\n" +
                    "\t\tGROUP BY movieId\n" +
                    "\t) as movies_with_genres\n" +
                    "WHERE movies.id = movies_with_stars.movieId\n" +
                    "\tAND movies.id = movies_with_genres.movieId\n" +
                    "    AND ratings.movieId = movies.id\n";
        } else {
            query = "SELECT movies.id, movies.title, movies.year, movies.director, movies.price, rating\n" +
                    "FROM movies, ratings\n" +
                    "WHERE ratings.movieId = movies.id\n";
        }

        if (input != null) {
            // handle regular search bar
            if (input.contains(" ")) search_terms = input.split(" ");
            query += "\tAND MATCH(movies.title) AGAINST (? IN BOOLEAN MODE)\n";
        } else if (browse != null) {
            // figure out if browse by genre or browse by title
            if (browse.length() == 1) {
                // handle browse by title
                if (browse.equals("*")) query += "\tAND LOWER(movies.title) REGEXP '^[^0-9A-Za-z]'\n";
                else query += "\tAND LOWER(movies.title) LIKE LOWER(?)\n";
            } else {
                // handle browse by genre
                query += "\tAND LOWER(movies_with_genres.genreNames) LIKE LOWER(?)\n";
            }
        } else {
            // check and handle title
            if (title != null) query += "\tAND LOWER(movies.title) LIKE LOWER(?)\n";
            // check and handle director
            if (director != null) query += "\tAND LOWER(movies.director) LIKE LOWER(?)\n";
            // check and handle star
            if (star != null) query += "\tAND LOWER(movies_with_stars.starNames) LIKE LOWER(?)\n";
            // check and handle year
            if (year != null) query += "\tAND movies.year = ?\n";
        }

        switch(sort) {
            case 0:
                query += "ORDER BY movies.title ASC, ratings.rating ASC\n";
                break;
            case 1:
                query += "ORDER BY movies.title ASC, ratings.rating DESC\n";
                break;
            case 2:
                query += "ORDER BY movies.title DESC, ratings.rating ASC\n";
                break;
            case 3:
                query += "ORDER BY movies.title DESC, ratings.rating DESC\n";
                break;
            case 4:
                query += "ORDER BY ratings.rating ASC, movies.title ASC\n";
                break;
            case 5:
                query += "ORDER BY ratings.rating ASC, movies.title DESC\n";
                break;
            case 6:
                query += "ORDER BY ratings.rating DESC, movies.title ASC\n";
                break;
            case 7:
                query += "ORDER BY ratings.rating DESC, movies.title DESC\n";
                break;
            default:
                query += "ORDER BY ratings.rating DESC\n";
        }

        int lim = 25;
        if (limit != null) lim = Integer.parseInt(limit);
        query += "LIMIT ?";

        if (page != null) query += " OFFSET ?"; // + Integer.parseInt(page) * lim;

        query += ";";

        return query;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        first = true;
        Connection dbcon = null;
        long startTimeTS = System.nanoTime(), startTimeTJ = 0, endTimeTS = 0, endTimeTJ = 0, elapsedTimeTS = 0, elapsedTimeTJ = 0;
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        HttpSession sess = request.getSession(true);
        Boolean past = Boolean.parseBoolean(request.getParameter("past"));
        int sort = -1;
        if (request.getParameter("sort") != null) sort = Integer.parseInt(request.getParameter("sort"));
        String input = null, title = null, director = null, star = null, browse = null, year = null, page = null, limit = null;

        if (past) {
            try {
                JsonObject result = new JsonObject();
                Stack<String> history = (Stack<String>) sess.getAttribute("history");
                if (history.empty()) result.addProperty("redir", "movieList.html");
                else result.addProperty("redir", history.peek());

                result.addProperty("status", "success");
                result.addProperty("message", "success");

                out.write(result.toString());
                response.setStatus(200);
            } catch (Exception e) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());

                jsonObject.addProperty("status", "fail");
                jsonObject.addProperty("message", "error in fetching history");

                out.write(jsonObject.toString());

                response.setStatus(500);
            }
            return;
        } else {
            page = request.getParameter("page");
            limit = request.getParameter("limit");

            input = request.getParameter("search_input");
            if (input == null) {
                browse = request.getParameter("genre");
                if (browse == null) browse = request.getParameter("title");
                if (browse == null) {
                    if (request.getParameter("search_title").length() > 0) title = request.getParameter("search_title");
                    if (request.getParameter("search_director").length() > 0) director = request.getParameter("search_director");
                    if (request.getParameter("search_star").length() > 0) star = request.getParameter("search_star");
                    if (request.getParameter("search_year").length() > 0) year = request.getParameter("search_year");
                }
            }
        }

        try {
            Stack<String> history;

            if (sess.getAttribute("history") != null) history = (Stack<String>) sess.getAttribute("history");
            else history = new Stack<>();

            history.push("movieList.html?" + request.getQueryString());
            sess.setAttribute("history", history);

            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");

            dbcon = dataSource.getConnection();

            PreparedStatement statement = makeStatement(buildQuery(sort, input, title, director, star, browse, year, page, limit), input, title, director, star, browse, year, page, limit, dbcon);
            //PreparedStatement statement = dbcon.prepareStatement(buildQuery(sort, input, title, director, star, browse, year, page, limit));

            startTimeTJ = System.nanoTime();
            ResultSet rs = statement.executeQuery();
            endTimeTJ = System.nanoTime();
            elapsedTimeTJ = endTimeTJ - startTimeTJ;

            JsonArray jsonArray = new JsonArray();

            while(rs.next()) {
                if (first) first = false;
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("movieId", rs.getString("id"));
                jsonObject.addProperty("movieTitle", rs.getString("title"));
                jsonObject.addProperty("year", rs.getInt("year"));
                jsonObject.addProperty("director", rs.getString("director"));
                jsonObject.addProperty("price", rs.getString("price"));

                jsonObject.addProperty("genres", rs.getString("genres"));
                jsonObject.addProperty("genreIds", rs.getString("genreIds"));

                jsonObject.addProperty("starNames", rs.getString("stars"));
                jsonObject.addProperty("starIds", rs.getString("starIds"));

                jsonObject.addProperty("rating", rs.getDouble("rating"));

                jsonArray.add(jsonObject);
            }

            if (first) {
                first = false;
                statement = makeStatement(buildQuery(sort, input, title, director, star, browse, year, page, limit), input, title, director, star, browse, year, page, limit, dbcon);
                //PreparedStatement statement = dbcon.prepareStatement(buildQuery(sort, input, title, director, star, browse, year, page, limit));

                startTimeTJ = System.nanoTime();
                rs = statement.executeQuery();
                endTimeTJ = System.nanoTime();
                elapsedTimeTJ += endTimeTJ - startTimeTJ;

                while(rs.next()) {
                    if (first) first = false;
                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("movieId", rs.getString("id"));
                    jsonObject.addProperty("movieTitle", rs.getString("title"));
                    jsonObject.addProperty("year", rs.getInt("year"));
                    jsonObject.addProperty("director", rs.getString("director"));
                    jsonObject.addProperty("price", rs.getString("price"));

                    jsonObject.addProperty("genres", "");
                    jsonObject.addProperty("genreIds", "");

                    jsonObject.addProperty("starNames", "");
                    jsonObject.addProperty("starIds", "");

                    jsonObject.addProperty("rating", rs.getDouble("rating"));

                    jsonArray.add(jsonObject);
                }
            }

            JsonObject result = new JsonObject();

            result.add("movies", jsonArray);
            result.addProperty("query", "movieList.html?" + request.getQueryString());

            if (sort >= 0) result.addProperty("sort", sort);
            else result.addProperty("sort", 0);

            if (page != null) result.addProperty("page", page);
            else result.addProperty("page", 0);

            if (limit != null) result.addProperty("limit", limit);
            else result.addProperty("limit", 25);

            result.addProperty("status", "success");
            result.addProperty("message", "success");

            out.write(result.toString());
            response.setStatus(200);

            rs.close();
            statement.close();
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());

            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("message", "error in fetching query");

            out.write(jsonObject.toString());
            response.setStatus(500);
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

        out.close();
    }
}
