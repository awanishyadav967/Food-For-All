package br.com.fomezero.joaofood.activities.merchant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.activities.ActiveUserData;
import br.com.fomezero.joaofood.model.MerchantData;
import br.com.fomezero.joaofood.model.OngData;
import br.com.fomezero.joaofood.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ktx.Auth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ktx.Firestore;
import com.google.firebase.ktx.Firebase;
import java.util.ArrayList;
import java.util.List;

public class MerchantHomeFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private RecyclerView productRecyclerView;
    private android.widget.Button addProductButton;

    private ArrayList<OngData> ongList = new ArrayList<>();
    private ArrayList<Product> productList = new ArrayList<>();
    private OngsRecycleViewAdapter ongAdapter;
    private ProductAdapter productAdapter;

    @Override
    public void onStart() {
        super.onStart();
        
        db = Firebase.getFirestore();
        auth = Firebase.getAuth();
        
        recyclerView = getView().findViewById(R.id.recyclerView);
        productRecyclerView = getView().findViewById(R.id.productRecyclerView);
        addProductButton = getView().findViewById(R.id.addProductButton);

        ongAdapter = new OngsRecycleViewAdapter(ongList, getActivity(), fragment -> {
            loadFragment(fragment);
            return null;
        });

        productAdapter = new ProductAdapter(productList, getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(ongAdapter);

        productRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        productRecyclerView.setItemAnimator(new DefaultItemAnimator());
        productRecyclerView.setAdapter(productAdapter);

        addProductButton.setOnClickListener(v -> {
            Intent newProductIntent = new Intent(getActivity(), NewProductActivity.class);
            startActivity(newProductIntent);
        });

        getProductList();

        ActiveUserData.getOngList(new ActiveUserData.OngListCallback() {
            @Override
            public void onSuccess(ArrayList<OngData> ongListData) {
                MerchantHomeFragment.this.ongList.clear();
                MerchantHomeFragment.this.ongList.addAll(ongListData);
                ongAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable throwable) {
            }
        });
    }

    private void getProductList() {
        db.collection("users")
                .whereEqualTo("email", auth.getCurrentUser().getEmail())
                .get()
                .addOnSuccessListener(result -> {
                    if (!result.isEmpty()) {
                        result.getDocuments().get(0).getDocumentReference("data").get().addOnSuccessListener(data -> {
                            db.collection("products")
                                    .whereEqualTo("user", result.getDocuments().get(0).getReference())
                                    .get()
                                    .addOnSuccessListener(products -> {
                                        if (!products.isEmpty()) {
                                            List<String> urlList = (List<String>) data.get("image");
                                            String imagePath = (urlList != null && !urlList.isEmpty()) ? urlList.get(0) : null;

                                            MerchantData merchantData = new MerchantData(
                                                    data.getString("name") != null ? data.getString("name") : "",
                                                    data.getString("phoneNumber") != null ? data.getString("phoneNumber") : "",
                                                    data.getString("email") != null ? data.getString("email") : "",
                                                    data.getString("address") != null ? data.getString("address") : "",
                                                    imagePath
                                            );

                                            productList.clear();
                                            for (int i = 0; i < products.getDocuments().size(); i++) {
                                                var product = products.getDocuments().get(i);
                                                List<String> productUrlList = (List<String>) product.get("image");
                                                String productImagePath = (productUrlList != null && !productUrlList.isEmpty()) ? productUrlList.get(0) : null;

                                                Log.d("Ashwith", productUrlList != null ? productUrlList.toString() : "null");

                                                productList.add(
                                                        new Product(
                                                                product.getString("name") != null ? product.getString("name") : "",
                                                                product.getString("amount") != null ? product.getString("amount") : "",
                                                                product.getString("price") != null ? product.getString("price") : "0",
                                                                productImagePath,
                                                                merchantData,
                                                                product.getString("postDate") != null ? product.getString("postDate") : "none"
                                                        )
                                                );
                                                productAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        });
                    }
                });
    }

    private void loadFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchant_home, container, false);
    }
}
