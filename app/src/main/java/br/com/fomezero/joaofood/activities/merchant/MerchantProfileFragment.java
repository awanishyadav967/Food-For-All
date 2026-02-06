package br.com.fomezero.joaofood.activities.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.activities.ActiveUserData;
import br.com.fomezero.joaofood.activities.LoginActivity;

import static br.com.fomezero.joaofood.util.ImageLoader.loadImage; // adjust if needed

public class MerchantProfileFragment extends Fragment {

    private TextView profileName;
    private TextView acc_name;
    private TextView acc_phone;
    private TextView acc_email;
    private ImageView profilePicture;
    private Button accountExitButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_merchant_perfil, container, false);

        // Initialize views
        profileName = root.findViewById(R.id.profileName);
        acc_name = root.findViewById(R.id.acc_name);
        acc_phone = root.findViewById(R.id.acc_phone);
        acc_email = root.findViewById(R.id.acc_email);
        profilePicture = root.findViewById(R.id.profilePicture);
        accountExitButton = root.findViewById(R.id.accountExitButton);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        accountExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        Bundle merchantData = ActiveUserData.getData();

        if (merchantData != null) {
            String name = merchantData.getString("name");
            if (name != null) {
                String profileFirstName = name.split(" ")[0];
                profileName.setText(profileFirstName);
            }

            acc_name.setText(merchantData.getString("name"));
            acc_phone.setText(merchantData.getString("phoneNumber"));
            acc_email.setText(merchantData.getString("email"));

            CircularProgressDrawable drawable =
                    new CircularProgressDrawable(requireActivity());
            drawable.start();

            loadImage(profilePicture,
                    merchantData.getString("imageProf"),
                    drawable);
        }
    }

    private void logout() {
        ActiveUserData.signOut();

        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
