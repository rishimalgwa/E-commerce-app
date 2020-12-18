package Buyers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.ecommercedemo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import Model.Product;
import Prevalent.Prevalent;

public class ProductDetailsActivity extends AppCompatActivity {
    private Button addToCartButton;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView productPrice, productDescription, productName;
    private String productId="", state="normal";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        productId = getIntent().getStringExtra("pid");
        addToCartButton = findViewById(R.id.pd_add_to_cart_button);
        productImage = findViewById(R.id.product_image_details);
        productName = findViewById(R.id.product_name_details);
        productPrice = findViewById(R.id.product_price_details);
        productDescription = findViewById(R.id.product_description_details);
        numberButton = findViewById(R.id.number_btn);
        getProductDetails(productId);
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (state.equals("Order placed")|| state.equals("Order Shipped")){
                    Toast.makeText(ProductDetailsActivity.this, "You can add more products to your cart, once the current order is shipped", Toast.LENGTH_LONG).show();
                }else {
                    addingToCart();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkOrderState();
    }

    private void addingToCart() {
        String saveCurrentTime,saveCurrentDate;
        Calendar calendarForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendarForDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendarForDate.getTime());

      final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        final HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("pid",productId);
        cartMap.put("pname",productName.getText().toString());
        cartMap.put("price",productPrice.getText().toString());
        cartMap.put("date",saveCurrentDate);
        cartMap.put("time",saveCurrentTime);
        cartMap.put("quantity",numberButton.getNumber());
        cartMap.put("discount","");

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone())
                .child("Product").child(productId)
                .updateChildren(cartMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone())
                                    .child("Product").child(productId)
                                    .updateChildren(cartMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ProductDetailsActivity.this, "Added To Cart", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    }
                });
    }

    private void getProductDetails(String productId) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Product");
        productsRef.child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Product product = dataSnapshot.getValue(Product.class);
                    productName.setText(product.getPname());
                    productPrice.setText(product.getPrice());
                    productDescription.setText(product.getDescription());
                    Picasso.get().load(product.getImage()).into(productImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void checkOrderState(){
        DatabaseReference orderReference;
        orderReference = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String shippedState = dataSnapshot.child("state").getValue().toString();
                    if (shippedState.equals("shipped")){
                       state="Order Shipped";
                    }else if(shippedState.equals("not shipped")){
                        state="Order placed";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}