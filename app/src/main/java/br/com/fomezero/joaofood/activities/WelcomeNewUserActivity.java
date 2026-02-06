package br.com.fomezero.joaofood.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.activities.merchant.SignUpMerchantActivity;
import br.com.fomezero.joaofood.activities.ong.SignUpOngActivity;

public class WelcomeNewUserActivity extends AppCompatActivity {

    private Button merchantButton;
    private Button ongButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_new_user);

        merchantButton = findViewById(R.id.merchantButton);
        ongButton = findViewById(R.id.ongButton);

        merchantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMerchant();
            }
        });

        ongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickOng();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void onClickMerchant() {
        Intent signUpIntent = new Intent(this, SignUpMerchantActivity.class);
        startActivity(signUpIntent);
        finish();
    }

    private void onClickOng() {
        Intent signUpIntent = new Intent(this, SignUpOngActivity.class);
        startActivity(signUpIntent);
        finish();
    }
}
