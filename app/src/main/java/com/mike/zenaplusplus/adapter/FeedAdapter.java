package com.mike.zenaplusplus.adapter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mike.zenaplusplus.App;
import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.models.FeedElementModel;
import com.mike.zenaplusplus.models.NewsModel;
import com.mike.zenaplusplus.models.ToolingModels;
import com.mike.zenaplusplus.repository.FeedRepo;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.mike.zenaplusplus.utils.Controller;
import com.mike.zenaplusplus.utils.Dialogs;
import com.mike.zenaplusplus.utils.Singletons;
import com.mike.zenaplusplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Application application;
    private Activity activity;
    private List<FeedElementModel> feedElementModelList = new ArrayList<>();

    public FeedAdapter(Application application, Activity activity) {
        this.application = application;
        this.activity = activity;
        App.dynamicVariables.observeForever(stringStringMap -> notifyDataSetChanged());
        FeedRepo.getInstance(application).getSavedFeedElements().observeForever(feedElementModels -> notifyDataSetChanged());
    }

    public void setFeedElementModelList(List<FeedElementModel> feedElementModelList) {
        if (feedElementModelList.isEmpty()) return;
        this.feedElementModelList.clear();
        this.feedElementModelList.addAll(feedElementModelList);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        FeedElementModel feedElementModel = feedElementModelList.get(position);
        return feedElementModel.getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case FeedElementModel.BIG_NEWS_ITEM:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_large_news, parent, false);
                return new BigNewsItemHolder(view);
            case FeedElementModel.SMALL_NEWS_ITEM:
                View smallNewsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_news, parent, false);
                return new SmallNewsItemHolder(smallNewsView);
            case FeedElementModel.BIG_DIVIDER_ITEM:
            case FeedElementModel.HEADER_ITEM:
                View otherItemsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_others, parent, false);
                return new OtherItemsViewHolder(otherItemsView);
            default:
                View dummyView = new View(application);
                dummyView.setVisibility(View.GONE);
                return new OtherItemsViewHolder(dummyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FeedElementModel feedElementModel = feedElementModelList.get(position);
        switch (feedElementModel.getItemType()) {
            case FeedElementModel.BIG_NEWS_ITEM:
                ((BigNewsItemHolder) holder).bind(application, activity, feedElementModel);
                break;
            case FeedElementModel.SMALL_NEWS_ITEM:
                ((SmallNewsItemHolder) holder).bind(application, activity, feedElementModel);
                break;
            case FeedElementModel.HEADER_ITEM:
                ((OtherItemsViewHolder) holder).bindHeader(application, feedElementModel);
                break;
            case FeedElementModel.BIG_DIVIDER_ITEM:
                ((OtherItemsViewHolder) holder).bindDivider(application, feedElementModel);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return feedElementModelList.size();
    }

    static class BigNewsItemHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView sourceTextView;
        TextView rankingTextView;
        TextView postedTimeTextView;
        TextView numberTextView;
        ImageView thumbnailImageView;
        ImageView sourceImageView;
        ImageView menuImageView;

        BigNewsItemHolder(@NonNull View itemView) {
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

        void bind(Application application, Activity activity, FeedElementModel feedElementModel) {
            NewsModel newsModel = Singletons.gson().fromJson(feedElementModel.getItemJson(), NewsModel.class);
            List<FeedElementModel> savedFeedElementModels = FeedRepo.getInstance(application).getSavedFeedElements().getValue();
            Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
            titleTextView.setText(newsModel.getTitle());
            Utils.getInstance().setImageSource(application, newsModel.getThumbnailLink(), thumbnailImageView);
            rankingTextView.setText(Utils.getInstance().timeFormatter(newsModel.getPostedTime(), application));
            postedTimeTextView.setText(newsModel.getCategory());

            if (newsModel.getNumber() != -1) {
                numberTextView.setVisibility(View.VISIBLE);
                numberTextView.setText(newsModel.getNumber() + ".");
            } else numberTextView.setVisibility(View.GONE);

            sourceTextView.setText(newsModel.getSource());
            if (newsModel.isShowSourceText()) sourceTextView.setVisibility(View.VISIBLE);
            else sourceTextView.setVisibility(View.GONE);
            Utils.getInstance().setImageSource(application, sourceLogos.get(newsModel.getSource()), sourceImageView);

            menuImageView.setOnClickListener(v -> itemView.showContextMenu());

            itemView.setOnClickListener(v -> {
                NewsRepo.getInstance().selectedNewsId.setValue(newsModel.getId());
                Controller.getInstance().detailsFragment.setValue(true);
            });
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                DocumentReference documentReference = FirebaseFirestore.getInstance().document("News/" + newsModel.getId());
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
                                FeedRepo.getInstance(application).saveNews(application, newsModel.getId());
                                return true;
                            });
                }
                menu.add(0, v.getId(), 0, "Share")
                        .setOnMenuItemClickListener(item -> {
                            Dialogs.getInstate().showLoadingDialog(activity, "Loading");
                            documentReference.get().addOnCompleteListener(t -> {
                                Dialogs.getInstate().hideLoadingDialog();
                                if (t.isSuccessful())
                                    Utils.getInstance().share(application, t.getResult().toObject(NewsModel.class).getLink());
                            });
                            return true;
                        });
                menu.add(0, v.getId(), 0, "Visit website")
                        .setOnMenuItemClickListener(item -> {
                            Dialogs.getInstate().showLoadingDialog(activity, "Loading");
                            documentReference.get().addOnCompleteListener(t -> {
                                Dialogs.getInstate().hideLoadingDialog();
                                if (t.isSuccessful())
                                    Utils.getInstance().openLink(application, t.getResult().toObject(NewsModel.class).getLink());
                            });
                            return true;
                        });
            });
        }
    }

    static class SmallNewsItemHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView sourceTextView;
        TextView rankingTextView;
        TextView postedTimeTextView;
        TextView numberTextView;
        ImageView thumbnailImageView;
        ImageView sourceImageView;
        CardView photoCardView;
        ImageView menuImageView;

        SmallNewsItemHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            rankingTextView = itemView.findViewById(R.id.rankingTextView);
            postedTimeTextView = itemView.findViewById(R.id.postedTimeTextView);
            numberTextView = itemView.findViewById(R.id.numberTextView);
            sourceImageView = itemView.findViewById(R.id.sourceImageView);
            photoCardView = itemView.findViewById(R.id.photoCardView);
            menuImageView = itemView.findViewById(R.id.menuImageView);
        }

        void bind(Application application, Activity activity, FeedElementModel feedElementModel) {
            NewsModel newsModel = Singletons.gson().fromJson(feedElementModel.getItemJson(), NewsModel.class);
            List<FeedElementModel> savedFeedElementModels = FeedRepo.getInstance(application).getSavedFeedElements().getValue();
            Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
            titleTextView.setText(newsModel.getTitle());
            if (newsModel.getThumbnailLink() != null) {
                photoCardView.setVisibility(View.VISIBLE);
                Utils.getInstance().setImageSource(application, newsModel.getThumbnailLink(), thumbnailImageView);
            } else {
                photoCardView.setVisibility(View.GONE);
            }
            rankingTextView.setText(Utils.getInstance().timeFormatter(newsModel.getPostedTime(), application));
            postedTimeTextView.setText(newsModel.getCategory());

            if (newsModel.getNumber() != -1) {
                numberTextView.setVisibility(View.VISIBLE);
                numberTextView.setText(newsModel.getNumber() + ".");
            } else numberTextView.setVisibility(View.GONE);

            if (newsModel.getNumber() != -1) {
                numberTextView.setVisibility(View.VISIBLE);
                numberTextView.setText(newsModel.getNumber() + ".");
            } else numberTextView.setVisibility(View.GONE);

            sourceTextView.setText(newsModel.getSource());
            if (newsModel.isShowSourceText()) sourceTextView.setVisibility(View.VISIBLE);
            else sourceTextView.setVisibility(View.GONE);
            Utils.getInstance().setImageSource(application, sourceLogos.get(newsModel.getSource()), sourceImageView);


            menuImageView.setOnClickListener(v -> itemView.showContextMenu());

            itemView.setOnClickListener(v -> {
                if (newsModel.getBody().isEmpty())
                    NewsRepo.getInstance().selectedNewsId.setValue(newsModel.getId());
                else NewsRepo.getInstance().selectedNewsModel.setValue(newsModel);
                Controller.getInstance().detailsFragment.setValue(true);
            });
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                DocumentReference documentReference = FirebaseFirestore.getInstance().document("News/" + newsModel.getId());
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
                                FeedRepo.getInstance(application).saveNews(application, newsModel.getId());
                                return true;
                            });
                }
                menu.add(0, v.getId(), 0, "Share")
                        .setOnMenuItemClickListener(item -> {
                            Dialogs.getInstate().showLoadingDialog(activity, "Loading");
                            documentReference.get().addOnCompleteListener(t -> {
                                Dialogs.getInstate().hideLoadingDialog();
                                if (t.isSuccessful())
                                    Utils.getInstance().share(application, t.getResult().toObject(NewsModel.class).getLink());
                            });
                            return true;
                        });
                menu.add(0, v.getId(), 0, "Visit website")
                        .setOnMenuItemClickListener(item -> {
                            Dialogs.getInstate().showLoadingDialog(activity, "Loading");
                            documentReference.get().addOnCompleteListener(t -> {
                                Dialogs.getInstate().hideLoadingDialog();
                                if (t.isSuccessful())
                                    Utils.getInstance().openLink(application, t.getResult().toObject(NewsModel.class).getLink());
                            });
                            return true;
                        });
            });
        }
    }

    static class OtherItemsViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;
        LinearLayout bigDivider;

        public OtherItemsViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
            bigDivider = itemView.findViewById(R.id.bigDivider);
        }

        void bindHeader(Context context, FeedElementModel feedElementModel) {
            bigDivider.setVisibility(View.GONE);
            ToolingModels.HeaderModel headerModel = Singletons.gson().fromJson(feedElementModel.getItemJson(), ToolingModels.HeaderModel.class);
            headerTextView.setVisibility(View.VISIBLE);
            headerTextView.setText(headerModel.text);
        }

        void bindDivider(Context context, FeedElementModel feedElementModel) {
            bigDivider.setVisibility(View.VISIBLE);
            headerTextView.setVisibility(View.GONE);
        }
    }
}
