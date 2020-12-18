package Buyers;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommercedemo.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import Prevalent.Prevalent;
import de.hdodenhof.circleimageview.CircleImageView;

    public class SettingsActivity extends AppCompatActivity {
    private CircleImageView profileImageView;
    private EditText fullNameEditText, userPhoneEditText, addressEditText;
    private TextView profileChangedTextBtn, closeTextBtn, saveTextBtn;
    private Uri imageURI;
    private String myURL ="";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicRef;
    private String checker = "";
    private Button securityQuesButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        storageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");
        profileImageView = findViewById(R.id.settings_profile_image);
       fullNameEditText = findViewById(R.id.settings_full_name);
       securityQuesButton = findViewById(R.id.security_question_btn);
       userPhoneEditText = findViewById(R.id.settings_phone_number);
       addressEditText = findViewById(R.id.settings_address);
       profileChangedTextBtn = findViewById(R.id.profile_image_change_btn);
       closeTextBtn = findViewById(R.id.close_settings_btn);
       saveTextBtn = findViewById(R.id.update_account_settings);
       userInfoDisplay(profileImageView,fullNameEditText,userPhoneEditText,addressEditText);
       closeTextBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               finish();
           }
       });

        securityQuesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check","settings");
                startActivity(intent);
            }
        });

       saveTextBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (checker.equals("clicked")){
                   userInfoSaved();
               }else {
                   updateOnlyUserInfo();
               }
           }
       });
       profileChangedTextBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               checker="clicked";
               CropImage.activity(imageURI)
                       .setAspectRatio(1,1)
                       .start(SettingsActivity.this);
           }
       });
    }

        private void updateOnlyUserInfo() {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", fullNameEditText.getText().toString());
            userMap.put("address", addressEditText.getText().toString());
            userMap.put("phoneOder", userPhoneEditText.getText().toString());
            ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
            startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
            Toast.makeText(SettingsActivity.this, "Profile info updated!", Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!= null){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                imageURI = result.getUri();
                profileImageView.setImageURI(imageURI);
            }else {
                Toast.makeText(this, "ERROR: try again", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                finish();
            }
        }

        private void userInfoSaved() {
            if (TextUtils.isEmpty(fullNameEditText.getText().toString())){
                Toast.makeText(this, "Name is Mandatory", Toast.LENGTH_SHORT).show();
            }else  if (TextUtils.isEmpty(addressEditText.getText().toString())){
                Toast.makeText(this, "Address is Mandatory", Toast.LENGTH_SHORT).show();
            }else  if (TextUtils.isEmpty(userPhoneEditText.getText().toString())){
                Toast.makeText(this, "Phone number is Mandatory", Toast.LENGTH_SHORT).show();
            }else if (checker.equals("clicked")){
                uploadImage();
            }
        }

        private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Update Profile");
        progressDialog.setMessage("Please wait, while we are updating your account information");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if (imageURI != null){
            final StorageReference fileRef = storageProfilePicRef
                    .child(Prevalent.currentOnlineUser.getPhone() +".jpg");
            uploadTask= fileRef.putFile(imageURI);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri downloadUrl = task.getResult();
                                myURL = downloadUrl.toString();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("name", fullNameEditText.getText().toString());
                                userMap.put("address", addressEditText.getText().toString());
                                userMap.put("phoneOder", userPhoneEditText.getText().toString());
                                userMap.put("image", myURL);
                                ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);
                                progressDialog.dismiss();

                                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                                Toast.makeText(SettingsActivity.this, "Profile info updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            }else {
                                progressDialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else {
            Toast.makeText(this, "Image is not selected.", Toast.LENGTH_SHORT).show();
        }

        }

        private void userInfoDisplay(final CircleImageView profileImageView, final EditText fullNameEditText, final EditText userPhoneEditText, final EditText addressEditText) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("image").exists()){
                        String image = dataSnapshot.child("image").getValue().toString();
                        String name = dataSnapshot.child("name").getValue().toString();
                        String phone= dataSnapshot.child("phone").getValue().toString();
                        String address = dataSnapshot.child("address").getValue().toString();
                        Picasso.get().load(image).into(profileImageView);
                        fullNameEditText.setText(name);
                        userPhoneEditText.setText(phone);
                        addressEditText.setText(address);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
