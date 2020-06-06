package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
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

@WebServlet(name = "UpdateServlet", urlPatterns = "/api/update")
public class UpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        HttpSession sess = request.getSession(true);

        String[] querySplit = request.getQueryString().substring(1).split("&");

        ArrayList<Product> cart = (ArrayList<Product>) sess.getAttribute("cart");

        for (String query : querySplit) {
            String[] curSplit = query.split("=");
            for (int ndx = 0; ndx < cart.size(); ndx++) {
                if (cart.get(ndx).id.equals(curSplit[0])) {
                    int newQuan = Integer.parseInt(curSplit[1]);
                    if (newQuan <= 0) cart.remove(ndx);
                    else {
                        Product curProd = cart.get(ndx);
                        curProd.quantity = newQuan;
                        cart.set(ndx, curProd);
                    }
                }
            }
        }

        sess.setAttribute("cart", cart);

        JsonObject result = new JsonObject();
        result.addProperty("removed", true);
        out.write(result.toString());
        response.setStatus(200);
        out.close();
    }
}