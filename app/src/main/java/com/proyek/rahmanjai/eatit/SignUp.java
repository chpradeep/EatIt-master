package com.proyek.rahmanjai.eatit;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.proyek.rahmanjai.eatit.Common.Common;
import com.proyek.rahmanjai.eatit.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import android.content.Intent;

public class SignUp extends AppCompatActivity {

    MaterialEditText edtPhone, edtName, edtPassword;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = findViewById(R.id.edtName);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone =  findViewById(R.id.edtPhone);

        btnSignUp = findViewById(R.id.btnSignUp);

        // Init Firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("user");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("Please wait...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Cek apakah Nomot telepon telah terdaftar
                            String mobile = edtPhone.getText().toString().trim();

                            if(mobile.isEmpty() || mobile.length() < 10){
                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "Enter a valid mobile", Toast.LENGTH_SHORT).show();
                            }
                            else if (dataSnapshot.child(mobile).exists()) {
                                mDialog.dismiss();
                                Toast.makeText(SignUp.this, "Number Already Registered!!", Toast.LENGTH_SHORT).show();
                            } else {
                                mDialog.dismiss();
                                Intent intent = new Intent(SignUp.this, VerifyPhone.class);
                                intent.putExtra("mobile", mobile);
                                intent.putExtra("username" , edtName.getText().toString());
                                intent.putExtra("password" , edtPassword.getText().toString());
                                startActivity(intent);

                                /*User user = new User(edtName.getText().toString(), edtPassword.getText().toString());
                                table_user.child(edtPhone.getText().toString()).setValue(user);
                                Toast.makeText(SignUp.this, "Registeration Successful!!", Toast.LENGTH_SHORT);
                                finish();*/
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else {
                    Toast.makeText(SignUp.this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
