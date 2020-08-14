package com.mike.zenaplusplus.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.ui.forYou.ForYouViewModel;
import com.mike.zenaplusplus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryItemHolder> {
    private static final String TAG = "CategoriesAdapter";

    private Activity activity;
    private Map<String, Map<String, Object>> categoryMap = new HashMap<>(new HashMap<>());
    private List<String> keys = new ArrayList<>();
    private List<String> selectedKeys = new ArrayList<>();
    private ForYouViewModel forYouViewModel;

    public CategoriesAdapter(Activity activity) {
        this.activity = activity;
        forYouViewModel = ViewModelProviders.of((FragmentActivity) activity).get(ForYouViewModel.class);
    }

    public void setCategoryMap(Map<String, Map<String, Object>> categoryModelList) {
        this.categoryMap.clear();
        this.categoryMap.putAll(categoryModelList);
        keys.clear();
        keys.addAll(this.categoryMap.keySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryItemHolder holder, int position) {
        holder.bind(categoryMap.get(keys.get(position)), activity, keys.get(position), selectedKeys, this,forYouViewModel);
    }

    @Override
    public int getItemCount() {
        return categoryMap.size();
    }

    public List<String> getSelectedKeys() {
        return selectedKeys;
    }

    public void setSelectedKeys(List<String> selectedKeys) {
        this.selectedKeys = selectedKeys;
    }

    static class CategoryItemHolder extends RecyclerView.ViewHolder {
        TextView categoryTitleTextView;
        ImageView categoryImageView;
        ImageView tickImageView;

        CategoryItemHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitleTextView = itemView.findViewById(R.id.categoryTitleTextView);
            categoryImageView = itemView.findViewById(R.id.categoryImageView);
            tickImageView = itemView.findViewById(R.id.tickImageView);
        }

        void bind(Map<String, Object> category, Activity activity, String key, List<String> selectedKeys, CategoriesAdapter categoriesAdapter, ForYouViewModel forYouViewModel) {
            categoryTitleTextView.setText((String) category.get("englishName"));
            Utils.getInstance().setImageSource(activity, category.get("photo"), categoryImageView);
            if (selectedKeys.contains(key)) {
                tickImageView.setVisibility(View.VISIBLE);
                itemView.setOnClickListener(v -> {
                    selectedKeys.remove(key);
                    if(selectedKeys.isEmpty()) forYouViewModel.boneBtnEnabled.setValue(false);
                    categoriesAdapter.notifyDataSetChanged();
                });
            } else {
                tickImageView.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> {
                    selectedKeys.add(key);
                    if(!selectedKeys.isEmpty()) forYouViewModel.boneBtnEnabled.setValue(true);
                    categoriesAdapter.notifyDataSetChanged();
                });
            }
        }
    }
}
