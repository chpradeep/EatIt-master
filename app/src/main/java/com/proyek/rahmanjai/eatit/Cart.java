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
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    float total=0;
    EditText edtAddress;

    String GPayUserName= "Surekha Rani";
    String GPayUPIID = "surekha284@oksbi";
    String GPayNote = "";
    String GPayAmount = "";
    final int UPI_PAYMENT = 0;

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

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //G-pay code
                payUsingUpi();


                //PAYTM code
                /*
                if (ContextCompat.checkSelfPermission(Cart.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Cart.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
                }


                Intent intent = new Intent(Cart.this, checksum.class);
                intent.putExtra("custid", Common.currentUser.getPhone());
                intent.putExtra("amount", ""+total);
                startActivityForResult(intent , 1);

                // Submit ke Firebase
                // We Will using System.CurrentMilli to Key
                //requests.child(Common.currentUser.getPhone()+String.valueOf(System.currentTimeMillis()))
                        .setValue(request);
                //Delete cart
                //new Database(getBaseContext()).cleanCart();
                //Toast.makeText(Cart.this, "Thank you for ordering!", Toast.LENGTH_SHORT).show();
                //finish();
                */
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

    private void payUsingUpi(){
        Uri uri = new Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", GPayUPIID)
            .appendQueryParameter("pn", GPayUserName)
            //.appendQueryParameter("mc", "your-merchant-code")
            .appendQueryParameter("tr", Common.currentUser.getPhone()+String.valueOf(System.currentTimeMillis()))
            .appendQueryParameter("tn", Common.currentUser.getPhone()+String.valueOf(System.currentTimeMillis()))
            .appendQueryParameter("am", ""+total)
            .appendQueryParameter("cu", "INR")
            //.appendQueryParameter("url", "your-transaction-url")
            .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(Cart.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("main ", "response "+resultCode );
        /*
       E/main: response -1
       E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPI: payment successfull: 922118921612
         */
        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.e("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList, data);
                    } else {
                        Log.e("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList, data);
                    }
                } else {
                    //when user simply back without payment
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList, data);
                }
                break;
        }
    }
    private void upiPaymentDataOperation(ArrayList<String> data , Intent intentData) {
        if (isConnectionAvailable(Cart.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Bundle bundle = intentData.getBundleExtra("TXN");
                // Create new Request
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getNama(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        approvalRefNo,
                        str,
                        cart
                );
                requests.child(approvalRefNo).setValue(request);
                //Delete cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank you for ordering!", Toast.LENGTH_SHORT).show();
                Intent orderIntent = new Intent(Cart.this, OrderStatus.class);
                startActivity(orderIntent);
                finish();

                Toast.makeText(Cart.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "payment successfull: "+approvalRefNo);
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(Cart.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: "+approvalRefNo);
            }
            else {
                Toast.makeText(Cart.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: "+approvalRefNo);
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(Cart.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }




/*
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
                    Intent orderIntent = new Intent(Cart.this, OrderStatus.class);
                    startActivity(orderIntent);
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
    }*/

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        // Kalkulasi total harga
        total = 0;
        for(Order order:cart) {
            total += (Float.parseFloat(order.getPrice())) * (Float.parseFloat(order.getQuantity()));
        }
        Locale locale = new Locale("en","IN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(Float.parseFloat(String.format("%.2f", total))));
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

    public void incrementQty(int position) {
        Order cart_item = cart.get(position);
        float qty = Float.parseFloat(cart_item.getQuantity());
        qty += Float.parseFloat(cart_item.getInc());
        cart_item.setQuantity(""+qty);
        new Database(this).cleanCart();
        //dan terakhir, kita akan mengupdate data baru dari List<Order> ke SQlite
        for (Order item:cart)
            new Database(this).addToCart(item);
        //refresh
        loadListFood();
    }

    public void decrementQty(int position) {
        Order cart_item = cart.get(position);
        float qty = Float.parseFloat(cart_item.getQuantity());
        if(qty == Float.parseFloat(cart_item.getMin())){
            cart.remove(position);
        }
        else {
            qty -= Float.parseFloat(cart_item.getInc());
            cart_item.setQuantity("" + qty);
        }
        new Database(this).cleanCart();
        //dan terakhir, kita akan mengupdate data baru dari List<Order> ke SQlite
        for (Order item:cart)
            new Database(this).addToCart(item);
        //refresh
        loadListFood();
    }


}
























