package com.chaosapps.zena.ui.forYou;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaosapps.zena.R;
import com.chaosapps.zena.adapter.CategoriesAdapter;
import com.chaosapps.zena.models.UserModel;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.Account;
import com.chaosapps.zena.utils.Controller;
import com.chaosapps.zena.utils.Utils;
import com.paginate.Paginate;

public class CategoriesFragment extends Fragment {

    //Objects
    private ForYouViewModel forYouViewModel;
    private Button doneBtn;
    public CategoriesAdapter categoriesAdapter;

    //Views
    private RecyclerView categoriesRecyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        forYouViewModel = ViewModelProviders.of(requireActivity()).get(ForYouViewModel.class);
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allFindViewByIds(view);
        setUpRecyclerView();
        setUpViews();
    }

    private void allFindViewByIds(View view) {
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        doneBtn = view.findViewById(R.id.doneBtn);
    }

    private void setUpViews(){
        try{
        doneBtn.setOnClickListener(v->{
            Controller.getInstance().showCategories.setValue(false);
            UserModel  userModel = Account.getInstance().user.getValue();
            assert userModel != null;
            userModel.setSelectedCategories(categoriesAdapter.getSelectedKeys());
            Account.getInstance().user.setValue(userModel);
            Account.getInstance().updateUserFirebaseData().addOnCompleteListener(t->{
                if(t.isSuccessful()) NewsRepo.getInstance().fetchNewsForMainFeed(getContext(),false);
            });
        });
        forYouViewModel.boneBtnEnabled.observe(getViewLifecycleOwner(), doneBtn::setEnabled);
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    private void setUpRecyclerView() {
        try{
        categoriesAdapter = new CategoriesAdapter(getActivity());
        categoriesAdapter.setSelectedKeys(Account.getInstance().user.getValue().getSelectedCategories());
        forYouViewModel.categoriesMap.observe(getViewLifecycleOwner(), categoriesAdapter::setCategoryMap);
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categoriesRecyclerView.setNestedScrollingEnabled(true);
        categoriesRecyclerView.setAdapter(categoriesAdapter);

        Paginate.with(categoriesRecyclerView, callbacks)
                .setLoadingTriggerThreshold(0)
                .addLoadingListItem(true)
                .build();
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    private Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {
        }

        @Override
        public boolean isLoading() {
            // Indicate whether new page loading is in progress or
            return NewsRepo.getInstance().loadingMainFeed.getValue();
        }

        @Override
        public boolean hasLoadedAllItems() {
            return true;
        }
    };
}
