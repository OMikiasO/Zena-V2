package com.mike.zenaplusplus.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.mike.zenaplusplus.App;
import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.models.NewsDetailsModel;
import com.mike.zenaplusplus.models.NewsModel;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.mike.zenaplusplus.utils.Controller;
import com.mike.zenaplusplus.utils.PlayerUtils;
import com.mike.zenaplusplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;

public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "DetailsAdapter";

    private Context context;
    private List<String> newsBodyItems = new ArrayList<>();
    private final int ITEM = 0;
    private final int ITEM_TYPE = 1;
    private final int RELATED_NEWS = 2;
    private static final String IMAGE = "image";
    private static final String MAIN_HEADER = "mainHeader";
    private static final String PARAGRAPH = "paragraph";
    private static final String HEADER = "header";
    private static final String AUDIO = "audio";
    private static final String SHARE_AND_VISIT = "shareAndVisit";

    public DetailsAdapter(Context context) {
        this.context = context;
    }

    public void setNews(NewsDetailsModel newsDetailsModel) {
        this.newsBodyItems.clear();
        this.newsBodyItems.add(MAIN_HEADER);
        this.newsBodyItems.add("headerPlaceHolder");
        this.newsBodyItems.addAll(newsDetailsModel.getBody());
        if (newsDetailsModel.getAudio() != null) {
            Log.e(TAG, "Audio initialzing");
            this.newsBodyItems.add(2, AUDIO);
            this.newsBodyItems.add(3, newsDetailsModel.getAudio());
        }
        if (!this.newsBodyItems.get(2).equals(IMAGE) && newsDetailsModel.getThumbnailLink() != null) {
            this.newsBodyItems.add(2, IMAGE);
            this.newsBodyItems.add(3, newsDetailsModel.getThumbnailLink());
        }
        newsBodyItems.add(SHARE_AND_VISIT);
        newsBodyItems.add("shareAndVisitPlaceHolder");
        newsBodyItems.add(HEADER);
        newsBodyItems.add("More from "+newsDetailsModel.getSource());
        newsBodyItems.add("relatedNewsPlaceHolder");
        newsBodyItems.add("relatedNewsPlaceHolder");
        newsBodyItems.add("relatedNewsPlaceHolder");
        Log.e(TAG, this.newsBodyItems.toString());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == newsBodyItems.size() - 1 || position == newsBodyItems.size() - 2 || position == newsBodyItems.size() - 3) {//if position is on last 3 items type is related news view
            return RELATED_NEWS;
        } else if (position % 2 != 0) {
            return ITEM;
        } else {
            return ITEM_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_news_detail, parent, false);
                return new DetailsItemHolder(view);
            case RELATED_NEWS:
                View smallNewsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_news, parent, false);
                return new SmallNewsItemHolder(smallNewsView);
            case ITEM_TYPE:
            default:
                View dummyView = new View(context);
                dummyView.setVisibility(GONE);
                return new DetailsAdapter.DetailsItemHolder(dummyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == RELATED_NEWS) {//if position is on last 3 items type is related news view
            ((SmallNewsItemHolder) holder).bind(context, newsBodyItems.size() - (position + 1));
        } else if (position != 0 && viewType == ITEM) {
            ((DetailsItemHolder) holder).bind(context, newsBodyItems.get(position), newsBodyItems.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return newsBodyItems.size();
    }

    static class DetailsItemHolder extends RecyclerView.ViewHolder {

        CardView imageCardView;
        ConstraintLayout mainHeaderCL;
        TextView paragraphTextView;
        TextView headerTextView;
        CardView playerCardView;
        LinearLayout shareAndVisitLL;

        DetailsItemHolder(@NonNull View itemView) {
            super(itemView);
            imageCardView = itemView.findViewById(R.id.imageCardView);
            mainHeaderCL = itemView.findViewById(R.id.mainHeaderCL);
            paragraphTextView = itemView.findViewById(R.id.paragraphTextView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
            playerCardView = itemView.findViewById(R.id.playerCardView);
            shareAndVisitLL = itemView.findViewById(R.id.shareAndVisitLL);
        }

        void bind(Context context, String bodyItem, String type) {
            Log.e(TAG, type);
            NewsDetailsModel newsDetailsModel = NewsRepo.getInstance().selectedNewsModel.getValue();
            mainHeaderCL.setVisibility(GONE);
            imageCardView.setVisibility(GONE);
            switch (type) {
                case IMAGE:
                    Utils.getInstance().setImageSource(context, bodyItem, imageCardView.findViewById(R.id.menuImageView));
                    hideAllExcept(IMAGE);
                    break;
                case MAIN_HEADER:
                    ((TextView) mainHeaderCL.findViewById(R.id.titleTextView)).setText(newsDetailsModel.getTitle());
                    ((TextView) mainHeaderCL.findViewById(R.id.categoryTextView)).setText(newsDetailsModel.getCategory());
                    ((TextView) mainHeaderCL.findViewById(R.id.postedTimeTextView)).setText(Utils.getInstance().timeFormatter(newsDetailsModel.getPostedTime(), context));
                    if (newsDetailsModel.getAudio() != null) {
                        (mainHeaderCL.findViewById(R.id.sourceTextView)).setVisibility(GONE);
                        (mainHeaderCL.findViewById(R.id.sourceImageView)).setVisibility(GONE);
                        (mainHeaderCL.findViewById(R.id.firstDotTextView)).setVisibility(GONE);
                    } else {
                        if (newsDetailsModel.isShowSourceText())
                            ((TextView) mainHeaderCL.findViewById(R.id.sourceTextView)).setText(newsDetailsModel.getSource());
                        else (mainHeaderCL.findViewById(R.id.sourceTextView)).setVisibility(GONE);
                        Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
                        Utils.getInstance().setImageSource(context, sourceLogos.get(newsDetailsModel.getSource()), (mainHeaderCL.findViewById(R.id.sourceImageView)));
                    }
                    hideAllExcept(MAIN_HEADER);
                    break;
                case PARAGRAPH:
                    paragraphTextView.setText(bodyItem);
                    hideAllExcept(PARAGRAPH);
                    break;
                case HEADER:
                    headerTextView.setText(bodyItem);
                    hideAllExcept(HEADER);
                    break;
                case AUDIO:
                    PlayerUtils.getInstance().initMediaPlayer(context, newsDetailsModel);
                    ((PlayerControlView) playerCardView.findViewById(R.id.playerView)).setPlayer(PlayerUtils.getInstance().player);
                    ((TextView) playerCardView.findViewById(R.id.sourceTextView)).setText(newsDetailsModel.getSource());
                    (playerCardView.findViewById(R.id.stopImageButton)).setOnClickListener(v -> {
                        PlayerUtils.getInstance().player.seekTo(0);
                        PlayerUtils.getInstance().player.setPlayWhenReady(false);
                    });
                    Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
                    Utils.getInstance().setImageSource(context, sourceLogos.get(newsDetailsModel.getSource()), (playerCardView.findViewById(R.id.sourceImageView)));
                    hideAllExcept(AUDIO);
                    break;
                case SHARE_AND_VISIT:
                    shareAndVisitLL.findViewById(R.id.shareBtn).setOnClickListener(v->Utils.getInstance().share(context, newsDetailsModel.getLink()));
                    shareAndVisitLL.findViewById(R.id.visitWebsiteBtn).setOnClickListener(v->Utils.getInstance().openLink(context, newsDetailsModel.getLink()));
                    hideAllExcept(SHARE_AND_VISIT);
                    break;
                default:
                    hideAllExcept("null");
                    break;
            }

        }

        void hideAllExcept(String type) {
            if (type.equals(IMAGE)) imageCardView.setVisibility(View.VISIBLE);
            else imageCardView.setVisibility(GONE);
            if (type.equals(MAIN_HEADER)) mainHeaderCL.setVisibility(View.VISIBLE);
            else mainHeaderCL.setVisibility(GONE);
            if (type.equals(PARAGRAPH)) paragraphTextView.setVisibility(View.VISIBLE);
            else paragraphTextView.setVisibility(GONE);
            if (type.equals(HEADER)) headerTextView.setVisibility(View.VISIBLE);
            else headerTextView.setVisibility(GONE);
            if (type.equals(AUDIO)) playerCardView.setVisibility(View.VISIBLE);
            else playerCardView.setVisibility(GONE);
            if (type.equals(SHARE_AND_VISIT)) shareAndVisitLL.setVisibility(View.VISIBLE);
            else shareAndVisitLL.setVisibility(GONE);
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
        }

        void bind(Context context, int position) {
            NewsRepo.getInstance().relatedNewsList.observe((LifecycleOwner) context, newsModels -> {
                try {
                    itemView.setVisibility(View.VISIBLE);
                    NewsModel newsModel = newsModels.get(position);
                    Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
                    titleTextView.setText(newsModel.getTitle());
                    if (newsModel.getThumbnailLink() != null) {
                        photoCardView.setVisibility(View.VISIBLE);
                        Utils.getInstance().setImageSource(context, newsModel.getThumbnailLink(), thumbnailImageView);
                    } else {
                        photoCardView.setVisibility(View.GONE);
                    }
                    rankingTextView.setText(Utils.getInstance().timeFormatter(newsModel.getPostedTime(), context));
                    postedTimeTextView.setText(newsModel.getCategory());

                    numberTextView.setVisibility(View.GONE);

                    sourceTextView.setText(newsModel.getSource());
                    if (newsModel.isShowSourceText()) sourceTextView.setVisibility(View.VISIBLE);
                    else sourceTextView.setVisibility(View.GONE);
                    Utils.getInstance().setImageSource(context, sourceLogos.get(newsModel.getSource()), sourceImageView);

                    itemView.setOnClickListener(v -> {
                        NewsRepo.getInstance().selectedNewsId.setValue(newsModel.getId());
                        Controller.getInstance().detailsFragment.setValue(true);
                    });
                } catch (Exception e){
                    itemView.setVisibility(GONE);
                    Log.e(TAG, e.getMessage());
                }
            });
        }
    }
}
