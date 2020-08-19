package com.mike.zenaplusplus;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.mike.zenaplusplus.ui.details.DetailsFragment;
import com.mike.zenaplusplus.ui.following.FollowingFragment;
import com.mike.zenaplusplus.ui.following.SavedNewsFragment;
import com.mike.zenaplusplus.ui.following.SourcesFragment;
import com.mike.zenaplusplus.ui.forYou.ForYouFragment;
import com.mike.zenaplusplus.ui.headlines.HeadlinesFragment;
import com.mike.zenaplusplus.ui.more.MoreFragment;
import com.mike.zenaplusplus.ui.search.SearchFragment;
import com.mike.zenaplusplus.utils.Account;
import com.mike.zenaplusplus.utils.Controller;
import com.mike.zenaplusplus.utils.PlayerUtils;
import com.mike.zenaplusplus.utils.Singletons;
import com.mike.zenaplusplus.utils.Utils;

import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Fragment fragmentForYou;
    Fragment fragmentHeadlines;
    Fragment fragmentFollowing;
    Fragment fragmentMore;
    final FragmentManager fm = getSupportFragmentManager();
    Fragment active;

    //Views
    CardView miniPlayerCardView;
    CardView navCardView;

    //vars
    long backSelectedTime = 0;
    int skipper = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        reStateData(savedInstanceState);
        allFindViewByIds();
        setUpFragments(savedInstanceState);
        setUpCustomNavView(savedInstanceState);
        setUpMiniPlayer();
    }


    private void allFindViewByIds() {
        navCardView = findViewById(R.id.navCardView);
        miniPlayerCardView = findViewById(R.id.miniPlayerCardView);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "OnSaveSate");
        outState.putString("activeTag", active.getTag());
        outState.putParcelable("account", Account.getInstance());
        outState.putString("dynamicVariables", Singletons.gson().toJson(App.dynamicVariables.getValue()));
    }

    private void reStateData(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getString("dynamicVariables") != null) {
            App.dynamicVariables.setValue(Singletons.gson().fromJson(savedInstanceState.getString("dynamicVariables"), App.DynamicVariables.class));
        }
    }

    private void setUpFragments(Bundle savedInstanceState) {
        fragmentForYou = fm.findFragmentByTag("1");
        if (fragmentForYou == null) {
            fragmentForYou = new ForYouFragment();
            fm.beginTransaction().add(R.id.nav_host_fragment, fragmentForYou, "1").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }

        fragmentHeadlines = fm.findFragmentByTag("2");
        if (fragmentHeadlines == null) {
            fragmentHeadlines = new HeadlinesFragment();
            fm.beginTransaction().add(R.id.nav_host_fragment, fragmentHeadlines, "2").hide(fragmentHeadlines).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }

        fragmentFollowing = fm.findFragmentByTag("3");
        if (fragmentFollowing == null) {
            fragmentFollowing = new FollowingFragment();
            fm.beginTransaction().add(R.id.nav_host_fragment, fragmentFollowing, "3").hide(fragmentFollowing).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }

        fragmentMore = fm.findFragmentByTag("4");
        if (fragmentMore == null) {
            fragmentMore = new MoreFragment();
            fm.beginTransaction().add(R.id.nav_host_fragment, fragmentMore, "4")
                    .hide(fragmentMore).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }

        if (savedInstanceState != null) {
            Log.e(getClass().getSimpleName(), savedInstanceState.toString());
            active = fm.findFragmentByTag(savedInstanceState.getString("activeTag"));
            if (active == null) active = fragmentForYou;
        } else {
            active = fragmentForYou;
        }

        Controller.getInstance().detailsFragment.observe(this, aBoolean -> {
            if (aBoolean) {
                Fragment fragment = new DetailsFragment();
                final FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.details_fragment_host, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            }
        });

        Controller.getInstance().searchFragment.observe(this, aBoolean -> {
            if (aBoolean) {
                Fragment fragment = new SearchFragment();
                final FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.details_fragment_host, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            }
        });

        Controller.getInstance().sourcesFragment.observe(this, aBoolean -> {
            if (aBoolean) {
                Fragment fragment = new SourcesFragment();
                final FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.details_fragment_host, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            }
        });

        Controller.getInstance().savedNewsFragment.observe(this, aBoolean -> {
            if (aBoolean) {
                Fragment fragment = new SavedNewsFragment();
                final FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.details_fragment_host, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            }
        });
    }

    private void setUpCustomNavView(Bundle savedInstanceState) {
        final int[] lastSelectedItemId = new int[1];
        if (savedInstanceState == null) {
            lastSelectedItemId[0] = R.id.forYouCL;
            fm.beginTransaction().hide(active).show(fragmentForYou).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            active = fragmentForYou;
        } else {
            switch (Objects.requireNonNull(savedInstanceState.getString("activeTag"))) {
                case "1":
                    lastSelectedItemId[0] = R.id.forYouCL;
                    Utils.getInstance().navSelect(MainActivity.this, navCardView, 0);
                    break;
                case "2":
                    lastSelectedItemId[0] = R.id.headlinesCL;
                    Utils.getInstance().navSelect(MainActivity.this, navCardView, 1);
                    break;
                case "3":
                    lastSelectedItemId[0] = R.id.followingCL;
                    Utils.getInstance().navSelect(MainActivity.this, navCardView, 2);
                    break;
                case "4":
                default:
                    lastSelectedItemId[0] = R.id.moreCL;
                    Utils.getInstance().navSelect(MainActivity.this, navCardView, 3);
                    break;
            }
        }
        ConstraintLayout forYouCL = findViewById(R.id.forYouCL);
        ConstraintLayout headlinesCL = findViewById(R.id.headlinesCL);
        ConstraintLayout followingCL = findViewById(R.id.followingCL);
        ConstraintLayout moreCL = findViewById(R.id.moreCL);

        forYouCL.setOnClickListener(v -> {
            Utils.getInstance().navSelect(MainActivity.this, navCardView, 0);
            if (lastSelectedItemId[0] != R.id.forYouCL) {
                fm.beginTransaction().hide(active).show(fragmentForYou).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                lastSelectedItemId[0] = R.id.forYouCL;
                active = fragmentForYou;
            }
        });

        headlinesCL.setOnClickListener(v -> {
            Utils.getInstance().navSelect(MainActivity.this, navCardView, 1);
            Controller.getInstance().headlinesClicked.setValue(Controller.getInstance().headlinesClicked.getValue()+1); // to trigger the setting up of all the category recycler views
            if (lastSelectedItemId[0] != R.id.headlinesCL) {
                fm.beginTransaction().hide(active).show(fragmentHeadlines).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                lastSelectedItemId[0] = R.id.headlinesCL;
                active = fragmentHeadlines;
            }
        });

        followingCL.setOnClickListener(v -> {
            Utils.getInstance().navSelect(MainActivity.this, navCardView, 2);
            if (lastSelectedItemId[0] != R.id.followingCL) {
                fm.beginTransaction().hide(active).show(fragmentFollowing).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                lastSelectedItemId[0] = R.id.followingCL;
                active = fragmentFollowing;
            }
        });

        moreCL.setOnClickListener(v -> {
            Utils.getInstance().navSelect(MainActivity.this, navCardView, 3);
            if (lastSelectedItemId[0] != R.id.moreCL) {
                fm.beginTransaction().hide(active).show(fragmentMore).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                lastSelectedItemId[0] = R.id.moreCL;
                active = fragmentMore;
            }
        });
    }

    private void setUpMiniPlayer() {
        //subView inits
        PlayerControlView playerControlView = miniPlayerCardView.findViewById(R.id.playerView);
        TextView sourceTextView = miniPlayerCardView.findViewById(R.id.sourceTextView);
        ImageView sourceImageView = miniPlayerCardView.findViewById(R.id.sourceImageView);
        ImageView closeImageView = miniPlayerCardView.findViewById(R.id.closeImageView);
        ProgressBar audioProgressBar = miniPlayerCardView.findViewById(R.id.audioProgressBar);

        playerControlView.setProgressUpdateListener((position, bufferedPosition) -> {
            int progressBarPosition = (int) ((position * 100) / PlayerUtils.getInstance().player.getDuration());
            int bufferedProgressBarPosition = (int) ((bufferedPosition * 100) / PlayerUtils.getInstance().player.getDuration());
            audioProgressBar.setProgress(progressBarPosition);
            audioProgressBar.setSecondaryProgress(bufferedProgressBarPosition);
        });

        PlayerUtils.getInstance().isPlaying.observe(this, isPlaying -> {
            if (isPlaying) {
                miniPlayerCardView.setVisibility(VISIBLE);
                playerControlView.setPlayer(PlayerUtils.getInstance().player);
            } else miniPlayerCardView.setVisibility(GONE);
        });
        miniPlayerCardView.setOnClickListener(v -> {
            try {
                NewsRepo.getInstance().selectedNewsModel.setValue(PlayerUtils.getInstance().currentlyPlayingNewsModel.getValue());
                Controller.getInstance().detailsFragment.setValue(true);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
        PlayerUtils.getInstance().currentlyPlayingNewsModel.observe(this, newsModel -> {
            if (newsModel == null) return;
            if (newsModel.isShowSourceText()) sourceTextView.setVisibility(VISIBLE);
            else sourceImageView.setVisibility(GONE);

            Map<String, String> sourceLogos = App.dynamicVariables.getValue().sourceLogos;
            Utils.getInstance().setImageSource(this, sourceLogos.get(newsModel.getSource()), sourceImageView);
            sourceTextView.setText(newsModel.getSource());
        });

        closeImageView.setOnClickListener(v -> {
            try {
                PlayerUtils.getInstance().player.stop();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (Controller.getInstance().detailsFragment.getValue()) {
            Controller.getInstance().detailsFragment.setValue(false);
        } else if (Controller.getInstance().sourcesFragment.getValue()) {
            Controller.getInstance().sourcesFragment.setValue(false);
        } else if (Controller.getInstance().savedNewsFragment.getValue()) {
            Controller.getInstance().savedNewsFragment.setValue(false);
        } else if (Controller.getInstance().searchFragment.getValue()) {
            Controller.getInstance().searchFragment.setValue(false);
        } else {
            if ((backSelectedTime + 2000) > System.currentTimeMillis()) {
                finish();
            } else {
                backSelectedTime = System.currentTimeMillis();
                Utils.getInstance().makeToast(this, "Press back again to exit");
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "CHANGE");
        recreate(); // thia ia crucial if u r using adapters;
    }

    @Override
    protected void onStart() {
        super.onStart();

        String notificationType = getIntent().getStringExtra("notificationType");
        if (notificationType != null) {
            if (notificationType.equals("newsNotification")) {
                try {
                    String newsId = getIntent().getStringExtra("newsId");
                    NewsRepo.getInstance().selectedNewsId.setValue(newsId);
                    Controller.getInstance().detailsFragment.setValue(true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), e.getMessage());
                }
            }
        } else {
            Log.e(getClass().getSimpleName(), "Extras are null");
        }
        getIntent().removeExtra("notificationType");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String notificationType = getIntent().getStringExtra("notificationType");
        if (notificationType != null) {
            if (notificationType.equals("newsNotification")) {
                try {
                    String newsId = getIntent().getStringExtra("newsId");
                    NewsRepo.getInstance().selectedNewsId.setValue(newsId);
                    Controller.getInstance().detailsFragment.setValue(true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), e.getMessage());
                }
            }
        } else {
            Log.e(getClass().getSimpleName(), "Extras are null");
        }
        intent.removeExtra("notificationType");
    }

}
