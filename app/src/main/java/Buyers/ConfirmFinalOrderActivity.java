package Buyers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import Prevalent.Prevalent;

public class ConfirmFinalOrderActivity extends AppCompatActivity {
    private EditText nameEditText,phoneEditText,addressEditText,cityEditText;
    private Button confirmButton;
    private String totalAmount="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);
        totalAmount = getIntent().getStringExtra("Total Price");
        confirmButton = findViewById(R.id.confirm_final_order_btn);
        nameEditText = findViewById(R.id.shipment_name);
        phoneEditText = findViewById(R.id.shipment_phoneNumber);
        addressEditText = findViewById(R.id.shipment_address);
        cityEditText = findViewById(R.id.shipment_city);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
    }

    private void check() {
        if (TextUtils.isEmpty(nameEditText.getText().toString())){
            Toast.makeText(this, "Please provide your Full Name", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(phoneEditText.getText().toString())){
            Toast.makeText(this, "Please provide your Phone Number", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(addressEditText.getText().toString())){
            Toast.makeText(this, "Please provide your Address", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(cityEditText.getText().toString())){
            Toast.makeText(this, "Please provide your City Name", Toast.LENGTH_SHORT).show();
        }else {
            confirmOrder();
        }
    }

    private void confirmOrder() {
       final String saveCurrentDate,saveCurrentTime;
        Calendar calendarForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendarForDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendarForDate.getTime());
        final DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference()
                .child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        final HashMap<String, Object> orderMap = new HashMap<>();
        orderMap.put("totalAmount",totalAmount);
        orderMap.put("name",nameEditText.getText().toString());
        orderMap.put("phone",phoneEditText.getText().toString());
        orderMap.put("date",saveCurrentDate);
        orderMap.put("time",saveCurrentTime);
        orderMap.put("address",addressEditText.getText().toString());
        orderMap.put("city",addressEditText.getText().toString());
        orderMap.put("state","not shipped");
        orderRef.updateChildren(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference()
                            .child("Cart List")
                            .child("User View")
                            .child(Prevalent.currentOnlineUser.getPhone())
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ConfirmFinalOrderActivity.this, "Order Placed Successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ConfirmFinalOrderActivity.this, HomeActivity.class);
                                       intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }
            }
        });
    }
}