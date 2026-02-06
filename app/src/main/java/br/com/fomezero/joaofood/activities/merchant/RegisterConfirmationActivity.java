package br.com.fomezero.joaofood.activities.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import br.com.fomezero.joaofood.R;

public class RegisterConfirmationActivity extends AppCompatActivity {

    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrer_confirmation);

        homeButton = findViewById(R.id.homeButton);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToHome();
    }

    private void goToHome() {
        Intent intentHome = new Intent(this, MerchantHomeActivity.class);
        startActivity(intentHome);
        finish();
    }
}
