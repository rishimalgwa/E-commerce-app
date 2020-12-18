package Seller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ecommercedemo.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class SellerAddNewProductActivity extends AppCompatActivity {
    private String CategoryName,description, price,pName, saveCurrentDate,saveCurrentTime;
    private Button addNewProductButton;
    private ImageView inputProductImage;
    private EditText inputProductName,inputProductDescription,inputProductPrice;
    private static final int GalleryPick=1;
    private Uri imageURI;
    private String productRandomKey, downloadImageURL;
    private StorageReference productImagesRef;
    private DatabaseReference productRef,sellerRef;
    private ProgressDialog loadingBar;
    private  String sName, sAddress, sPhone,sEmail, sID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_add_new_product);
       CategoryName = getIntent().getExtras().get("Category").toString();
        loadingBar = new ProgressDialog(this);
       productImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
       productRef = FirebaseDatabase.getInstance().getReference().child("Product");
       sellerRef = FirebaseDatabase.getInstance().getReference().child("Seller");
        addNewProductButton = findViewById(R.id.add_new_product);
        inputProductDescription = findViewById(R.id.product_description);
        inputProductName = findViewById(R.id.product_name);
        inputProductPrice = findViewById(R.id.product_price);
        inputProductImage = findViewById(R.id.select_product_image);
        inputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        addNewProductButton.setOnClickListener(new View.OnClickListener() {
             @Override
            public void onClick(View v) {
                validateProductData();
         }
        });

        sellerRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            sName = dataSnapshot.child("name").getValue().toString();
                            sPhone = dataSnapshot.child("phone").getValue().toString();
                            sAddress = dataSnapshot.child("address").getValue().toString();
                            sID = dataSnapshot.child("uid").getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void OpenGallery() {
        Intent galleryIntent= new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            imageURI =data.getData();
            inputProductImage.setImageURI(imageURI);
        }
    }
    private void validateProductData(){
        description= inputProductDescription.getText().toString();
        price= inputProductPrice.getText().toString();
        pName= inputProductName.getText().toString();
        if (imageURI==null){
            Toast.makeText(this, "Image is mandatory", Toast.LENGTH_SHORT).show();
        }else  if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "write description", Toast.LENGTH_SHORT).show();
        }else  if (TextUtils.isEmpty(price)){
            Toast.makeText(this, "write price", Toast.LENGTH_SHORT).show();
        }else  if (TextUtils.isEmpty(pName)){
            Toast.makeText(this, "write name", Toast.LENGTH_SHORT).show();
        }else {
            StoreProductInfo();
        }
    }

    private void StoreProductInfo() {
        loadingBar.setTitle("Adding new Product");
        loadingBar.setMessage("Please wait");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate= new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime= new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate+ saveCurrentTime;

        final StorageReference filePath= productImagesRef.child(imageURI.getLastPathSegment() + productRandomKey+".jpg");
        final UploadTask uploadTask = filePath.putFile(imageURI);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(SellerAddNewProductActivity.this, "ERROR: "+message, Toast.LENGTH_SHORT).show();
            loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SellerAddNewProductActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();

                        }
                        downloadImageURL= filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            downloadImageURL= task.getResult().toString();
                            Toast.makeText(SellerAddNewProductActivity.this, "Got the product image URL successfully", Toast.LENGTH_SHORT).show();
                        saveProductInfoToDatabase();
                        }
                    }
                });
            }
        });

    }

    private void saveProductInfoToDatabase() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", description);
        productMap.put("image", downloadImageURL);
        productMap.put("category", CategoryName);
        productMap.put("price", price);
        productMap.put("pname", pName);


        productMap.put("sellerName", sName);
        productMap.put("sellerAddress", sAddress);
        productMap.put("sellerEmail",sEmail);
        productMap.put("sid",sID) ;
        productMap.put("sellerPhone",sPhone);
        productMap.put("productState", "Not Approved");
      productRef.child(productRandomKey).updateChildren(productMap)
              .addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      if (task.isSuccessful()){
                          Intent intent = new Intent(SellerAddNewProductActivity.this, SellerHomeActivity.class);
                          startActivity(intent);

                          loadingBar.dismiss();
                          Toast.makeText(SellerAddNewProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
                      }else {
                          loadingBar.dismiss();
                          String message= task.getException().toString();
                          Toast.makeText(SellerAddNewProductActivity.this, "ERROR: "+message, Toast.LENGTH_SHORT).show();
                      }
                  }
              });
    }

}
