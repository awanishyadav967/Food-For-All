package br.com.fomezero.joaofood.activities.merchant;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.fragments.MerchantHomeFragment;
import br.com.fomezero.joaofood.fragments.MerchantProfileFragment;
import br.com.fomezero.joaofood.activities.NewProductActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MerchantHomeActivity extends AppCompatActivity {

    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_home);
        loadFragment(new MerchantHomeFragment());

        BottomNavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        loadFragment(new MerchantHomeFragment());
                        return true;

                    case R.id.profile:
                        loadFragment(new MerchantProfileFragment());
                        return true;

                    case R.id.addFood:
                        Intent newProductIntent = new Intent(MerchantHomeActivity.this, NewProductActivity.class);
                        startActivity(newProductIntent);
                        return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
    }
}
