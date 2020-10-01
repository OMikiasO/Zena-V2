package com.chaosapps.zena.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.chaosapps.zena.App;
import com.chaosapps.zena.R;
import com.chaosapps.zena.models.FeedElementModel;
import com.chaosapps.zena.models.NewsModel;
import com.chaosapps.zena.repository.NewsRepo;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class Utils {
    private final static String TAG = "Utils";
    private DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
    private RequestOptions requestOptions = new RequestOptions();

    private static Utils INSTANCE;

    private Utils() {
    }

    public static synchronized Utils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Utils();
        }
        return INSTANCE;
    }

    public void navSelect(Activity activity, CardView navCardView, int id) {
        try{
        TextView forYouNavTextView = navCardView.findViewById(R.id.forYouNavTextView);
        TextView headlinesTextView = navCardView.findViewById(R.id.headlinesTextView);
        TextView followingNavTextView = navCardView.findViewById(R.id.followingNavTextView);
        TextView moreNavTextView = navCardView.findViewById(R.id.moreNavTextView);
        ImageView forYouVanImageView = navCardView.findViewById(R.id.forYouNavImageView);
        ImageView headlinesImageView = navCardView.findViewById(R.id.headlinesNavImageView);
        ImageView followingImageView = navCardView.findViewById(R.id.followingNavImageView);
        ImageView moreImageView = navCardView.findViewById(R.id.moreNavImageView);

        TextView[] textViews = {forYouNavTextView, headlinesTextView, followingNavTextView, moreNavTextView};
        ImageView[] imageViews = {forYouVanImageView, headlinesImageView, followingImageView, moreImageView};

        for (int i = 0; i < textViews.length; i++) {
            if (i == id) {
                imageViews[i].setColorFilter(ContextCompat.getColor(activity, R.color.accentPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                textViews[i].setTextColor(activity.getResources().getColor(R.color.accentPrimary));
            } else {
                imageViews[i].setColorFilter(ContextCompat.getColor(activity, R.color.secondary), android.graphics.PorterDuff.Mode.SRC_IN);
                textViews[i].setTextColor(activity.getResources().getColor(R.color.secondary));
            }

        }} catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    public void makeToast(Context context, String message) {
        try{
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.taost_layout, null);
        toast.setView(view);
        ((TextView) view.findViewById(R.id.message)).setText(message);
        toast.show();} catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    public void setImageSource(Context activity, Object imgLink, ImageView imageView) {
        try{
        Glide.with(activity)
                .load(imgLink)
                .transition(withCrossFade(factory))
                .apply(requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL).timeout(30000).override(600, 350))
                .into(imageView);} catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    String getFirstAndLastId(List<FeedElementModel> feedElementModels) {
        List<String> idList = new ArrayList<>();
        for (FeedElementModel feedElementModel : feedElementModels) {
            if (feedElementModel.getItemType() == FeedElementModel.BIG_NEWS_ITEM || feedElementModel.getItemType() == FeedElementModel.SMALL_NEWS_ITEM) {
                NewsModel newsModel = Singletons.gson().fromJson(feedElementModel.getItemJson(), NewsModel.class);
                idList.add(newsModel.getId());
            }
        }
        Collections.sort(idList);
        if (idList.isEmpty()) return null;
        return idList.get(0) + "," + idList.get(idList.size() - 1);
    }

    public String timeFormatter(long postedTime, Context context) {
        long difference = System.currentTimeMillis() - postedTime;
        if (difference < 60000L) {
            return context.getString(R.string.moments_ago);
        } else if (difference < 3600000L) {
            if (difference / 60000L < 2) {
                return difference / 60000L + " " + context.getString(R.string.min_ago);
            } else {
                return difference / 60000L + " " + context.getString(R.string.mins_ago);
            }
        } else if (difference < 86400000L) {
            if (difference / 3600000L < 2) {
                return difference / 3600000L + " " + context.getString(R.string.hr_ago);
            } else {
                return difference / 3600000L + " " + context.getString(R.string.hrs_ago);
            }
        } else if (difference < 2592000000L) {
            if (difference / 86400000L < 2) {
                return difference / 86400000L + " " + context.getString(R.string.day_ago);
            } else {
                return difference / 86400000L + " " + context.getString(R.string.days_ago);
            }
        } else if (difference < 31104000000L) {
            if (difference / 2592000000L < 2) {
                return difference / 2592000000L + " " + context.getString(R.string.month_ago);
            } else {
                return difference / 2592000000L + " " + context.getString(R.string.months_ago);
            }
        } else {
            if (difference / 31104000000L < 2) {
                return difference / 31104000000L + " " + context.getString(R.string.year_ago);
            } else {
                return difference / 31104000000L + " " + context.getString(R.string.years_ago);
            }
        }
    }

    public void share(Context context, String link) {
        if(link==null) {
            makeToast(context, "News hasn't loaded yet");
            return;
        }
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Zena app");
            String shareMessage = link+ "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            context.startActivity(Intent.createChooser(shareIntent, "Choose one"));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void openLink(Context context, String link){
        if(link==null) {
            makeToast(context, "News hasn't loaded yet");
            return;
        }
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(link));
            context.startActivity(i);
        } catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

    }

    public void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboardFrom(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public boolean feedContainsNews(List<FeedElementModel> feedElementModels, NewsModel newsModel){
        for (FeedElementModel feedElementModel : feedElementModels) {
            NewsModel savedNewsModel = Singletons.gson().fromJson(feedElementModel.getItemJson(), NewsModel.class);
            if(savedNewsModel.getId().equals(newsModel.getId())){
                return true;
            }
        }
        return false;
    }

    public void buildMainFeed(List<NewsModel> newsModelList, boolean loadMore){
        try{
            Map<String, Double> rankingVariables = App.dynamicVariables.getValue().rankingVariables;
            for (NewsModel newsModel : newsModelList) {
                long freshness = (System.currentTimeMillis() - newsModel.getPostedTime()) / 60000;
                newsModel.setRankingScore(rankingVariables.get("P") * Math.pow(newsModel.getRelevanceScore(), rankingVariables.get("relevanceScoreExponent")) / Math.pow(freshness, rankingVariables.get("freshnessExponent")));
            }
            Collections.sort(newsModelList);

            if(!loadMore){
                for (int i = 0; i < newsModelList.size(); i++) {
                    NewsModel newsModel = newsModelList.get(i);
                    if (i < 5) newsModel.setNumber(i + 1);
                    if (i > 0 && i < 5) newsModel.setNewsType(NewsModel.SMALL_NEWS_ITEM);
                }
            }
            List<Object> mainFeed;
            if(!loadMore) mainFeed = new ArrayList<>();
            else mainFeed = new ArrayList<>(Objects.requireNonNull(NewsRepo.getInstance().mainFeedNewsList.getValue()));

            mainFeed.addAll(newsModelList);

            if(!loadMore){
                FeedElementModel headerElement = new FeedElementModel();
                headerElement.setItemType(FeedElementModel.HEADER_ITEM);
                headerElement.setHeaderText("Top 5 News right now");
                mainFeed.add(0, headerElement);
                FeedElementModel bigDivider = new FeedElementModel();
                bigDivider.setItemType(FeedElementModel.BIG_DIVIDER_ITEM);
                mainFeed.add(6, bigDivider);
            }

            NewsRepo.getInstance().mainFeedNewsList.setValue(mainFeed);
        } catch (Exception e){
            recordException(e);
        }

    }

    public void recordException(Exception e){
        FirebaseCrashlytics.getInstance().recordException(e);
        Log.e(TAG, e.getMessage());
        try {
            FirebaseCrashlytics.getInstance().setUserId(Account.getInstance().user.getValue().getUserId());
        } catch (Exception exception){
            Log.e(TAG, exception.getMessage());
        }
    }

    public String removeSpecialCharacters(String s){
        return  s.replaceAll("[-+_)(*&^%$#@!=~`';/.,\\]\\[|}{:?><]","");
    }
}
