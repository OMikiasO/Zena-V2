package com.chaosapps.zena.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chaosapps.zena.App;
import com.chaosapps.zena.R;
import com.chaosapps.zena.models.FeedElementModel;
import com.chaosapps.zena.models.NewsModel;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.AdminUtils;
import com.chaosapps.zena.utils.CacheUtils;
import com.chaosapps.zena.utils.Controller;
import com.chaosapps.zena.utils.Utils;
import com.google.firebase.firestore.Source;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int FEED_ELEMENT = 0;
    private final int BIG_NEWS_ELEMENT = 1;
    private final int SMALL_NEWS_ELEMENT = 2;

    private Fragment fragment;
    private List<Object> mainFeed = new ArrayList<>();

    public NewsAdapter(Fragment fragment) {
        this.fragment = fragment;
        CacheUtils.getInstance().savedNewsIds.observe(fragment.getViewLifecycleOwner(), strings -> notifyDataSetChanged());
    }

    public void setMainFeed(List<Object> mainFeed) {
        this.mainFeed.clear();
        this.mainFeed.addAll(mainFeed);
        notifyDataSetChanged();
    }

    public void setNewsList(List<NewsModel> newsModels) {
        this.mainFeed.clear();
        this.mainFeed.addAll(newsModels);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mainFeed.get(position) instanceof NewsModel) {
            switch (((NewsModel) mainFeed.get(position)).getNewsType()) {
                case NewsModel.SMALL_NEWS_ITEM:
                    return SMALL_NEWS_ELEMENT;
                case NewsModel.BIG_NEWS_ITEM:
                default:
                    return BIG_NEWS_ELEMENT;
            }
        } else return FEED_ELEMENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case FEED_ELEMENT:
                View feedElementView = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_others, parent, false);
                return new FeedElementItemsViewHolder(feedElementView);
            case SMALL_NEWS_ELEMENT:
                View smallNewsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_news, parent, false);
                return new SmallNewsItemHolder(smallNewsView);
            case BIG_NEWS_ELEMENT:
            default:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_large_news, parent, false);
                return new NewsItemHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case FEED_ELEMENT:
                FeedElementModel feedElementModel = (FeedElementModel) mainFeed.get(position);
                switch (feedElementModel.getItemType()) {
                    case FeedElementModel.BIG_DIVIDER_ITEM:
                        ((FeedElementItemsViewHolder) holder).bindDivider();
                        break;
                    case FeedElementModel.HEADER_ITEM:
                        ((FeedElementItemsViewHolder) holder).bindHeader(feedElementModel);
                        break;
                }
                break;
            case SMALL_NEWS_ELEMENT:
                ((SmallNewsItemHolder) holder).bind(fragment.getActivity(), (NewsModel) mainFeed.get(position));
                break;
            case BIG_NEWS_ELEMENT:
            default:
                ((NewsItemHolder) holder).bind(fragment.getActivity(), (NewsModel) mainFeed.get(position));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mainFeed.size();
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

        void bind(Activity activity, NewsModel newsModel) {
            List<String> savedNewsIds = CacheUtils.getInstance().savedNewsIds.getValue();
            Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
            titleTextView.setText(newsModel.getTitle());
            Utils.getInstance().setImageSource(activity, newsModel.getThumbnailLink(), thumbnailImageView);
            rankingTextView.setText(Utils.getInstance().timeFormatter(newsModel.getPostedTime(), activity));
            postedTimeTextView.setText(newsModel.getCategory());

            if (newsModel.getNumber() != -1) {
                numberTextView.setVisibility(View.VISIBLE);
                numberTextView.setText(MessageFormat.format("{0}.", newsModel.getNumber()));
            } else numberTextView.setVisibility(View.GONE);

            sourceTextView.setText(newsModel.getSource());
            if (newsModel.isShowSourceText()) sourceTextView.setVisibility(View.VISIBLE);
            else sourceTextView.setVisibility(View.GONE);
            Utils.getInstance().setImageSource(activity, sourceLogos.get(newsModel.getSource()), sourceImageView);

            menuImageView.setOnClickListener(v -> itemView.showContextMenu());

            itemView.setOnClickListener(v -> {
                NewsRepo.getInstance().selectedNewsId.setValue(newsModel.getId());
                Controller.getInstance().detailsFragment.setValue(true);
            });
            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle("Choose an action");

                if (savedNewsIds.contains(newsModel.getId())) {
                    menu.add(0, v.getId(), 0, "Remove from saved")
                            .setOnMenuItemClickListener(item -> {
                                CacheUtils.getInstance().unSaveNewsId(newsModel.getId());
                                return true;
                            });
                } else {
                    menu.add(0, v.getId(), 0, "Save")
                            .setOnMenuItemClickListener(item -> {
                                CacheUtils.getInstance().saveNewsId(activity, newsModel.getId(), Source.DEFAULT);
                                return true;
                            });
                }

                menu.add(0, v.getId(), 0, "Share")
                        .setOnMenuItemClickListener(item -> {
                            Utils.getInstance().share(activity, newsModel.getLink());
                            return true;
                        });
                menu.add(0, v.getId(), 0, "Visit website")
                        .setOnMenuItemClickListener(item -> {
                            Utils.getInstance().openLink(activity, newsModel.getLink());
                            return true;
                        });
                menu.add(0, v.getId(), 0, "Send notification")
                        .setOnMenuItemClickListener(item -> {
                            AdminUtils.getInstance().sendNotification(activity, newsModel);
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

        void bind(Activity activity, NewsModel newsModel) {
            List<String> savedNewsIds = CacheUtils.getInstance().savedNewsIds.getValue();
            Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
            titleTextView.setText(newsModel.getTitle());
            if (newsModel.getThumbnailLink() != null) {
                photoCardView.setVisibility(View.VISIBLE);
                Utils.getInstance().setImageSource(activity, newsModel.getThumbnailLink(), thumbnailImageView);
            } else {
                photoCardView.setVisibility(View.GONE);
            }
            rankingTextView.setText(Utils.getInstance().timeFormatter(newsModel.getPostedTime(), activity));
            postedTimeTextView.setText(newsModel.getCategory());

            if (newsModel.getNumber() != -1) {
                numberTextView.setVisibility(View.VISIBLE);
                numberTextView.setText(MessageFormat.format("{0}.", newsModel.getNumber()));
            } else numberTextView.setVisibility(View.GONE);

            sourceTextView.setText(newsModel.getSource());
            if (newsModel.isShowSourceText()) sourceTextView.setVisibility(View.VISIBLE);
            else sourceTextView.setVisibility(View.GONE);
            Utils.getInstance().setImageSource(activity, sourceLogos.get(newsModel.getSource()), sourceImageView);

            itemView.setOnClickListener(v -> {
                NewsRepo.getInstance().selectedNewsId.setValue(newsModel.getId());
                Controller.getInstance().detailsFragment.setValue(true);
            });

            menuImageView.setOnClickListener(v -> itemView.showContextMenu());

            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle("Choose an action");

                if (savedNewsIds.contains(newsModel.getId())) {
                    menu.add(0, v.getId(), 0, "Remove from saved")
                            .setOnMenuItemClickListener(item -> {
                                CacheUtils.getInstance().unSaveNewsId(newsModel.getId());
                                return true;
                            });
                } else {
                    menu.add(0, v.getId(), 0, "Save")
                            .setOnMenuItemClickListener(item -> {
                                CacheUtils.getInstance().saveNewsId(activity, newsModel.getId(), Source.DEFAULT);
                                return true;
                            });
                }

                menu.add(0, v.getId(), 0, "Share")
                        .setOnMenuItemClickListener(item -> {
                            Utils.getInstance().share(activity, newsModel.getLink());
                            return true;
                        });
                menu.add(0, v.getId(), 0, "Visit website")
                        .setOnMenuItemClickListener(item -> {
                            Utils.getInstance().openLink(activity, newsModel.getLink());
                            return true;
                        });
                menu.add(0, v.getId(), 0, "Send notification")
                        .setOnMenuItemClickListener(item -> {
                            AdminUtils.getInstance().sendNotification(activity, newsModel);
                            return true;
                        });
            });

        }
    }


    static class FeedElementItemsViewHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;
        LinearLayout bigDivider;

        public FeedElementItemsViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
            bigDivider = itemView.findViewById(R.id.bigDivider);
        }

        void bindHeader(FeedElementModel feedElementModel) {
            bigDivider.setVisibility(View.GONE);
            headerTextView.setVisibility(View.VISIBLE);
            headerTextView.setText(feedElementModel.getHeaderText());
        }

        void bindDivider() {
            bigDivider.setVisibility(View.VISIBLE);
            headerTextView.setVisibility(View.GONE);
        }
    }
}
