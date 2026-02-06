package br.com.fomezero.joaofood.activities.merchant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import java.util.List;

import br.com.fomezero.joaofood.R;
import br.com.fomezero.joaofood.model.OngData;
import br.com.fomezero.joaofood.fragments.OngInfoFragment;

import static br.com.fomezero.joaofood.util.ImageLoader.loadImage; // adjust if needed

public class OngsRecycleViewAdapter extends RecyclerView.Adapter<OngsRecycleViewAdapter.MyViewHolder> {

    private List<OngData> ongList;
    private Context context;
    private LoadFragmentListener loadFragmentListener;

    // Interface to replace Kotlin lambda
    public interface LoadFragmentListener {
        void load(Fragment fragment);
    }

    public OngsRecycleViewAdapter(List<OngData> ongList,
                                  Context context,
                                  LoadFragmentListener loadFragmentListener) {
        this.ongList = ongList;
        this.context = context;
        this.loadFragmentListener = loadFragmentListener;
    }

    @Override
    public int getItemCount() {
        return ongList.size();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        OngData ong = ongList.get(position);

        holder.name.setText(ong.getName());

        CircularProgressDrawable drawable = new CircularProgressDrawable(context);
        drawable.start();

        // Image loading (converted from Kotlin extension function)
        loadImage(holder.image, ong.getImageUrl(), drawable);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragmentListener.load(new OngInfoFragment(ong));
            }
        });
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_list, parent, false);
        return new MyViewHolder(itemView);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;
        CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            image = view.findViewById(R.id.image);
            cardView = view.findViewById(R.id.cardView);
        }
    }
}
