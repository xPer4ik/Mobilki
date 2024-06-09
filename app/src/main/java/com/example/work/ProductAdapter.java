package com.example.work;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends BaseAdapter {
    private Context ctx;
    private LayoutInflater lInflater;
    private ArrayList<Product> objects;
    public ProductAdapter(Context context, ArrayList<Product> products) {
        ctx = context;
        objects = products;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return objects.size();
    }
    @Override
    public Object getItem(int position) { return objects.get(position); }
    @Override
    public long getItemId(int position) { return position; }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) { view = lInflater.inflate(R.layout.product_item, parent, false); }
        Log.d("myLogs", "322");
        Product p = getProduct(position);
        Log.d("myLogs", "1312");
        ((TextView) view.findViewById(R.id.product_name)).setText(p.getName());
        Log.d("myLogs", "1");
        ((TextView) view.findViewById(R.id.product_price)).setText("Цена: " + String.valueOf(p.getPrice()) + "руб.");
        Log.d("myLogs", "3");
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(p.getImg(), 280, 280, true);
        ((ImageView) view.findViewById(R.id.product_image)).setImageBitmap(scaledBitmap);
        Log.d("myLogs", "4");
        Log.d("myLogs", "5");
        return view;
    }
    Product getProduct(int position) { return (Product) getItem(position); }

    public int getPrice(ArrayList<Product> arr) {
        int sum = 0;
        for (Product p: arr) {
            sum += p.getPrice();
        }
        return sum;
    }
}