package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sess = request.getSession(true);

        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        ArrayList<Product> cart = (ArrayList<Product>) sess.getAttribute("cart");

        JsonObject result = new JsonObject();

        if (cart == null || cart.size() == 0) {
            result.addProperty("empty", true);
        } else {
            result.addProperty("empty", false);
            JsonArray products = new JsonArray();
            int numItems = 0;
            double total = 0.0, subtotal = 0.0, tax = 0.0;
            for (Product curProd : cart) {
                JsonObject newProd = new JsonObject();
                numItems += curProd.quantity;
                newProd.addProperty("title", curProd.title);
                newProd.addProperty("id", curProd.id);
                newProd.addProperty("quantity", curProd.quantity);
                newProd.addProperty("price", curProd.price);
                newProd.addProperty("subtotal", curProd.getSubtotal());
                subtotal += curProd.getSubtotal();
                tax += curProd.getTax();
                total += curProd.getTotal();
                products.add(newProd);
            }
            result.add("movies", products);
            result.addProperty("quantity", numItems);
            result.addProperty("total", total);
            result.addProperty("subtotal", subtotal);
            result.addProperty("tax", tax);
        }

        if (request.getParameter("complete") != null) sess.removeAttribute("cart");

        out.write(result.toString());
        response.setStatus(200);

        out.close();
    }
}
