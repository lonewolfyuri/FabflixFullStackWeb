package main.java;

public class Product {
    public String title, id;
    public int quantity;
    public double price, tax;

    public Product(String tit, String iden, int quan, double pri) {
        title = tit;
        id = iden;
        quantity = quan;
        price = pri;
        tax = 0.08;
    }

    public double getTax() {
        return price * quantity * tax;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    public double getTotal() {
        return price * quantity * (1.0 + tax);
    }
}
