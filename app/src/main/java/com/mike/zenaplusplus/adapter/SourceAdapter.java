package com.mike.zenaplusplus.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.models.UserModel;
import com.mike.zenaplusplus.utils.Account;
import com.mike.zenaplusplus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.DummyAdapterHolder> {
    private final static String TAG = "SourceAdapter";

    private Activity activity;
    private Map<String, String> sourcesMap = new HashMap<>();
    private List<String> sourcesKeys = new ArrayList<>();

    public SourceAdapter(Activity activity) {
        this.activity = activity;
        Account.getInstance().user.observe((LifecycleOwner) activity, userModel -> notifyDataSetChanged());
    }

    public void setSourcesMap(Map<String, String> sourcesMap) {
        this.sourcesMap.clear();
        this.sourcesMap.putAll(sourcesMap);
        this.sourcesKeys.clear();
        this.sourcesKeys.addAll(sourcesMap.keySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DummyAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source, parent, false);
        return new DummyAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DummyAdapterHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return sourcesMap.size();
    }

    class DummyAdapterHolder extends RecyclerView.ViewHolder {
        TextView sourceTextView;
        ImageView sourceImageView;
        Button followBtn;

        DummyAdapterHolder(@NonNull View itemView) {
            super(itemView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            sourceImageView = itemView.findViewById(R.id.sourceImageView);
            followBtn = itemView.findViewById(R.id.followBtn);
        }

        void bind(int position) {
            UserModel userModel = Account.getInstance().user.getValue();
            sourceTextView.setText(sourcesKeys.get(position));
            Utils.getInstance().setImageSource(activity, sourcesMap.get(sourcesKeys.get(position)), sourceImageView);
            followBtn.setOnClickListener(v-> Account.getInstance().followSource(sourcesKeys.get(position)));
            try {
                if(userModel.getFollowingSources().contains(sourcesKeys.get(position))){
                    followBtn.setOnClickListener(v-> Account.getInstance().unFollowSource(sourcesKeys.get(position)));
                    followBtn.setText("Unfollow");
                    followBtn.setTextColor(ContextCompat.getColor(activity, R.color.tertiary));
                } else {
                    followBtn.setOnClickListener(v-> Account.getInstance().followSource(sourcesKeys.get(position)));
                    followBtn.setText("Follow");
                    followBtn.setTextColor(ContextCompat.getColor(activity, R.color.accentPrimary));
                }
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
