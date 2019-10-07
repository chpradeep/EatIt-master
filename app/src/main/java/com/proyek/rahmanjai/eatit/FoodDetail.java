package com.proyek.rahmanjai.eatit;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyek.rahmanjai.eatit.Common.Common;
import com.proyek.rahmanjai.eatit.Database.Database;
import com.proyek.rahmanjai.eatit.Model.Food;
import com.proyek.rahmanjai.eatit.Model.Order;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

public class FoodDetail extends AppCompatActivity {

    TextView food_name, food_price, food_description, item_value, availability, minimum , order_qty;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Button btnCart , btnIncQty, btnDecQty;
    ElegantNumberButton numberButton;
    NumberFormat fmt;

    String foodId="";

    FirebaseDatabase database;
    DatabaseReference foods;
    Food currentFood;
    float currentPrice, currentQty , incQty , stdPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        Locale locale = new Locale("en","IN");
        fmt = NumberFormat.getCurrencyInstance(locale);

        //Firbase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        // Init view
        //numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnIncQty = findViewById(R.id.incQty);
        btnDecQty = findViewById(R.id.decQty);

        btnIncQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentQty ==0) {
                    currentQty = Float.parseFloat(currentFood.getMin());
                    currentPrice = (currentQty / incQty) * stdPrice;
                    btnDecQty.setEnabled(true);
                    btnCart.setEnabled(true);
                }
                else{
                    currentQty += incQty;
                    currentPrice += stdPrice;
                }
                item_value.setText(fmt.format(currentPrice));
                order_qty.setText(String.format("%.2f", currentQty)+""+currentFood.getUnits());
            }
        });
        btnDecQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentQty == Float.parseFloat(currentFood.getMin())){
                    currentQty =0;
                    currentPrice = 0;
                    btnDecQty.setEnabled(false);
                    btnCart.setEnabled(false);
                }
                else{
                    currentQty -= incQty;
                    currentPrice -= stdPrice;
                }
                item_value.setText(fmt.format(currentPrice));
                order_qty.setText(String.format("%.2f", currentQty)+""+currentFood.getUnits());
            }
        });
    /*
        numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
           @Override
           public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                //int total = Integer.parseInt(currentFood.getPrice()) * newValue;
                //item_value.setText(fmt.format(total));
                if(newValue>0)
                    btnCart.setEnabled(true);
                else
                    btnCart.setEnabled(false);
                if(newValue==1){
                    currentQty = Float.parseFloat(currentFood.getMin());
                    currentPrice = (currentQty/incQty)*stdPrice;
                    item_value.setText(fmt.format(currentPrice)+"/"+currentQty+""+currentFood.getUnits());
                }
                else if(newValue == 0){
                    currentPrice =0;
                    currentQty=0;
                    item_value.setText(fmt.format(currentPrice)+"/"+currentQty+""+currentFood.getUnits());
                }
                else {
                    int change = newValue-oldValue;
                    currentQty += (incQty*change);
                    currentPrice += (change*stdPrice);
                    item_value.setText(fmt.format(currentPrice)+"/"+currentQty+""+currentFood.getUnits());
                }
           }
        });*/

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qty = order_qty.getText().toString();
                if(qty.contains(currentFood.getUnits())) {
                    Log.i("theStringIs" , qty);
                    qty = qty.replace(currentFood.getUnits(), "");
                    Log.i("theStringIs" , qty);
                }
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        qty,
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage(),
                        currentFood.getInc(),
                        currentFood.getMin(),
                        currentFood.getUnits()
                        ));

                Toast.makeText(FoodDetail.this, "Added to Shopping Cart", Toast.LENGTH_SHORT).show();
            }
        });

        food_description = findViewById(R.id.food_description);
        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);
        food_image = findViewById(R.id.img_food);
        item_value = findViewById(R.id.item_value);
        minimum = findViewById(R.id.minimum);
        availability = findViewById(R.id.availability);
        order_qty = findViewById(R.id.quantity);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapseAppbar);

        //Get Food Id From Internet
        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()){
            if (Common.isConnectedToInternet(getBaseContext()))
                getDetailFood(foodId);
            else {
                Toast.makeText(FoodDetail.this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
            }
        }

        FloatingActionButton showCart = findViewById(R.id.showCart);
        showCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(FoodDetail.this, Cart.class);
                startActivity(cartIntent);
            }
        });

    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                //Set Image
                Picasso.with(getBaseContext()).load(currentFood.getImage())
                        .into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(fmt.format(Integer.parseInt(currentFood.getPrice()))+"/"+currentFood.getInc()+currentFood.getUnits());

                food_name.setText(currentFood.getName());

                food_description.setText(currentFood.getDescription());

                minimum.setText("* Minium quantity required is "+currentFood.getMin()+" "+currentFood.getUnits());

                availability.setText("** Available in multiples of "+currentFood.getInc()+""+currentFood.getUnits());

                incQty = Float.parseFloat(currentFood.getInc());
                stdPrice = Float.parseFloat(currentFood.getPrice());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
