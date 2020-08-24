package com.chaosapps.zena.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaosapps.zena.R;
import com.chaosapps.zena.adapter.AutoSuggestAdapter;
import com.chaosapps.zena.adapter.NewsAdapter;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.CacheUtils;
import com.chaosapps.zena.utils.Controller;
import com.chaosapps.zena.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.View.GONE;

public class SearchFragment extends Fragment {
    final static private String TAG = "SearchFragment";


    //View Declarations
    private ListView searchSuggestionsRV;
    private RecyclerView resultsRV;
    private ProgressBar autoSuggestionProgressBar;
    private ProgressBar loadingSearchResult;
    private CardView clearCardView;
    private AutoCompleteTextView autoCompleteTextView;
    private ConstraintLayout errorStateCL;
    private ImageView actionBarIV;

    //Object Declarations
    private SearchViewModel searchViewModel;
    private AutoSuggestAdapter autoSuggestAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel =
                ViewModelProviders.of(this).get(SearchViewModel.class);
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allFindViewByIds(view);
        setUpViews();
        setUpAutoCompleteTextView(view);
        setUpRecyclerView(savedInstanceState);
        linkWithController();
        setUpLastSearchResults();
//        setUpLangs();
    }

    private void allFindViewByIds(View view) {
        searchSuggestionsRV = view.findViewById(R.id.searchSuggestionsRV);
        resultsRV = view.findViewById(R.id.resultsRV);
        autoSuggestionProgressBar = view.findViewById(R.id.autoSuggestionProgressBar);
        loadingSearchResult = view.findViewById(R.id.loadingSearchResult);
        clearCardView = view.findViewById(R.id.clearCardView);
        errorStateCL = view.findViewById(R.id.errorStateCL);
        actionBarIV = view.findViewById(R.id.actionBarIV);
    }

    private void setUpViews() {
        actionBarIV.setOnClickListener(v -> Controller.getInstance().searchFragment
                .setValue(false));
        NewsRepo.getInstance().loadingSuggestions.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                autoSuggestionProgressBar.setVisibility(View.VISIBLE);
            } else {
                autoSuggestionProgressBar.setVisibility(GONE);
            }
        });

        NewsRepo.getInstance().loadingSearchResult.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                loadingSearchResult.setVisibility(View.VISIBLE);
                searchSuggestionsRV.setVisibility(GONE);
                resultsRV.setVisibility(GONE);
            } else {
                loadingSearchResult.setVisibility(GONE);
                resultsRV.setVisibility(View.VISIBLE);
            }
        });

        NewsRepo.getInstance().searchResults.observe(getViewLifecycleOwner(), productModels -> {
            if (productModels.isEmpty()) {
                clearCardView.setVisibility(GONE);
                searchSuggestionsRV.setVisibility(View.VISIBLE);
                if(searchViewModel.searchedOrCleared) errorStateCL.setVisibility(View.VISIBLE);
                else errorStateCL.setVisibility(View.GONE);
            } else {
                clearCardView.setVisibility(View.VISIBLE);
                searchSuggestionsRV.setVisibility(GONE);
                errorStateCL.setVisibility(View.GONE);
            }
        });

        clearCardView.setOnClickListener(v -> {
            searchViewModel.searchedOrCleared = false;
            NewsRepo.getInstance().searchResults.setValue(new ArrayList<>());
            NewsRepo.getInstance().searchSuggestions.setValue(new ArrayList<>());
            NewsRepo.getInstance().loadingSuggestions.setValue(false);
            NewsRepo.getInstance().loadingSearchResult.setValue(false);
            autoCompleteTextView.setText("");
        });

        //Link gotoSearch controller to show keyboard and focus on autoCompleteTextView
        Controller.getInstance().searchFragment.observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) {
                autoCompleteTextView.requestFocus();
                autoCompleteTextView.setPressed(true);
                autoCompleteTextView.setCursorVisible(true);
                Utils.getInstance().showKeyboardFrom(getContext());
            }
        });

    }

    private void linkWithController() {
        Controller.getInstance().searchFragment.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                Utils.getInstance().hideKeyboardFrom(getContext(), autoCompleteTextView);
                getParentFragmentManager().beginTransaction().remove(SearchFragment.this).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            }
        });
    }
    private void setUpAutoCompleteTextView(View view) {

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextViewSearch);


        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(getContext(), android.R.layout.simple_dropdown_item_1line);
        NewsRepo.getInstance().searchSuggestions.observe(getViewLifecycleOwner(), strings -> {
            autoSuggestAdapter.setData(strings);
            autoSuggestAdapter.notifyDataSetChanged();
        });

        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);
        autoCompleteTextView.setOnItemClickListener((parent, view1, position, id) -> {
            Utils.getInstance().hideKeyboardFrom(getContext(), autoCompleteTextView);
            NewsRepo.getInstance().searchNews(NewsRepo.getInstance().searchSuggestions.getValue().get(position));
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 2) NewsRepo.getInstance().createSuggestions(s.toString());
            }
        });

        autoCompleteTextView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KEYCODE_ENTER) {
                Utils.getInstance().hideKeyboardFrom(getContext(), autoCompleteTextView);
                NewsRepo.getInstance().searchNews(autoCompleteTextView.getText().toString());
                autoCompleteTextView.dismissDropDown();
                searchViewModel.searchedOrCleared = true;
            }
            Log.e(TAG, event.getCharacters() + event.toString() + keyCode);
            return false;
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("position",((LinearLayoutManager) resultsRV.getLayoutManager()).findLastVisibleItemPosition()); // get current recycle view position here.
        super.onSaveInstanceState(savedInstanceState);
    }


    private void setUpRecyclerView(Bundle savedInstanceState){
        resultsRV.setLayoutManager(new LinearLayoutManager(getContext()));
        final NewsAdapter newsAdapter = new NewsAdapter(requireActivity().getApplication());
        NewsRepo.getInstance().searchResults.observe(getViewLifecycleOwner(), newsAdapter::setNewsList);
        resultsRV.setAdapter(newsAdapter);
        if(savedInstanceState != null){
            resultsRV.scrollToPosition(savedInstanceState.getInt("position"));
        }
    }

    private void setUpLastSearchResults() {
        try {
            final ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            CacheUtils.getInstance().savedSearchTerms.observe(getViewLifecycleOwner(), strings -> {
                Collections.reverse(strings);
                itemsAdapter.clear();
                if(strings.size()>5) itemsAdapter.addAll(strings.subList(0, 5));
                else itemsAdapter.addAll(strings);
                itemsAdapter.notifyDataSetChanged();
            });

            searchSuggestionsRV.setAdapter(itemsAdapter);
            searchSuggestionsRV.setOnItemClickListener((parent, view, position, id) -> NewsRepo.getInstance().searchNews(itemsAdapter.getItem(position)));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

}
