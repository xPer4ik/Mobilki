package com.example.work;

import android.graphics.Bitmap;

public class Product {
    private String name;
    private String desc;
    private int price;
    private Bitmap img;

    public Product(String name, int price, Bitmap img, String desc) {
        this.name = name;
        this.price = price;
        this.img = img;
        this.desc = desc;
    }
    public String getName() {
        return name;
    }
    public int getPrice() {
        return price;
    }
    public Bitmap getImg() {
        return img;
    }
    public String getDesc() {return desc;}
}
