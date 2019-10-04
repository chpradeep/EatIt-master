package com.proyek.rahmanjai.eatit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyek.rahmanjai.eatit.Common.Common;
import com.proyek.rahmanjai.eatit.Database.Database;
import com.proyek.rahmanjai.eatit.Model.Order;
import com.proyek.rahmanjai.eatit.Model.Request;
import com.proyek.rahmanjai.eatit.ViewHolder.CartAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    int total=0;
    EditText edtAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //init
        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        
        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Common.currentUser.getNama() == "Guest"){
                    Toast.makeText(Cart.this, "Kindly Register to place order!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (cart.size() > 0)
                        showAlertDialog();
                    else
                        Toast.makeText(Cart.this, "Your Cart is Empty!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        loadListFood();
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        edtAddress = new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAddress.setLayoutParams(lp);
        alertDialog.setView(edtAddress); // menambahkan edit text ke alert dialog
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("YA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (ContextCompat.checkSelfPermission(Cart.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Cart.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
                }


                Intent intent = new Intent(Cart.this, checksum.class);
                intent.putExtra("custid", Common.currentUser.getPhone());
                intent.putExtra("amount", ""+total);
                startActivityForResult(intent , 1);

                // Submit ke Firebase
                // We Will using System.CurrentMilli to Key
                /*requests.child(Common.currentUser.getPhone()+String.valueOf(System.currentTimeMillis()))
                        .setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank you for ordering!", Toast.LENGTH_SHORT).show();
                finish();*/
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Bundle bundle = data.getBundleExtra("TXN");
                    // Create new Request
                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getNama(),
                            edtAddress.getText().toString(),
                            txtTotalPrice.getText().toString(),
                            bundle.getString("TXNID"),
                            bundle.toString(),
                            cart
                    );
                    requests.child(bundle.getString("ORDERID")).setValue(request);
                    //Delete cart
                    new Database(getBaseContext()).cleanCart();
                    Toast.makeText(Cart.this, "Thank you for ordering!", Toast.LENGTH_SHORT).show();
                    finish();

            }
            else if(resultCode == RESULT_CANCELED){
                Bundle err = data.getBundleExtra("ERROR");
                Toast.makeText(Cart.this, "TRANACTION FAILED : "+err.getString("RESPMSG") , Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(Cart.this, "Transaction Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        // Kalkulasi total harga
        total = 0;
        for(Order order:cart)
            total+=(Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","IN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    public void updateQuantity(int position , int newQuantity){
        Order cart_item = cart.get(position);
        cart_item.setQuantity(""+newQuantity);
        new Database(this).cleanCart();
        //dan terakhir, kita akan mengupdate data baru dari List<Order> ke SQlite
        for (Order item:cart)
            new Database(this).addToCart(item);
        //refresh
        loadListFood();
    }

    public void deleteCart(int position) {
        //Kita akan menghapus item pada List<Order> berdasarkan posisi
        cart.remove(position);
        //Setelah itu, kita akan menghapus semua data yang lama dari SQLite
        new Database(this).cleanCart();
        //dan terakhir, kita akan mengupdate data baru dari List<Order> ke SQlite
        for (Order item:cart)
            new Database(this).addToCart(item);
        //refresh
        loadListFood();
    }
}
























