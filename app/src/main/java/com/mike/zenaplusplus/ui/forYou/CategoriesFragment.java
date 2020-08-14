package com.mike.zenaplusplus.ui.forYou;

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

import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.adapter.CategoriesAdapter;
import com.mike.zenaplusplus.models.UserModel;
import com.mike.zenaplusplus.utils.Account;

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
        doneBtn.setOnClickListener(v->{
            UserModel  userModel = Account.getInstance().user.getValue();
            assert userModel != null;
            userModel.setSelectedCategories(categoriesAdapter.getSelectedKeys());
            Account.getInstance().updateUserFirebaseData().addOnCompleteListener(t->{
                if(t.isSuccessful()) forYouViewModel.feedRepo.mainFeedCF(false, true,false);
            });
            Account.getInstance().user.setValue(userModel);
        });
        forYouViewModel.boneBtnEnabled.observe(getViewLifecycleOwner(), doneBtn::setEnabled);
    }

    private void setUpRecyclerView() {
        categoriesAdapter = new CategoriesAdapter(getActivity());
        categoriesAdapter.setSelectedKeys(Account.getInstance().user.getValue().getSelectedCategories());
        forYouViewModel.categoriesMap.observe(getViewLifecycleOwner(), categoriesAdapter::setCategoryMap);
        categoriesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categoriesRecyclerView.setAdapter(categoriesAdapter);
    }
}
