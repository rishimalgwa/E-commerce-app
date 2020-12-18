package Buyers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import Prevalent.Prevalent;

public class ResetPasswordActivity extends AppCompatActivity {
    private String check ="";
    private TextView pageTitle, titleQuestions;
    private EditText txtPhoneNumber, question1, question2;
    private Button verifyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        check =getIntent().getStringExtra("check");
        pageTitle = findViewById(R.id.page_title);
        titleQuestions = findViewById(R.id.title_questions);
        txtPhoneNumber = findViewById(R.id.find_phone_number);
        question1 = findViewById(R.id.question_1);
        question2 = findViewById(R.id.question_2);
        verifyButton = findViewById(R.id.verify_btn);
    }

    @Override
    protected void onStart() {
        super.onStart();
        txtPhoneNumber.setVisibility(View.GONE);
        if (check.equals("settings")){
            pageTitle.setText("Set Questions");
            titleQuestions.setText("Set answers for the following security questions");
            verifyButton.setText("Set");
            displayPreviousAnswers();
            verifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setAnswers();
                }
            });
        }else if (check.equals("login")){
            txtPhoneNumber.setVisibility(View.VISIBLE);
            verifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    verifyUser();
                }
            });
        }
    }

    private void setAnswers(){
        String answer1 = question1.getText().toString().toLowerCase();
        String answer2 = question2.getText().toString().toLowerCase();
        if (question1.equals("")&& question2.equals("")){
            Toast.makeText(ResetPasswordActivity.this, "Please answer both questions", Toast.LENGTH_SHORT).show();
        }else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(Prevalent.currentOnlineUser.getPhone());
            HashMap<String,Object> userDataMap = new HashMap<>();
            userDataMap.put("answer1",answer1);
            userDataMap.put("answer2",answer2);
            reference.child("Security questions").updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Yours answers saved, successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                }
            });
        }
    }
    private void displayPreviousAnswers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(Prevalent.currentOnlineUser.getPhone());
        reference.child("Security questions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String ans1 = dataSnapshot.child("answer1").getValue().toString();
                    String ans2 = dataSnapshot.child("answer2").getValue().toString();
                    question1.setText(ans1);
                    question2.setText(ans2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void verifyUser(){
        final String phone= txtPhoneNumber.getText().toString();
        final String answer1 = question1.getText().toString().toLowerCase();
        final String answer2 = question2.getText().toString().toLowerCase();
        if (!phone.equals("")&& !answer1.equals("") && !answer2.equals("")){
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(phone);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        String mPhone = dataSnapshot.child("phone").getValue().toString();
                        if (dataSnapshot.hasChild("Security questions")){
                            String ans1 = dataSnapshot.child("Security questions").child("answer1").getValue().toString();
                            String ans2 = dataSnapshot.child("Security questions").child("answer2").getValue().toString();
                            if (!ans1.equals(answer1)){
                                Toast.makeText(ResetPasswordActivity.this, "Incorrect first Answer", Toast.LENGTH_SHORT).show();
                            }else if (!ans2.equals(answer2)){
                                Toast.makeText(ResetPasswordActivity.this, "Incorrect second Answer", Toast.LENGTH_SHORT).show();
                            }else {
                                AlertDialog.Builder builder= new AlertDialog.Builder(ResetPasswordActivity.this);
                                builder.setTitle("New Password");
                                final EditText newPassword = new EditText(ResetPasswordActivity.this);
                                newPassword.setHint("Write Password Here");
                                builder.setView(newPassword);
                                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!newPassword.getText().toString().equals("")){
                                            reference.child("password").setValue(newPassword.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(ResetPasswordActivity.this, "Password Changed Successfully!", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                            }
                        }else {
                            Toast.makeText(ResetPasswordActivity.this, "You have not set the security questions", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(ResetPasswordActivity.this, "This phone number does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else {
            Toast.makeText(this, "please complete th form", Toast.LENGTH_SHORT).show();
        }
    }
}