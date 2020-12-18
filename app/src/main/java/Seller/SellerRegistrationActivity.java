package Seller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ecommercedemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import Buyers.MainActivity;
import Buyers.RigisterActivity;

public class SellerRegistrationActivity extends AppCompatActivity {
    private Button alreadyHaveAccountBtn, registerButton;
    private EditText nameInput, phoneInput, emailInput, passwordInput, addressInput;
    private FirebaseAuth mAuth;
    public ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_registration);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
        alreadyHaveAccountBtn = findViewById(R.id.seller_alreadyHaveAccount_button);
        registerButton = findViewById(R.id.seller_register_button);
        nameInput = findViewById(R.id.seller_name);
        phoneInput = findViewById(R.id.seller_phone);
        emailInput = findViewById(R.id.seller_email);
        passwordInput = findViewById(R.id.seller_password);
        addressInput = findViewById(R.id.seller_address);
        alreadyHaveAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellerRegistrationActivity.this, SellerLoginActivity.class);
                startActivity(intent);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerSeller();
            }
        });
    }

    private void registerSeller() {
        final String name = nameInput.getText().toString();
        final String phone = phoneInput.getText().toString();
        final String address = addressInput.getText().toString();
        String password = passwordInput.getText().toString();
        final String email = emailInput.getText().toString();
        if (!name.equals("") && !phone.equals("") && !address.equals("") && !password.equals("") && !email.equals("")){
            loadingBar.setTitle("Creating Sellers Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                final DatabaseReference rootRef;
                                rootRef = FirebaseDatabase.getInstance().getReference();
                                String sid = mAuth.getCurrentUser().getUid();
                                HashMap<String ,Object> sellerMap = new HashMap<>();
                                sellerMap.put("uid",sid);
                                sellerMap.put("phone",phone);
                                sellerMap.put("email",email);
                                sellerMap.put("address",address);
                                sellerMap.put("name",name);

                                rootRef.child("Seller").child(sid).updateChildren(sellerMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                loadingBar.dismiss();
                                                Toast.makeText(SellerRegistrationActivity.this, "You are registered successfully!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SellerRegistrationActivity.this, SellerHomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            }
                        }
                    });
        }else {
            Toast.makeText(this, "Please fill to fields", Toast.LENGTH_SHORT).show();
        }
    }
}