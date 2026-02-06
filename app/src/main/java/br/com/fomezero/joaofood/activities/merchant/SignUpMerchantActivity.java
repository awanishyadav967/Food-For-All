package br.com.fomezero.joaofood.activities.merchant;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableKt;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.activities.LoginActivity;
import br.com.fomezero.joaofood.activities.WelcomeNewUserActivity;
import br.com.fomezero.joaofood.modules.img.domain.api.UploadImageProvider;
import br.com.fomezero.joaofood.modules.img.domain.model.ImgResult;

import kotlinx.android.synthetic.main.activity_new_food.*;
import kotlinx.android.synthetic.main.activity_sign_up_merchant.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SignUpMerchantActivity extends AppCompatActivity implements View.OnClickListener {
    
    private static final String TAG = "SignUpMerchantActivity";
    private static final int CAMERA_REQUEST = 1888;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextInputEditText completeNameField;
    private TextInputEditText emailField;
    private TextInputEditText phoneNumberField;
    private TextInputEditText addressField;
    private TextInputEditText passwordField;
    private TextInputEditText passwordConfirmationField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_merchant);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views (assuming you're using findViewById or ViewBinding)
        completeNameField = findViewById(R.id.completeNameField);
        emailField = findViewById(R.id.emailField);
        phoneNumberField = findViewById(R.id.phoneNumberField);
        addressField = findViewById(R.id.addressfield);
        passwordField = findViewById(R.id.passwordField);
        passwordConfirmationField = findViewById(R.id.passwordConfirmationField);

        // Set up sign up button click listener
        View signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(this);

        // Set up add food button click listener
        View addFoodButton = findViewById(R.id.addFoodButton);
        addFoodButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < 23) {
                takePhoto();
            } else {
                if (ActivityCompat.checkSelfPermission(
                        SignUpMerchantActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    String[] permissionStorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(SignUpMerchantActivity.this, permissionStorage, 9);
                }
            }
        });
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Camera is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap photo = null;
            if (data != null && data.getExtras() != null) {
                photo = (Bitmap) data.getExtras().get("data");
            }
            
            if (photo != null) {
                View profilepic = findViewById(R.id.profilepic);
                // Assuming profilepic is an ImageView, set bitmap accordingly
                // profilepic.setImageBitmap(photo);
                profilepic.setVisibility(View.VISIBLE);
                // TODO: Send photo to imgur and send url to database
            }
        }
    }

    @Override
    public void onClick(View v) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        // Validate if all fields are filled
        if (completeNameField.getText().toString().isEmpty() && 
            emailField.getText().toString().isEmpty() &&
            phoneNumberField.getText().toString().isEmpty() && 
            addressField.getText().toString().isEmpty()) {
            
            Toast msg = Toast.makeText(
                    getApplicationContext(),
                    "Please fill in all fields.",
                    Toast.LENGTH_LONG
            );
            msg.setGravity(Gravity.CENTER, 0, 400);
            msg.show();
            return;
        }

        // Check if any field is empty
        TextInputEditText[] fields = {
            completeNameField,
            emailField,
            phoneNumberField,
            addressField,
            passwordField,
            passwordConfirmationField
        };

        for (TextInputEditText field : fields) {
            if (field.getText().toString().isEmpty()) {
                Toast msg = Toast.makeText(
                        getApplicationContext(),
                        "Please fill in all fields.",
                        Toast.LENGTH_LONG
                );
                msg.setGravity(Gravity.CENTER, 0, 400);
                msg.show();
                return;
            }
        }

        // Validate password match
        if (!validatePassword()) {
            Toast msg = Toast.makeText(
                    getApplicationContext(),
                    "Passwords are different.",
                    Toast.LENGTH_LONG
            );
            msg.setGravity(Gravity.CENTER, 0, 400);
            msg.show();
            return;
        }

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "createUserWithEmail:success");
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpMerchantActivity.this, "Successful Sign Up!.", Toast.LENGTH_SHORT).show();
                        Intent welcomeNewUserIntent = new Intent(SignUpMerchantActivity.this, LoginActivity.class);
                        startActivity(welcomeNewUserIntent);
                        finish();
                    });
                    saveDataToFirestore();
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    runOnUiThread(() -> 
                        Toast.makeText(SignUpMerchantActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    );
                }
            });
    }

    private void saveDataToFirestore() {
        View profilepicView = findViewById(R.id.profilepic);
        Bitmap bitmap = DrawableKt.toBitmap(profilepicView.getBackground());
        
        ImgResult imgResult = UploadImageProvider.uploadFile(bitmap, "img123");
        String url = "";
        
        if (imgResult instanceof ImgResult.Success) {
            ImgResult.Success successResult = (ImgResult.Success) imgResult;
            if (successResult.getReponse() != null && successResult.getReponse().getData() != null) {
                url = successResult.getReponse().getData().getLink().toString();
            }
        }

        HashMap<String, Object> ongData = new HashMap<>();
        ongData.put("name", completeNameField.getText().toString());
        ongData.put("email", emailField.getText().toString());
        ongData.put("phoneNumber", phoneNumberField.getText().toString());
        ongData.put("address", addressField.getText().toString());
        ongData.put("imageProf", url);

        db.collection("merchants")
            .add(ongData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                HashMap<String, Object> userData = new HashMap<>();
                userData.put("email", emailField.getText().toString());
                userData.put("type", "merchant");
                userData.put("data", documentReference);

                db.collection("users")
                    .add(userData)
                    .addOnSuccessListener(usersDocumentReference -> {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + usersDocumentReference.getId());

                        // Upload image in background thread
                        new Thread(() -> {
                            View profilepicView1 = findViewById(R.id.profilepic);
                            Bitmap bitmap1 = DrawableKt.toBitmap(profilepicView1.getBackground());
                            ImgResult imgResult1 = UploadImageProvider.uploadFile(bitmap1, usersDocumentReference.getId());

                            if (imgResult1 instanceof ImgResult.Success) {
                                ImgResult.Success successResult = (ImgResult.Success) imgResult1;
                                if (successResult.getReponse() != null && successResult.getReponse().getData() != null) {
                                    String imgUrl = successResult.getReponse().getData().getLink();
                                    
                                    HashMap<String, Object> map = new HashMap<>();
                                    List<String> array = new ArrayList<>();
                                    array.add(imgUrl);
                                    map.put("image", array);
                                    
                                    usersDocumentReference.update(map);
                                }
                            }
                        }).start();
                    })
                    .addOnFailureListener(e -> 
                        Log.w(TAG, "Error adding document", e)
                    );
            })
            .addOnFailureListener(e -> 
                Log.w(TAG, "Error adding document", e)
            );
    }

    private boolean validatePassword() {
        return passwordField.getText().toString()
                .equals(passwordConfirmationField.getText().toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent welcomeNewUserIntent = new Intent(this, WelcomeNewUserActivity.class);
        startActivity(welcomeNewUserIntent);
        finish();
    }
}
