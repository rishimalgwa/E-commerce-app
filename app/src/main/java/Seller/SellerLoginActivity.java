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

public class SellerLoginActivity extends AppCompatActivity {
    private Button LoginSellerButton;
    private EditText emailInput, passwordInput;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_login);
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.seller_login_email);
        passwordInput = findViewById(R.id.seller_login_password);
        LoginSellerButton = findViewById(R.id.seller_login_button);
        LoginSellerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginSeller();
            }
        });
    }

    private void loginSeller() {
        String password = passwordInput.getText().toString();
        final String email = emailInput.getText().toString();
        if (!password.equals("") && !email.equals(""))
        {
            loadingBar.setTitle("Login Sellers Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                       if (task.isSuccessful()){
                           Intent intent = new Intent(SellerLoginActivity.this, SellerHomeActivity.class);
                           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                           startActivity(intent);
                           finish();
                       }
                        }
                    });
        }else {
            Toast.makeText(this, "Please fill to fields", Toast.LENGTH_SHORT).show();
        }
    }

}