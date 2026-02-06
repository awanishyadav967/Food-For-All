package br.com.fomezero.joaofood.activities.merchant;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.activities.ActiveUserData;
import br.com.fomezero.joaofood.modules.img.domain.api.UploadImageProvider;
import br.com.fomezero.joaofood.modules.img.domain.model.ImgResult;

public class NewProductActivity extends AppCompatActivity {

    private static final String TAG = "NewProductActivity";
    private static final int CAMERA_REQUEST = 1888;

    // View declarations
    private Switch donationSwitch;
    private View priceInputLayout;
    private Button addFoodButton;
    private Button submitProductButton;
    private ImageView foodImage;
    private EditText priceField;
    private EditText productNameField;
    private EditText quantityField;

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_food);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Views
        donationSwitch = findViewById(R.id.donationSwitch);
        priceInputLayout = findViewById(R.id.priceInputLayout);
        addFoodButton = findViewById(R.id.addFoodButton);
        submitProductButton = findViewById(R.id.submitProductButton);
        foodImage = findViewById(R.id.foodImage);
        priceField = findViewById(R.id.priceField);
        productNameField = findViewById(R.id.productNameField);
        quantityField = findViewById(R.id.quantityField);

        // Setup logic
        donationSwitch.setChecked(true);

        donationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (donationSwitch.isChecked()) {
                    priceInputLayout.setVisibility(View.GONE);
                } else {
                    priceInputLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        addFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 23) {
                    takePhoto();
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            NewProductActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED) {
                        takePhoto();
                    } else {
                        String[] permissionStorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(NewProductActivity.this, permissionStorage, 9);
                    }
                }
            }
        });

        submitProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
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
    public void onBackPressed() {
        super.onBackPressed();
        Intent intentHome = new Intent(this, MerchantHomeActivity.class);
        startActivity(intentHome);
        finish();
    }

    private void onSubmit() {
        final String price;
        if (donationSwitch.isChecked()) {
            price = "0.0";
        } else {
            price = priceField.getText().toString();
        }

        if (auth.getCurrentUser() == null) return;

        db.collection("users")
                .whereEqualTo("email", auth.getCurrentUser().getEmail())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot result) {
                        if (!result.isEmpty()) {
                            DocumentSnapshot document = result.getDocuments().get(0);

                            Map<String, Object> productData = new HashMap<>();
                            productData.put("name", productNameField.getText().toString());
                            productData.put("amount", quantityField.getText().toString());
                            productData.put("price", price);
                            productData.put("user", document.getReference());
                            
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                productData.put("postDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                            } else {
                                // Fallback for older Android versions if strictly needed, or just string
                                productData.put("postDate", new java.util.Date().toString()); 
                            }

                            db.collection("products").add(productData)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference productReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + productReference.getId());
                                            
                                            ActiveUserData.sendNotification(productReference.getId());

                                            // Replicating Kotlin GlobalScope.launch with a Thread
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Bitmap bitmap = null;
                                                    if (foodImage.getDrawable() instanceof BitmapDrawable) {
                                                        bitmap = ((BitmapDrawable) foodImage.getDrawable()).getBitmap();
                                                    }
                                                    
                                                    // Assuming UploadImageProvider is accessible from Java
                                                    if (bitmap != null) {
                                                        ImgResult imgResult = UploadImageProvider.uploadFile(bitmap, productReference.getId());

                                                        if (imgResult instanceof ImgResult.Success) {
                                                            // Note: Accessing Kotlin properties from Java might require getter methods 
                                                            // depending on how ImgResult is compiled (e.g., getReponse(), getData(), getLink())
                                                            // Below assumes direct field access or compatible getters
                                                            String url = ((ImgResult.Success) imgResult).getReponse().getData().getLink();
                                                            
                                                            Map<String, Object> map = new HashMap<>();
                                                            map.put("image", Arrays.asList(url));
                                                            productReference.update(map);
                                                        } else {
                                                            Map<String, Object> map = new HashMap<>();
                                                            map.put("image", Arrays.asList("https://ibb.co/5Tkdsc6"));
                                                            productReference.update(map);
                                                            Log.e("Ash", "error in image");
                                                        }
                                                    }

                                                    // Return to UI thread for navigation
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Intent intentRegisterConfirmation = new Intent(NewProductActivity.this, RegisterConfirmationActivity.class);
                                                            startActivity(intentRegisterConfirmation);
                                                            finish();
                                                        }
                                                    });
                                                }
                                            }).start();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                            finish();
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                foodImage.setImageBitmap(photo);
                foodImage.setVisibility(View.VISIBLE);
            }
        }
    }
}
