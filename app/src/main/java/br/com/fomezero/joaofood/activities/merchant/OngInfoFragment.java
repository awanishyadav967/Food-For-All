package br.com.fomezero.joaofood.activities.merchant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.model.OngData;

import static br.com.fomezero.joaofood.util.ImageLoader.loadImage; // adjust if needed

public class OngInfoFragment extends Fragment {

    public static final String PLACEHOLDER_ADDRESS =
            "177A Bleecker Street, New York City, NY 10012-1406";

    private OngData ongData;

    // UI elements
    private TextView profileName;
    private ImageView profilePicture;
    private TextView description;
    private TextView address;
    private TextView phoneNumber;
    private TextView peopleHelped;
    private Button mapsButton;
    private Button callButton;

    public OngInfoFragment(OngData ongData) {
        this.ongData = ongData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ong_info, container, false);

        // Initialize views
        profileName = root.findViewById(R.id.profileName);
        profilePicture = root.findViewById(R.id.profilePicture);
        description = root.findViewById(R.id.description);
        address = root.findViewById(R.id.address);
        phoneNumber = root.findViewById(R.id.phoneNumber);
        peopleHelped = root.findViewById(R.id.peopleHelped);
        mapsButton = root.findViewById(R.id.mapsButton);
        callButton = root.findViewById(R.id.callButton);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (ongData.isApproved()) {
            profileName.setText(ongData.getName() + " ✔️");
        } else {
            profileName.setText(ongData.getName() + " ❌");
        }

        if (ongData.getDescription() != null) {
            description.setText(
                    getString(R.string.about_text, ongData.getSiteUrl())
            );
        }

        CircularProgressDrawable drawable =
                new CircularProgressDrawable(requireActivity());
        drawable.start();

        loadImage(profilePicture,
                String.valueOf(ongData.getImageUrl()),
                drawable);

        address.setText(
                getString(R.string.address_text, ongData.getSiteUrl())
        );

        phoneNumber.setText(
                getString(R.string.phone_number, ongData.getPhoneNumber())
        );

        description.setText("Email :" + ongData.getDescription());

        peopleHelped.setText(
                getString(R.string.people_helped, 75000)
        );

        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://" + ongData.getSiteUrl())
                );
                startActivity(browserIntent);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 23) {
                    phoneCall();
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED) {
                        phoneCall();
                    } else {
                        String[] permissionStorage =
                                new String[]{Manifest.permission.CALL_PHONE};
                        ActivityCompat.requestPermissions(
                                requireActivity(),
                                permissionStorage,
                                9
                        );
                    }
                }
            }
        });
    }

    private void phoneCall() {
        Intent intent = new Intent(
                Intent.ACTION_CALL,
                Uri.parse("tel:" + ongData.getPhoneNumber())
        );
        startActivity(intent);
    }
}
