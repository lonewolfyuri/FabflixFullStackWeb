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
import java.io.IOException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    //@Resource(name = "jdbc/moviedb")
    //private DataSource dataSource;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection dbcon = null;
        response.setContentType("application/json");

        HttpSession sess = request.getSession(true);

        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String cc = request.getParameter("cc");
        String exp = request.getParameter("exp");

        PrintWriter out = response.getWriter();

        JsonObject responseJsonObject = new JsonObject();

        if (cc.equals("01234567890")) {
            ArrayList<Order> orders = (ArrayList<Order>) sess.getAttribute("orders");
            if (orders == null) orders = new ArrayList<>();

            ArrayList<Product> cart = (ArrayList<Product>) sess.getAttribute("cart");

            Order newOrder = new Order(Long.toString(Math.round(Math.random() * 9999999)), cart);

            orders.add(newOrder);

            try {
                int numItems = 0;
                double total = 0.0, subtotal = 0.0, tax = 0.0;

                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                DataSource dataSource = (DataSource) envContext.lookup("jdbc/master/moviedb");
                dbcon = dataSource.getConnection();


                for (Product curProd : cart) {

                    numItems += curProd.quantity;
                    subtotal += curProd.getSubtotal();
                    tax += curProd.getTax();
                    total += curProd.getTotal();

                    String query = "INSERT INTO sales VALUES(NULL, 0, ?, DATE(NOW()))";

                    PreparedStatement statement = dbcon.prepareStatement(query);
                    statement.setString(1, curProd.id);

                    statement.executeUpdate();
                }

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                responseJsonObject.addProperty("order", newOrder.orderNum);
                responseJsonObject.addProperty("quantity", numItems);
                responseJsonObject.addProperty("subtotal", subtotal);
                responseJsonObject.addProperty("tax", tax);
                responseJsonObject.addProperty("total", total);

                sess.setAttribute("orders", orders);

                response.setStatus(200);
                System.out.println(responseJsonObject.toString());
                response.getWriter().write(responseJsonObject.toString());
            } catch (Exception e) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", " * Transaction Declined - Please Try Again! * ");
                out.write(responseJsonObject.toString());
            } finally {
                try {
                    dbcon.close();
                } catch (Exception e) {
                    System.out.println();
                }
            }
        } else {
            try {
                Context initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:/comp/env");
                DataSource dataSource = (DataSource) envContext.lookup("jdbc/master/moviedb");

                dbcon = dataSource.getConnection();

                String query = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";

                PreparedStatement statement = dbcon.prepareStatement(query);

                statement.setString(1, cc);
                statement.setString(2, fname);
                statement.setString(3, lname);
                statement.setString(4, exp);

                System.out.println(statement.toString());

                ResultSet rs = statement.executeQuery();

                boolean isValid = false;
                while (rs.next()) {
                    if (rs.getString("id").equals(cc)) {
                        if (rs.getString("expiration").equals(exp)) {
                            if (rs.getString("firstName").toLowerCase().equals(fname.toLowerCase())) {
                                if (rs.getString("lastName").toLowerCase().equals(lname.toLowerCase())) {
                                    isValid = true;
                                    responseJsonObject.addProperty("status", "success");
                                    responseJsonObject.addProperty("message", "success");
                                    break;
                                }
                            }
                        }
                    }
                }


                if (!isValid) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", " * Transaction Declined - Please Try Again! * ");
                    out.write(responseJsonObject.toString());
                } else {
                    ArrayList<Order> orders = (ArrayList<Order>) sess.getAttribute("orders");
                    if (orders == null) orders = new ArrayList<>();

                    ArrayList<Product> cart = (ArrayList<Product>) sess.getAttribute("cart");

                    Order newOrder = new Order(Long.toString(Math.round(Math.random() * 9999999)), cart);

                    orders.add(newOrder);

                    query = "SELECT customers.id\n" +
                            "FROM creditcards, customers\n" +
                            "WHERE creditcards.id = customers.ccid\n" +
                            "\tAND creditcards.id = ?";

                    statement = dbcon.prepareStatement(query);
                    statement.setString(1, cc);

                    rs = statement.executeQuery();

                    if (rs.first()) {
                        String customerId = rs.getString("id");

                        int numItems = 0;
                        double total = 0.0, subtotal = 0.0, tax = 0.0;

                        sess.setAttribute("orders", orders);

                        for (Product curProd : cart) {
                            query = "INSERT INTO sales VALUES(NULL, ?, ?, DATE(NOW()))";

                            numItems += curProd.quantity;
                            subtotal += curProd.getSubtotal();
                            tax += curProd.getTax();
                            total += curProd.getTotal();

                            statement = dbcon.prepareStatement(query);
                            statement.setString(1, customerId);
                            statement.setString(2, curProd.id);

                            statement.executeUpdate();
                        }

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");
                        responseJsonObject.addProperty("order", newOrder.orderNum);
                        responseJsonObject.addProperty("quantity", numItems);
                        responseJsonObject.addProperty("subtotal", subtotal);
                        responseJsonObject.addProperty("tax", tax);
                        responseJsonObject.addProperty("total", total);

                        response.setStatus(200);
                        out.write(responseJsonObject.toString());
                    } else {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", " * Transaction Declined - Please Try Again! * ");
                        out.write(responseJsonObject.toString());
                    }
                }
                rs.close();
                statement.close();
            } catch (Exception e) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", " * Transaction Declined - Please Try Again! * ");
                out.write(responseJsonObject.toString());
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
}
