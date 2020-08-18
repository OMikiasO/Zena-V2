package com.mike.zenaplusplus.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mike.zenaplusplus.repository.NewsRepo;
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
    private final int ITEM_TYPE = 1;
    private final int RELATED_NEWS = 2;
    private static final int IMAGE = 3;
    private static final int HEADER = 4;
    private static final int MAIN_HEADER = 5;
    private static final int PARAGRAPH = 6;
    private static final int AUDIO = 7;
    private static final int SHARE_AND_VISIT = 8;
    private static final int PROGRESS_BAR = 9;

    public DetailsAdapter(Context context) {
        this.context = context;

    }

    public void setNews(NewsDetailsModel newsDetailsModel) {
        this.newsBodyItems.clear();
        this.newsBodyItems.add("mainHeader");
        this.newsBodyItems.add("headerPlaceHolder");
        this.newsBodyItems.addAll(newsDetailsModel.getBody());
        if (newsDetailsModel.getAudio() != null) {
            Log.e(TAG, "Audio initialzing");
            this.newsBodyItems.add(2, "audio");
            this.newsBodyItems.add(3, newsDetailsModel.getAudio());
        }
        if (!this.newsBodyItems.get(2).equals(IMAGE) && newsDetailsModel.getThumbnailLink() != null) {
            this.newsBodyItems.add(2, "image");
            this.newsBodyItems.add(3, newsDetailsModel.getThumbnailLink());
        }
        newsBodyItems.add("shareAndVisit");
        newsBodyItems.add("shareAndVisitPlaceHolder");

        newsBodyItems.add("progressBar");
        newsBodyItems.add("progressBarPlaceHolder");
        Log.e(TAG, this.newsBodyItems.toString());
        notifyDataSetChanged();

        NewsRepo.getInstance().loadingRelatedNews.observe((LifecycleOwner) context, aBoolean -> {
            try {
                if(!aBoolean) {
                    int indexOfProgressBar = newsBodyItems.indexOf("progressBarPlaceHolder");
                    newsBodyItems.remove("progressBar");
                    newsBodyItems.remove("progressBarPlaceHolder");
                    notifyItemRangeRemoved(indexOfProgressBar-1,2);
                }
            } catch (Exception e){
                Log.e(TAG, e.getMessage());
            }
        });

        NewsRepo.getInstance().relatedNewsList.observe((LifecycleOwner) context, newsModelList -> {

                if(newsModelList.size()>0){
                    newsBodyItems.add("header");
                    newsBodyItems.add("More from "+newsDetailsModel.getSource());
                    for (int i = 0; i < newsModelList.size(); i++) {
                        newsBodyItems.add("relatedNews");
                        newsBodyItems.add(""+i);
                    }
                    notifyItemRangeInserted(newsBodyItems.size(), newsModelList.size());
                }

        });

    }

    @Override
    public int getItemViewType(int position) {
         if (position % 2 != 0) {
            switch (newsBodyItems.get(position - 1)) {
                case "image":
                    return IMAGE;
                case "header":
                    return HEADER;
                case "mainHeader":
                    return MAIN_HEADER;
                case "paragraph":
                    return PARAGRAPH;
                case "audio":
                    return AUDIO;
                case "shareAndVisit":
                    return SHARE_AND_VISIT;
                case "progressBar":
                    return PROGRESS_BAR;
                case "relatedNews":
                    return RELATED_NEWS;
                default:
                    return 10;
            }
        } else {
            return ITEM_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case IMAGE:
                View imageCardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_image, parent, false);
                return new ImageItemHolder(imageCardView);
            case HEADER:
                View headerTextView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_header, parent, false);
                return new HeaderItemHolder(headerTextView);
            case MAIN_HEADER:
                View mainHeaderView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_main_header, parent, false);
                return new MainHeaderItemHolder(mainHeaderView);
            case PARAGRAPH:
                View paragraphTextView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_paragraph, parent, false);
                return new ParagraphItemHolder(paragraphTextView);
            case AUDIO:
                View playerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_player_view, parent, false);
                return new PlayerItemHolder(playerView);
            case SHARE_AND_VISIT:
                View shareAndVisitView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_share_and_visit, parent, false);
                return new ShareAndVisitItemHolder(shareAndVisitView);
            case PROGRESS_BAR:
                View progressBarView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dtails_progress_bar, parent, false);
                return new ProgressBarItemHolder(progressBarView);
            case RELATED_NEWS:
                View smallNewsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_news, parent, false);
                return new NewsAdapter.SmallNewsItemHolder(smallNewsView);
            case ITEM_TYPE:
            default:
                View emptyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.details_empty, parent, false);
                return new EmptyItemHolder(emptyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case RELATED_NEWS:
                ((NewsAdapter.SmallNewsItemHolder) holder).bind(context, NewsRepo.getInstance().relatedNewsList.getValue().get(Integer.parseInt(newsBodyItems.get(position))));
                break;
            case IMAGE:
                ((ImageItemHolder) holder).bind(context, newsBodyItems.get(position));
                break;
            case HEADER:
                ((HeaderItemHolder) holder).bind(newsBodyItems.get(position));
                break;
            case MAIN_HEADER:
                ((MainHeaderItemHolder) holder).bind(context);
                break;
            case PARAGRAPH:
                ((ParagraphItemHolder) holder).bind(newsBodyItems.get(position));
                break;
            case AUDIO:
                ((PlayerItemHolder) holder).bind(context);
                break;
            case SHARE_AND_VISIT:
                ((ShareAndVisitItemHolder) holder).bind(context);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return newsBodyItems.size();
    }

    static class ImageItemHolder extends RecyclerView.ViewHolder {
        CardView imageCardView;

        ImageItemHolder(@NonNull View itemView) {
            super(itemView);
            imageCardView = itemView.findViewById(R.id.imageCardView);
        }

        void bind(Context context, String bodyItem) {
            Utils.getInstance().setImageSource(context, bodyItem, imageCardView.findViewById(R.id.menuImageView));
        }
    }

    static class HeaderItemHolder extends RecyclerView.ViewHolder {
        TextView headerTextView;

        HeaderItemHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.headerTextView);
        }

        void bind(String bodyItem) {
            headerTextView.setText(bodyItem);
        }
    }

    static class MainHeaderItemHolder extends RecyclerView.ViewHolder {
        ConstraintLayout mainHeaderCL;

        MainHeaderItemHolder(@NonNull View itemView) {
            super(itemView);
            mainHeaderCL = itemView.findViewById(R.id.mainHeaderCL);
        }

        void bind(Context context) {
            NewsDetailsModel newsDetailsModel = NewsRepo.getInstance().selectedNewsModel.getValue();

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
        }
    }

    static class ParagraphItemHolder extends RecyclerView.ViewHolder {
        TextView paragraphTextView;

        ParagraphItemHolder(@NonNull View itemView) {
            super(itemView);
            paragraphTextView = itemView.findViewById(R.id.paragraphTextView);
        }

        void bind(String bodyItem) {
            paragraphTextView.setText(bodyItem);
        }
    }

    static class PlayerItemHolder extends RecyclerView.ViewHolder {
        CardView playerCardView;

        PlayerItemHolder(@NonNull View itemView) {
            super(itemView);
            playerCardView = itemView.findViewById(R.id.playerCardView);
        }

        void bind(Context context) {
            NewsDetailsModel newsDetailsModel = NewsRepo.getInstance().selectedNewsModel.getValue();
            PlayerUtils.getInstance().initMediaPlayer(context, newsDetailsModel);
            ((PlayerControlView) playerCardView.findViewById(R.id.playerView)).setPlayer(PlayerUtils.getInstance().player);
            ((TextView) playerCardView.findViewById(R.id.sourceTextView)).setText(newsDetailsModel.getSource());
            (playerCardView.findViewById(R.id.stopImageButton)).setOnClickListener(v -> {
                PlayerUtils.getInstance().player.seekTo(0);
                PlayerUtils.getInstance().player.setPlayWhenReady(false);
            });
            Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
            Utils.getInstance().setImageSource(context, sourceLogos.get(newsDetailsModel.getSource()), (playerCardView.findViewById(R.id.sourceImageView)));
        }
    }

    static class ShareAndVisitItemHolder extends RecyclerView.ViewHolder {
        LinearLayout shareAndVisitLL;

        ShareAndVisitItemHolder(@NonNull View itemView) {
            super(itemView);
            shareAndVisitLL = itemView.findViewById(R.id.shareAndVisitLL);
        }

        void bind(Context context) {
            NewsDetailsModel newsDetailsModel = NewsRepo.getInstance().selectedNewsModel.getValue();
            shareAndVisitLL.findViewById(R.id.shareBtn).setOnClickListener(v -> Utils.getInstance().share(context, newsDetailsModel.getLink()));
            shareAndVisitLL.findViewById(R.id.visitWebsiteBtn).setOnClickListener(v -> Utils.getInstance().openLink(context, newsDetailsModel.getLink()));
        }
    }

    static class EmptyItemHolder extends RecyclerView.ViewHolder {

        EmptyItemHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class ProgressBarItemHolder extends RecyclerView.ViewHolder {

        ProgressBarItemHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
