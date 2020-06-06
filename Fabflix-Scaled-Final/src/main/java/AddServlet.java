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

@WebServlet(name = "AddServlet", urlPatterns = "/api/add")
public class AddServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Connection dbcon = null;
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        HttpSession sess = request.getSession(true);

        String id = request.getParameter("id");
        if (id != null) {

            ArrayList<Product> cart = null;
            if (sess.getAttribute("cart") != null) cart = (ArrayList<Product>) sess.getAttribute("cart");
            else cart = new ArrayList<>();

            boolean seen = false;
            for (int ndx = 0; ndx < cart.size(); ndx++) {
                if (cart.get(ndx).id.equals(id)) {
                    Product curProd = cart.get(ndx);
                    curProd.quantity += 1;
                    cart.set(ndx, curProd);
                    seen = true;

                    sess.setAttribute("cart", cart);

                    JsonObject result = new JsonObject();
                    result.addProperty("title", curProd.title);
                    result.addProperty("price", curProd.price);
                    result.addProperty("id", id);
                    out.write(result.toString());
                    response.setStatus(200);
                }
            }

            if (!seen) {
                try {
                    Context initContext = new InitialContext();
                    Context envContext = (Context) initContext.lookup("java:/comp/env");
                    DataSource dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
                    dbcon = dataSource.getConnection();

                    String query = "SELECT title, id, price FROM movies WHERE movies.id = ?";

                    PreparedStatement statement = dbcon.prepareStatement(query);

                    statement.setString(1, id);

                    ResultSet rs = statement.executeQuery();

                    JsonObject result = new JsonObject();
                    if (rs.first()) {
                        String title = rs.getString("title");
                        double price = rs.getDouble("price");
                        cart.add(new Product(title, id, 1, price));

                        sess.setAttribute("cart", cart);

                        result.addProperty("title", title);
                        result.addProperty("price", price);
                        result.addProperty("id", id);
                    }

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
            }
            out.close();
        }
    }
}
