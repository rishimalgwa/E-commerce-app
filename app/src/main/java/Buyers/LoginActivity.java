package Buyers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommercedemo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import Admin.AdminHomeActivity;
import Seller.SellerProductCategoryActivity;
import Model.Users;
import Prevalent.Prevalent;
import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {
private EditText inputNumber, inputPassword;
private Button loginButton;
private ProgressDialog loadingBar;
private String parentDbName="Users";
private CheckBox rememberMeCheckBox;
private TextView AdminLink, NotAdminLink, forgetPasswordLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputNumber =findViewById(R.id.login_phoneNumber_input);
        inputPassword =findViewById(R.id.login_password_input);
        loginButton =findViewById(R.id.loginButton);
        AdminLink = findViewById(R.id.admin_panel_link);
        NotAdminLink =findViewById(R.id.not_admin_panel_link);
        forgetPasswordLink =findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(LoginActivity.this);
        rememberMeCheckBox = findViewById(R.id.rememberMe_checkBox);
        Paper.init(this);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });
        forgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check","login");
                startActivity(intent);
            }
        });
    }

    private void LoginUser() {
        String phone = inputNumber.getText().toString();
        String password = inputPassword.getText().toString();
        if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("LoginAccount");
            loadingBar.setMessage("Please wait, while we are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            AllowAccessToAccount(phone,password);
        }
        AdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setText("Login Admin");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminLink.setVisibility(View.VISIBLE);
                parentDbName ="Admins";
            }
        });
        NotAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminLink.setVisibility(View.INVISIBLE);
                parentDbName ="Users";
            }
        });
    }

    private void AllowAccessToAccount(final String phone, final String password) {
        if (rememberMeCheckBox.isChecked()){
            Paper.book().write(Prevalent.UserPhoneKey,phone);
            Paper.book().write(Prevalent.UserPasswordKey,password);
        }
        final DatabaseReference RootRef;
        RootRef= FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(parentDbName).child(phone).exists()){
                    Users usersData=dataSnapshot.child(parentDbName).child(phone).getValue(Users.class);
                    if (usersData.getPhone().equals(phone)){
                        if (usersData.getPassword().equals(password)){
                            if (parentDbName.equals("Admins")){
                                Toast.makeText(LoginActivity.this, "welcome admin,Logged in successfully!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                startActivity(intent);
                            }else if (parentDbName.equals("Users")){
                                Toast.makeText(LoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currentOnlineUser =usersData;
                                startActivity(intent);
                            }
                        }else {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Account with this "+phone+" not exist", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
