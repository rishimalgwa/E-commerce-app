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
import android.widget.Toast;

import com.example.ecommercedemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RigisterActivity extends AppCompatActivity {
private Button createAccountButton;
private EditText inputName, inputPhoneNumber, inputPassword;
    public ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rigister);
        createAccountButton = findViewById(R.id.register_Button);
        inputName = findViewById(R.id.register_username_input);
        inputPhoneNumber = findViewById(R.id.register_phoneNumber_input);
        inputPassword = findViewById(R.id.register_password_input);
        loadingBar = new ProgressDialog(RigisterActivity.this);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }
    void createAccount(){
        String name = inputName.getText().toString();
        String password = inputPassword.getText().toString();
        String phone = inputPhoneNumber.getText().toString();
        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            ValidatePhoneNumber(name,phone,password);
        }
    }
    private void ValidatePhoneNumber(final String name, final String phone, final String password){
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child("Users").child(phone).exists())){
                    HashMap<String,Object> userDataMap = new HashMap<>();
                    userDataMap.put("phone",phone);
                    userDataMap.put("password",password);
                    userDataMap.put("name",name);
                    RootRef.child("Users").child(phone).updateChildren(userDataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RigisterActivity.this, "Congratulations, your account has been created", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                        Intent intent = new Intent(RigisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }else {
                                        loadingBar.dismiss();
                                        Toast.makeText(RigisterActivity.this, "Network Error: Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else {
                    Toast.makeText(RigisterActivity.this, "This "+phone+" already exist", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                    Toast.makeText(RigisterActivity.this, "Please Try again with another phone number", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RigisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
