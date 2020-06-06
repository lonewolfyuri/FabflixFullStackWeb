package main.java;

import java.util.ArrayList;

public class Order {
    ArrayList<Product> products;
    String orderNum;

    public Order(String ordNum, ArrayList<Product> prods) {
        orderNum = ordNum;
        products = prods;
    }
}
