package com.proyek.rahmanjai.eatit;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.proyek.rahmanjai.eatit.Common.Common;
import com.proyek.rahmanjai.eatit.Database.Database;
import com.proyek.rahmanjai.eatit.Model.User;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnSignUp;
    TextView txtSlogan , guest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/Roboto-Regular.TTF"); // font from assets: "assets/fonts/Roboto-Regular.ttf
        setContentView(R.layout.activity_main);

        btnSignIn =  findViewById(R.id.btnSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        guest = findViewById(R.id.guest);

        txtSlogan = findViewById(R.id.txtSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/NABILA.TTF");
        txtSlogan.setTypeface(face);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = new Intent (MainActivity.this, SignIn.class);
                startActivity(signIn);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = new Intent (MainActivity.this, SignUp.class);
                startActivity(signIn);
            }
        });

        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User gUser = new User("Guest","Guest");
                gUser.setPhone("0000000000");
                Common.currentUser = gUser;
                Intent signIn = new Intent (MainActivity.this, Home.class);
                startActivity(signIn);
            }
        });



    }
}
