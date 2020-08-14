package com.mike.zenaplusplus.adapter;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mike.zenaplusplus.App;
import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.models.FeedElementModel;
import com.mike.zenaplusplus.models.NewsModel;
import com.mike.zenaplusplus.repository.FeedRepo;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.mike.zenaplusplus.utils.Controller;
import com.mike.zenaplusplus.utils.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsItemHolder> {

    private Application application;
    private List<NewsModel> newsModels = new ArrayList<>();

    public NewsAdapter(Application application) {
        this.application = application;
        FeedRepo.getInstance(application).getSavedFeedElements().observeForever(feedElementModels -> notifyDataSetChanged());
    }

    public void setNewsList(List<NewsModel> newsModels) {
        this.newsModels.clear();
        this.newsModels.addAll(newsModels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_large_news, parent, false);
        return new NewsItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsItemHolder holder, int position) {
        holder.bind(application, newsModels.get(position));
    }

    @Override
    public int getItemCount() {
        return newsModels.size();
    }

    static class NewsItemHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView sourceTextView;
        TextView rankingTextView;
        TextView postedTimeTextView;
        TextView numberTextView;
        ImageView thumbnailImageView;
        ImageView sourceImageView;
        ImageView menuImageView;

        NewsItemHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            rankingTextView = itemView.findViewById(R.id.rankingTextView);
            postedTimeTextView = itemView.findViewById(R.id.postedTimeTextView);
            numberTextView = itemView.findViewById(R.id.numberTextView);
            sourceImageView = itemView.findViewById(R.id.sourceImageView);
            menuImageView = itemView.findViewById(R.id.menuImageView);
        }

        void bind(Application application, NewsModel newsModel) {
            List<FeedElementModel> savedFeedElementModels = FeedRepo.getInstance(application).getSavedFeedElements().getValue();
            Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
            titleTextView.setText(newsModel.getTitle());
            Utils.getInstance().setImageSource(application, newsModel.getThumbnailLink(), thumbnailImageView);
            rankingTextView.setText(Utils.getInstance().timeFormatter(newsModel.getPostedTime(), application));
            postedTimeTextView.setText(newsModel.getCategory());

            if (newsModel.getNumber() != -1) {
                numberTextView.setVisibility(View.VISIBLE);
                numberTextView.setText(MessageFormat.format("{0}.", newsModel.getNumber()));
            } else numberTextView.setVisibility(View.GONE);

            sourceTextView.setText(newsModel.getSource());
            if (newsModel.isShowSourceText()) sourceTextView.setVisibility(View.VISIBLE);
            else sourceTextView.setVisibility(View.GONE);
            Utils.getInstance().setImageSource(application, sourceLogos.get(newsModel.getSource()), sourceImageView);

            menuImageView.setOnClickListener(v -> itemView.showContextMenu());

            itemView.setOnClickListener(v -> {
                NewsRepo.getInstance().selectedNewsModel.setValue(newsModel);
                Controller.getInstance().detailsFragment.setValue(true);
            });
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle("Choose an action");
                if (Utils.getInstance().feedContainsNews(savedFeedElementModels, newsModel)) {
                    menu.add(0, v.getId(), 0, "Remove from saved")
                            .setOnMenuItemClickListener(item -> {
                                FeedRepo.getInstance(application).unSaveNews(newsModel);
                                return true;
                            });
                } else {
                    menu.add(0, v.getId(), 0, "Save")
                            .setOnMenuItemClickListener(item -> {
                                FeedRepo.getInstance(application).saveNews(application,newsModel);
                                return true;
                            });
                }

                menu.add(0, v.getId(), 0, "Share")
                        .setOnMenuItemClickListener(item -> {
                            Utils.getInstance().share(application, newsModel.getLink());
                            return true;
                        });
                menu.add(0, v.getId(), 0, "Visit website")
                        .setOnMenuItemClickListener(item -> {
                            Utils.getInstance().openLink(application, newsModel.getLink());
                            return true;
                        });
            });


        }


    }
}
