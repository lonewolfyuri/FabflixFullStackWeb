package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@WebServlet(name = "AccountPageServlet", urlPatterns = "/api/account")
public class AccountPageServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection dbcon = null;
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        JsonObject responseObj = new JsonObject();

        HttpSession sess = request.getSession();

        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");

            dbcon = dataSource.getConnection();

            if ((boolean) sess.getAttribute("admin")) {
                responseObj.addProperty("admin", true);

                try {
                    PreparedStatement statement = dbcon.prepareStatement("select TABLE_NAME as name \nfrom information_schema.tables\n\twhere table_schema = 'moviedb';");
                    ResultSet rs = statement.executeQuery();
                    JsonArray tables = new JsonArray();

                    while (rs.next()) {
                        String name = rs.getString("name");
                        if (name != null) {
                            System.out.println("Table Name: " + name);
                            JsonObject tableObj = new JsonObject();
                            tableObj.addProperty("name", name);
                            tables.add(tableObj);
                        }
                    }

                    int len = tables.size();
                    for (int ndx = 0; ndx < len; ndx++) {
                        JsonObject table = (JsonObject) tables.get(ndx);

                        String query = "select column_name as name, data_type as type\nfrom information_schema.columns\nwhere TABLE_SCHEMA = 'moviedb'\n\tAND TABLE_NAME = ?;";

                        statement = dbcon.prepareStatement(query);
                        String val = table.get("name").toString();
                        System.out.println(val);
                        statement.setString(1, val.substring(1, val.length() - 1));

                        rs = statement.executeQuery();

                        JsonArray attributes = new JsonArray();
                        while (rs.next()) {
                            JsonObject attr = new JsonObject();

                            attr.addProperty("name", rs.getString("name"));
                            attr.addProperty("type", rs.getString("type"));

                            attributes.add(attr);
                        }

                        table.add("attributes", attributes);
                        tables.set(ndx, table);
                    }

                    log_processing log_proc = new log_processing(getServletContext().getRealPath("/") + "log_processing.txt");
                    log_proc.process_log();
                    log_proc.output_result(getServletContext().getRealPath("/") + "log_processing_result.txt");
                    responseObj.addProperty("search_avg", log_proc.toString());

                    responseObj.add("tables", tables);
                    responseObj.addProperty("status", "success");
                    responseObj.addProperty("message", "success");
                    response.setStatus(200);

                    rs.close();
                    statement.close();
                } catch (Exception e) {
                    responseObj.addProperty("status", "fail");
                    responseObj.addProperty("message", "Error in Database - Please Try Again Later");
                    response.setStatus(500);
                }

            } else {
                responseObj.addProperty("admin", false);

                try {
                    String query = "SELECT sales.id as id, movies.title as title, movies.price as price, sales.saleDate as date\n" +
                            "FROM sales, customers, movies\n" +
                            "WHERE sales.customerId = customers.id\n" +
                            "\tAND sales.movieId = movies.id\n" +
                            "\tAND LOWER(customers.email) = ?\n" +
                            "ORDER BY date DESC, id DESC\n" +
                            "LIMIT 50;";

                    PreparedStatement statement = dbcon.prepareStatement(query);

                    statement.setString(1, ((User) sess.getAttribute("user")).getUsername().trim().toLowerCase());

                    ResultSet rs = statement.executeQuery();
                    JsonArray sales = new JsonArray();

                    while (rs.next()) {
                        JsonObject sale = new JsonObject();
                        String id = rs.getString("id");
                        sale.addProperty("id", id);
                        String title = rs.getString("title");
                        sale.addProperty("title", title);
                        String price = rs.getString("price");
                        sale.addProperty("price", price);
                        String date = rs.getString("date");
                        sale.addProperty("date", date);
                        System.out.println("Order ID: " + id + " | Movie Title: " + title + " | Price: " + price + " | Date: " + date);
                        sales.add(sale);
                    }

                    responseObj.add("sales", sales);

                    responseObj.addProperty("status", "success");
                    responseObj.addProperty("message", "success");
                    response.setStatus(200);

                    rs.close();
                    statement.close();
                } catch (Exception e) {
                    responseObj.addProperty("status", "fail");
                    responseObj.addProperty("message", "Error in Database - Please Try Again Later");
                    response.setStatus(500);
                }
            }

        } catch (SQLException | NamingException sqle) {
            responseObj.addProperty("status", "fail");
            responseObj.addProperty("message", "No Connection to Database - Please Try Again Later");
            response.setStatus(500);
        } finally {
            try {
                dbcon.close();
            } catch (Exception e) {
                System.out.println();
            }
        }

        out.write(responseObj.toString());
        out.close();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Connection dbcon = null;
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        JsonObject responseObj = new JsonObject();

        String type = request.getQueryString().substring(1).split("=")[1];

        if (type.equals("star")) {

            String name = request.getParameter("star_name");
            String yearStr = request.getParameter("star_birth");
            int year = -1;
            try {
                year = Integer.parseInt(yearStr);
            } catch (Exception e) {
                year = -1;
            }

            try {
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                DataSource dataSource = (DataSource) envContext.lookup("jdbc/master/moviedb");

                dbcon = dataSource.getConnection();

                PreparedStatement statement = dbcon.prepareStatement("select max(id) as id from stars;");
                ResultSet rs = statement.executeQuery();

                String id = "";
                if (rs.next()) {
                    id = rs.getString("id");
                }

                int num = Integer.parseInt(id.substring(2));
                num++;

                id = id.substring(0, 2) + num;

                if (year > 0) {
                    statement = dbcon.prepareStatement("call add_star_full(?, ?, ?)");
                    statement.setString(1, id);
                    statement.setString(2, name);
                    statement.setInt(3, year);
                } else {
                    statement = dbcon.prepareStatement("call add_star(?, ?)");
                    statement.setString(1, id);
                    statement.setString(2, name);
                }

                statement.executeUpdate();
                responseObj.addProperty("name", name);
                responseObj.addProperty("id", id);
                responseObj.addProperty("status", "success");
                responseObj.addProperty("message", "success");
                response.setStatus(200);

            } catch (SQLException | NamingException sqle) {
                responseObj.addProperty("status", "fail");
                responseObj.addProperty("message", "No Connection to Database - Please Try Again Later");
                response.setStatus(500);
            } finally {
                try {
                    dbcon.close();
                } catch (Exception e) {
                    System.out.println();
                }
            }
        } else if (type.equals("movie")) {
            String title = request.getParameter("movie_title");
            int year = Integer.parseInt(request.getParameter("movie_year"));
            String director = request.getParameter("movie_director");
            double price = Double.parseDouble(request.getParameter("movie_price"));
            String star_name = request.getParameter("star_name");
            String genre_name = request.getParameter("genre_name");

            try {
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                DataSource dataSource = (DataSource) envContext.lookup("jdbc/master/moviedb");

                dbcon = dataSource.getConnection();

                PreparedStatement statement = dbcon.prepareStatement("SELECT title FROM movies WHERE LOWER(title) = LOWER(?) AND year = ? AND LOWER(director) = LOWER(?)");
                statement.setString(1, title);
                statement.setInt(2, year);
                statement.setString(3, director);

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    responseObj.addProperty("added", false);
                } else {
                    statement = dbcon.prepareStatement("select max(id) as id from movies;");
                    rs = statement.executeQuery();

                    String id = "";
                    if (rs.next()) id = rs.getString("id");

                    int num = -1, pos = 0;
                    boolean success = false;
                    for (int ndx = 1; ndx < id.length(); ndx++) {
                        try {
                            String temp_id = id.substring(0, ndx);
                            num = Integer.parseInt(id.substring(ndx));
                            success = true;
                            pos = ndx;
                            break;
                        } catch (Exception e) {
                            continue;
                        }
                    }

                    if (success) {
                        id = id.substring(0, pos) + (++num);
                    } else {
                        id += "0";
                    }

                    String star_id = "";

                    statement = dbcon.prepareStatement("select id from stars where lower(name) = lower(?);");
                    statement.setString(1, star_name.trim());
                    rs = statement.executeQuery();

                    if (rs.next()) {
                        star_id = rs.getString("id");
                    } else {
                        statement = dbcon.prepareStatement("select max(id) as id from stars;");
                        rs = statement.executeQuery();

                        if (rs.next()) star_id = rs.getString("id");

                        num = Integer.parseInt(star_id.substring(2));

                        star_id = star_id.substring(0, 2) + (++num);
                    }

                    statement = dbcon.prepareStatement("call add_movie(?, ?, ?, ?, ?, ?, ?, ?);");
                    statement.setString(1, id);
                    statement.setString(2, title);
                    statement.setInt(3, year);
                    statement.setString(4, director);
                    statement.setDouble(5, price);
                    statement.setString(6, star_name);
                    statement.setString(7, star_id);
                    statement.setString(8, genre_name);

                    statement.executeUpdate();
                    responseObj.addProperty("added", true);
                    responseObj.addProperty("id", id);
                    responseObj.addProperty("starId", star_id);

                    statement = dbcon.prepareStatement("select id from genres where lower(name) like lower(?);");
                    statement.setString(1, genre_name);
                    rs = statement.executeQuery();

                    int genre_id = -1;

                    if (rs.next()) {
                        genre_id = rs.getInt("id");
                        responseObj.addProperty("genreId", genre_id);
                    }

                }
                responseObj.addProperty("title", title);

                responseObj.addProperty("status", "success");
                responseObj.addProperty("message", "success");
                response.setStatus(200);

            } catch (SQLException | NamingException sqle) {
                responseObj.addProperty("status", "fail");
                responseObj.addProperty("message", "No Connection to Database - Please Try Again Later");
                response.setStatus(500);
            } finally {
                try {
                    dbcon.close();
                } catch (Exception e) {
                    System.out.println();
                }
            }
        } else if (type.equals("xml")) {
            try {
                List<String> files = new ArrayList<>();

                 /*
                 Map<String, Movie> movies = preloadMovies();
                 Map<String, Star> stars = preloadStars();
                 Map<String, Cast> casts = preloadCasts();
                */


                 Map<String, Movie> movies = null;
                 Map<String, Star> stars = null;
                 Map<String, Cast> casts = null;

                 String cbase = new File(System.getProperty("catalina.home")).getAbsolutePath();
                 files.add(cbase + "/../stanford-movies/mains243.xml");
                 files.add(cbase + "/../stanford-movies/actors63.xml");
                 files.add(cbase + "/../stanford-movies/casts124.xml");

                 /*
                 for (String filename : files) {
                     XMLParser curPrsr = new XMLParser(filename, movies, stars, casts);
                     movies = curPrsr.movies;
                     stars = curPrsr.stars;
                     casts = curPrsr.casts;
                     curPrsr.printMsgs();
                 }

                  */

                for (String filename : files) {
                    XMLParser curPrsr = new XMLParser(filename);
                    if (curPrsr.movies != null && curPrsr.movies.size() > 0) movies = curPrsr.movies;
                    if (curPrsr.stars != null && curPrsr.stars.size() > 0) stars = curPrsr.stars;
                    if (curPrsr.casts != null && curPrsr.casts.size() > 0) casts = curPrsr.casts;
                    curPrsr.printMsgs();
                }

                List<String> errors = populateTables(movies, stars, casts);

                /*
                generateUpdates(movies, stars, casts);
                try {
                    Connection dbcon = dataSource.getConnection();
                    ScriptRunner runner = new ScriptRunner(dbcon);
                    Reader reader = new BufferedReader(new FileReader("/generated-script.sql"));
                    //runner.runScript(reader);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                 */

                responseObj.addProperty("status", "success");
                responseObj.addProperty("message", "success");
                response.setStatus(200);
            } catch (Exception e) {
                System.out.println("Failed to Parse/Load File(s)");
                responseObj.addProperty("status", "fail");
                responseObj.addProperty("message", "could not parse xml/insert values in db");
            }
        }

        out.write(responseObj.toString());
        out.close();

    }

    public Map<String, Movie> preloadMovies() throws SQLException, NamingException {
        Map<String, Movie> movies = new HashMap<>();

        Context initContext = new InitialContext();
        Context envContext = (Context) initContext.lookup("java:/comp/env");
        DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");

        Connection dbcon = dataSource.getConnection();

        PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM movies");
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            String id = rs.getString("id");
            Movie newMov = new Movie(id, rs.getString("title"), rs.getInt("year"), rs.getString("director"), rs.getDouble("price"), new HashMap<>());

            statement = dbcon.prepareStatement("SELECT genreId, name FROM genres, genres_in_movies WHERE genres.id = genres_in_movies.genreId AND movieId = ?");
            statement.setString(1, id);

            ResultSet rs2 = statement.executeQuery();
            Map<String, Integer> genres = new HashMap<>();
            while (rs2.next()) {
                String name = rs2.getString("name");
                if (!genres.containsKey(name)) genres.put(name, rs2.getInt("genreId"));
            }

            newMov.genres = genres;

            movies.put(id, newMov);
        }

        return movies;
    }

    public Map<String, Star> preloadStars() throws SQLException, NamingException {
        Map<String, Star> stars = new HashMap<>();

        Context initContext = new InitialContext();
        Context envContext = (Context) initContext.lookup("java:/comp/env");
        DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");

        Connection dbcon = dataSource.getConnection();

        PreparedStatement statement = dbcon.prepareStatement("SELECT * FROM stars");
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            String name = rs.getString("name").toLowerCase().trim();
            stars.put(name, new Star(name, rs.getInt("birthYear"), rs.getString("id")));
        }

        return stars;
    }

    public Map<String, Cast> preloadCasts() throws SQLException, NamingException {
        Map<String, Cast> casts = new HashMap<>();

        Context initContext = new InitialContext();
        Context envContext = (Context) initContext.lookup("java:/comp/env");
        DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");

        Connection dbcon = dataSource.getConnection();

        PreparedStatement statement = dbcon.prepareStatement("SELECT name, id, movieId FROM stars, stars_in_movies WHERE stars.id = stars_in_movies.starId");
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            String id = rs.getString("movieId");
            if (!casts.containsKey(id)) casts.put(id, new Cast(id, new HashMap<>()));

            Cast curCast = casts.get(id);
            String name = rs.getString("name").toLowerCase().trim();
            String starId = rs.getString("id");
            if (!curCast.stars.containsKey(name)) curCast.stars.put(name, starId);
            casts.put(id, curCast);
        }

        return casts;
    }

    public void generateUpdates(Map<String, Movie> movies, Map<String, Star> stars, Map<String, Cast> casts) {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/master/moviedb");

            Connection dbcon = dataSource.getConnection();
            FileWriter oFile = new FileWriter("/generated-script.sql");

            String max_star_id = "";

            PreparedStatement statement = dbcon.prepareStatement("select max(id) as id from stars;");
            ResultSet rs = statement.executeQuery();

            if (rs.next()) max_star_id = rs.getString("id");

            String start = max_star_id.substring(0, 2);
            int num = Integer.parseInt(max_star_id.substring(2));

            statement = dbcon.prepareStatement("select max(id) as id from genres;");
            rs = statement.executeQuery();

            int max_genre_id = 100;
            if (rs.next()) max_genre_id = rs.getInt("id");

            //oFile.write("DELETE FROM movies;\nDELETE FROM genres;\nDELETE FROM stars;\nDELETE FROM stars_in_movies;\nDELETE FROM genres_in_movies;\n\n");

            Map<String, Integer> addedGenres = new HashMap<>();

            for (String curStr : movies.keySet()) {
                Movie curMov = movies.get(curStr);

                oFile.write("INSERT INTO movies (id, title, year, director, price) VALUES ('" + curStr + "', '" + curMov.title + "', " + curMov.year + ", '" + curMov.director + "', " + curMov.price + ");\n");

                for (String genre : curMov.genres.keySet()) {
                    int genreId = curMov.genres.get(genre);

                    if (!addedGenres.containsKey(genre)) {
                        if (genreId < 0) genreId = ++max_genre_id;
                        oFile.write("INSERT INTO genres (id, name) VALUES (" + genreId + ", '" + genre + "');\n");
                        addedGenres.put(genre, genreId);
                    }

                    oFile.write("INSERT INTO genres_in_movies(genreId, movieId) VALUES (" + genreId + ", '" + curMov.id + "');\n");
                }
            }

            for (String starName : stars.keySet()) {
                Star curStar = stars.get(starName);

                if (curStar.id == null) curStar.id = (start + (++num));

                if (curStar.year > 0) oFile.write("INSERT INTO stars(id, name, birthYear) VALUES ('" + curStar.id + "', '" + starName + "', " + curStar.year + ");\n");
                else oFile.write("INSERT INTO stars(id, name) VALUES ('" + curStar.id + "', '" + starName + "');\n");

                stars.put(starName, curStar);
            }

            for (String movieId : casts.keySet()) {
                Cast curCast = casts.get(movieId);
                for (String star : curCast.stars.keySet()) {
                    String starId = curCast.stars.get(star);
                    if (starId == null) {
                        if (!stars.containsKey(star) || stars.get(star).id == null) {
                            statement = dbcon.prepareStatement("SELECT id FROM stars WHERE stars.name = ?");
                            statement.setString(1, star);
                            rs = statement.executeQuery();
                            if (rs.next()) starId = rs.getString("id");
                            else {
                                starId = (start + (++num));
                                Star newStar = new Star(star);
                                newStar.id = starId;
                                stars.put(star, newStar);
                                oFile.write("INSERT INTO stars(id, name) VALUES ('" + starId + "', '" + star + "');\n");
                            }
                        } else starId = stars.get(star).id;
                    }
                    oFile.write("INSERT INTO stars_in_movies(starId, movieId) VALUES ('" +  starId + "', '" + movieId + "');\n");
                }
            }
            oFile.close();
        } catch (Exception e) {
            System.out.println("ERROR!");
        };
    }

    public List<String> populateTables(Map<String, Movie> movies, Map<String, Star> stars, Map<String, Cast> casts) {
        Connection dbcon = null;
        List<String> errors = new ArrayList<>();
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup("jdbc/master/moviedb");

            dbcon = dataSource.getConnection();

            String max_star_id = "";

            PreparedStatement statement = dbcon.prepareStatement("select max(id) as id from stars;");
            ResultSet rs = statement.executeQuery();

            if (rs.next()) max_star_id = rs.getString("id");

            String start = max_star_id.substring(0, 2);
            int num = Integer.parseInt(max_star_id.substring(2));

            statement = dbcon.prepareStatement("select max(id) as id from genres;");
            rs = statement.executeQuery();

            int max_genre_id = 100;
            if (rs.next()) max_genre_id = rs.getInt("id");

            String max_mov_id = "";

            statement = dbcon.prepareStatement("select max(id) as id from movies;");
            rs = statement.executeQuery();

            if (rs.next()) max_mov_id = rs.getString("id");

            String mov_start = max_mov_id.substring(0,2);
            int mov_num = Integer.parseInt(max_mov_id.substring(2));


            Map<String, Integer> addedGenres = new HashMap<>();

            for (String curStr : movies.keySet()) {
                Movie curMov = movies.get(curStr);
                if (curStr == null) curStr = (mov_start + (++mov_num));

                try {
                    statement = dbcon.prepareStatement("INSERT INTO movies (id, title, year, director, price) VALUES (?,?,?,?,?);");
                    statement.setString(1, curStr);
                    statement.setString(2, curMov.title);
                    statement.setInt(3, curMov.year);
                    statement.setString(4, curMov.director);
                    statement.setDouble(5, curMov.price);
                    System.out.println(statement.toString());
                    statement.executeUpdate();
                } catch (Exception e) {
                    errors.add(e.getMessage());
                }

                try {
                    statement = dbcon.prepareStatement("insert into ratings (movieId, rating, numVotes) values (?, 9.0, 1);");
                    statement.setString(1, curStr);
                    System.out.println(statement.toString());
                    statement.executeUpdate();
                } catch (Exception e) {
                    errors.add(e.getMessage());
                }

                for (String genre : curMov.genres.keySet()) {
                    genre = genre.trim();
                    int genreId = curMov.genres.get(genre);

                    if (!addedGenres.containsKey(genre)) {
                        if (genreId < 0) {
                            statement = dbcon.prepareStatement("SELECT * FROM genres WHERE LOWER(genres.name) LIKE LOWER(?);");
                            statement.setString(1, "%" + genre + "%");
                            ResultSet rsIn = statement.executeQuery();
                            if (rs.next()) genreId = rs.getInt("id");
                        }

                        if (genreId < 0) genreId = ++max_genre_id;
                        try {
                            statement = dbcon.prepareStatement("INSERT INTO genres (id, name) VALUES (?,?);");
                            statement.setInt(1, genreId);
                            statement.setString(2, genre);
                            System.out.println(statement.toString());
                            statement.executeUpdate();
                            addedGenres.put(genre, genreId);
                        } catch (Exception e) {
                            errors.add(e.getMessage());
                        }
                    } else genreId = addedGenres.get(genre);

                    try {
                        statement = dbcon.prepareStatement("INSERT INTO genres_in_movies(genreId, movieId) VALUES (?,?);");
                        statement.setInt(1, genreId);
                        statement.setString(2, curMov.id);
                        System.out.println(statement.toString());
                        statement.executeUpdate();
                    } catch (Exception e) {
                        errors.add(e.getMessage());
                    }
                }
            }

            for (String starName : stars.keySet()) {
                Star curStar = stars.get(starName);

                if (curStar.id == null) curStar.id = (start + (++num));

                try {
                    if (curStar.year > 0) {
                        statement = dbcon.prepareStatement("INSERT INTO stars(id, name, birthYear) VALUES (?,?,?);");
                        statement.setString(1, curStar.id);
                        statement.setString(2, starName);
                        statement.setInt(3, curStar.year);
                    } else {
                        statement = dbcon.prepareStatement("INSERT INTO stars(id, name) VALUES (?,?);");
                        statement.setString(1, curStar.id);
                        statement.setString(2, starName);
                    }
                    System.out.println(statement.toString());
                    statement.executeUpdate();
                } catch (Exception e) {
                    errors.add(e.getMessage());
                }

                stars.put(starName, curStar);
            }

            for (String movieId : casts.keySet()) {
                Cast curCast = casts.get(movieId);
                for (String star : curCast.stars.keySet()) {
                    String starId = curCast.stars.get(star);
                    if (starId == null) {
                        if (!stars.containsKey(star) || stars.get(star).id == null) {
                            try {
                                statement = dbcon.prepareStatement("SELECT id FROM stars WHERE stars.name = ?");
                                statement.setString(1, star);
                                System.out.println(statement.toString());
                                rs = statement.executeQuery();
                                if (rs.next()) starId = rs.getString("id");
                                else {
                                    starId = (start + (++num));
                                    Star newStar = new Star(star);
                                    newStar.id = starId;
                                    stars.put(star, newStar);
                                    try {
                                        statement = dbcon.prepareStatement("INSERT INTO stars(id, name) VALUES (?,?);");
                                        statement.setString(1, starId);
                                        statement.setString(2, star);
                                        System.out.println(statement.toString());
                                        statement.executeUpdate();
                                    } catch (Exception e) {
                                        errors.add(e.getMessage());
                                    }
                                }
                            } catch (Exception e) {
                                errors.add(e.getMessage());
                            }
                        } else starId = stars.get(star).id;
                    }

                    try {
                        statement = dbcon.prepareStatement("INSERT INTO stars_in_movies(starId, movieId) VALUES (?,?);");
                        statement.setString(1, starId);
                        statement.setString(2, movieId);
                        System.out.println(statement.toString());
                        statement.executeUpdate();
                    } catch (Exception e) {
                        errors.add(e.getMessage());
                    }
                }
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("ERROR!");
        } finally {
            try {
                dbcon.close();
            } catch (Exception e) {
                System.out.println();
            }
        }

        return errors;
    }
}
