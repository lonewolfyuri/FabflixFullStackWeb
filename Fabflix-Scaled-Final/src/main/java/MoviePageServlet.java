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
import java.util.ArrayList;

@WebServlet(name = "MoviePageServlet", urlPatterns="/api/singleMovie")
public class MoviePageServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

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

            String query = "SELECT movies.id, movies.title, movies.year, movies.director, movies.price, genres.name as genreName, genreId, stars.name, stars.id as starId, movieCount, rating\n" +
                    "FROM movies, ratings, stars, genres, genres_in_movies, stars_in_movies,\n" +
                    "\t(SELECT starId, count(movieId) as movieCount\n" +
                    "\tFROM stars_in_movies\n" +
                    "\tGROUP BY starId\n" +
                    "\tORDER BY movieCount DESC) as movie_counts\n" +
                    "WHERE movies.id = stars_in_movies.movieId\n" +
                    "\tAND movies.id = genres_in_movies.movieId\n" +
                    "\tAND ratings.movieId = movies.id\n" +
                    "    AND stars.id = stars_in_movies.starId\n" +
                    "    AND genres.id = genres_in_movies.genreId\n" +
                    "    AND movie_counts.starId = stars.id\n" +
                    "    AND movies.id = ?\n" +
                    "ORDER BY genreName ASC, movieCount DESC, stars.name ASC";

            PreparedStatement statement = dbcon.prepareStatement(query);

            statement.setString(1, id);

            ResultSet rs = statement.executeQuery();

            JsonObject result = new JsonObject();

            JsonArray genreArray = new JsonArray();
            JsonArray starArray = new JsonArray();

            ArrayList<String> seenStars = new ArrayList<>();
            ArrayList<String> seenGenres = new ArrayList<>();

            boolean first = true;
            while(rs.next()) {
                if (first) {
                    result.addProperty("movieId", rs.getString("id"));
                    result.addProperty("movieTitle", rs.getString("title"));
                    result.addProperty("year", rs.getInt("year"));
                    result.addProperty("director", rs.getString("director"));
                    result.addProperty("rating", rs.getDouble("rating"));
                    result.addProperty("price", rs.getDouble("price"));

                    first = false;
                }

                boolean seen = false;
                for (String genre : seenGenres) if (rs.getString("genreName").toLowerCase().equals(genre.toLowerCase())) seen = true;
                if (!seen) {
                    JsonObject genreObject = new JsonObject();
                    genreObject.addProperty("genre", rs.getString("genreName"));
                    genreObject.addProperty("id", rs.getString("genreId"));
                    seenGenres.add(rs.getString("genreName"));
                    genreArray.add(genreObject);
                }

                seen = false;
                for (String star : seenStars) if (rs.getString("name").toLowerCase().equals(star.toLowerCase())) seen = true;
                if (!seen) {
                    JsonObject starObject = new JsonObject();
                    starObject.addProperty("star", rs.getString("name"));
                    starObject.addProperty("id", rs.getString("starId"));
                    seenStars.add(rs.getString("name"));
                    starArray.add(starObject);
                }
            }


            if (first) {
                query = "SELECT movies.id, movies.title, movies.year, movies.director, movies.price, genres.name as genreName, genreId, rating\n" +
                        "FROM movies, ratings, genres, genres_in_movies\n" +
                        "WHERE movies.id = genres_in_movies.movieId\n" +
                        "\tAND ratings.movieId = movies.id\n" +
                        "    AND genres.id = genres_in_movies.genreId\n" +
                        "    AND movies.id = ?\n" +
                        "ORDER BY genreName ASC";

                statement = dbcon.prepareStatement(query);
                statement.setString(1, id);
                rs = statement.executeQuery();

                first = true;
                while (rs.next()) {
                    if (first) {
                        result.addProperty("movieId", rs.getString("id"));
                        result.addProperty("movieTitle", rs.getString("title"));
                        result.addProperty("year", rs.getInt("year"));
                        result.addProperty("director", rs.getString("director"));
                        result.addProperty("rating", rs.getDouble("rating"));
                        result.addProperty("price", rs.getDouble("price"));

                        first = false;
                    }

                    boolean seen = false;
                    for (String genre : seenGenres) if (rs.getString("genreName").toLowerCase().equals(genre.toLowerCase())) seen = true;
                    if (!seen) {
                        JsonObject genreObject = new JsonObject();
                        genreObject.addProperty("genre", rs.getString("genreName"));
                        genreObject.addProperty("id", rs.getString("genreId"));
                        seenGenres.add(rs.getString("genreName"));
                        genreArray.add(genreObject);
                    }
                }
            }

            result.add("genres", genreArray);
            result.add("stars", starArray);

            out.write(result.toString());
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
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
