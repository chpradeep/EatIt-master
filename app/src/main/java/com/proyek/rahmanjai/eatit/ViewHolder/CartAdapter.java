package com.proyek.rahmanjai.eatit.ViewHolder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.proyek.rahmanjai.eatit.Cart;
import com.proyek.rahmanjai.eatit.Common.Common;
import com.proyek.rahmanjai.eatit.Interface.ItemClickListener;
import com.proyek.rahmanjai.eatit.Model.Order;
import com.proyek.rahmanjai.eatit.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;

import android.util.Log;

/**
 * Created by rahmanjai on 14/04/2018.
 */

class  CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener{

    public TextView txt_cart_name, txt_price, txt_quantity, txt_value, txt_min, txt_avl;
    public Button btn_incQty, btn_decQty;
    public ImageView cart_item_image;

    private ItemClickListener itemClickListener;

    public void setTxt_cart_name(TextView txt_cart_name) {
        this.txt_cart_name = txt_cart_name;
    }

    public CartViewHolder(View itemView) {
        super(itemView);

        txt_cart_name = itemView.findViewById(R.id.food_name);
        txt_price = itemView.findViewById(R.id.food_price);
        txt_quantity = itemView.findViewById(R.id.quantity);
        txt_value = itemView.findViewById(R.id.item_value);
        txt_min = itemView.findViewById(R.id.minimum);
        txt_avl = itemView.findViewById(R.id.availability);
        btn_decQty = itemView.findViewById(R.id.decQty);
        btn_incQty = itemView.findViewById(R.id.incQty);
        cart_item_image = itemView.findViewById(R.id.cart_item_image);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Choose Action");
        menu.add(0,0,getAdapterPosition(), Common.DELETE);
    }
}

public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData = new ArrayList<>();
    private Context context;

    public CartAdapter(List<Order> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {
        /*TextDrawable drawable = TextDrawable.builder()
                .buildRound(""+listData.get(position).getQuantity(), Color.RED);

        holder.quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                //Log.d("update cart", String.format("oldValue: %d   newValue: %d position: %d", oldValue, newValue , position));
                Cart c = (Cart)context;
                if(newValue==0){
                    c.deleteCart(position);
                }
                else{
                    c.updateQuantity(position,newValue);
                }
            }
        });*/
        holder.btn_incQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cart c = (Cart)context;
                c.incrementQty(position);
            }
        });

        holder.btn_decQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cart c = (Cart)context;
                c.decrementQty(position);
            }
        });

        Order tempOrder = listData.get(position);
        Locale locale = new Locale("en","IN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        float price = (Float.parseFloat(tempOrder.getPrice())) * (Float.parseFloat(tempOrder.getQuantity()));
        Picasso.with(context).load(tempOrder.getImage())
                .into(holder.cart_item_image);
        holder.txt_price.setText(fmt.format(Float.parseFloat(tempOrder.getPrice()))+"/"+tempOrder.getUnits());
        holder.txt_cart_name.setText(tempOrder.getProductName());
        holder.txt_value.setText(fmt.format(price));
        holder.txt_quantity.setText(tempOrder.getQuantity());
        holder.txt_avl.setText("** Available in multiples of "+tempOrder.getInc()+" "+tempOrder.getUnits());
        holder.txt_min.setText("* Minium quantity required is "+tempOrder.getMin()+" "+tempOrder.getUnits());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
