package com.proyek.rahmanjai.eatit;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.proyek.rahmanjai.eatit.Common.Common;
import com.proyek.rahmanjai.eatit.Interface.ItemClickListener;
import com.proyek.rahmanjai.eatit.Model.Request;
import com.proyek.rahmanjai.eatit.ViewHolder.OrderViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder>  adapter;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Init Firebase
        database  = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Jka kita memulai OrderStatus aktivity daur Home Aktivity
        // Kita tidak akan melakuakn Put Extra apapun, jadi kita hanya loadOrder dengan menggunakan No Hp dari Common.

        if (getIntent() == null)
            loadOrders(Common.currentUser.getPhone());
        else
            loadOrders(getIntent().getStringExtra("userPhone"));

        loadOrders(Common.currentUser.getPhone());
    }

    private void loadOrders(String phone) {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests.orderByChild("phone")
                    .equalTo(phone)
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, Request model, int position) {
                String temp = adapter.getRef(position).getKey();
                temp = temp.replace("_"+model.getPhone() , "");
                viewHolder.txtOrderId.setText(temp);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                long t = Long.parseLong(temp);
                Date resultdate = new Date(t);
                viewHolder.txtOrderDate.setText(""+resultdate);
                int status = Integer.parseInt(model.getStatus());
                if(status ==0)
                    viewHolder.txtOrderStatus.setTextColor(Color.RED);
                else if(status==1)
                    viewHolder.txtOrderStatus.setTextColor(Color.GREEN);
                else
                    viewHolder.txtOrderStatus.setTextColor(Color.BLACK);
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddres.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClik) {

                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }
}
