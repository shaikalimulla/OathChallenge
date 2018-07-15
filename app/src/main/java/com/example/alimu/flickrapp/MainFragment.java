package com.example.alimu.flickrapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;
import com.example.alimu.flickrapp.config.DaggerServiceComponent;
import com.example.alimu.flickrapp.config.ServiceModule;
import com.example.alimu.flickrapp.util.UtilityClass;

import java.util.List;

import javax.inject.Inject;

import static com.example.alimu.flickrapp.util.UtilityClass.clearAlertDialog;
import static com.example.alimu.flickrapp.util.UtilityClass.createAlertDialog;

public class MainFragment extends Fragment implements MainContract.View, RecyclerAdapter.ItemClickListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerAdapter mAdapterRecycler;
    private Context context;
    private LottieAnimationView animationView;

    private String LOG_TAG = MainFragment.class.getSimpleName();

    @Inject
    MainContract.Presenter mainPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "MainFragment onCreate");
        DaggerServiceComponent.builder().getServiceModule(new ServiceModule()).build().inject(this);
        context = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        Log.i(LOG_TAG, "MainFragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_main, viewGroup, false);

        return view;
    }

    protected void initRecyclerView(View view){
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, UtilityClass.sharedVariables.RECYCLER_VIEW_COLUMNS_COUNT));
        mAdapterRecycler = new RecyclerAdapter(context);
        mAdapterRecycler.setClickListener(this);
        mRecyclerView.setAdapter(mAdapterRecycler);
    }

    @Override
    public void onItemClicked(View view, int position) {
        String imageUrl = mAdapterRecycler.getItem(position);
        Log.i(LOG_TAG, "You clicked " + imageUrl + ", which is at cell position " + position);
        Intent intent = new Intent(context, ImageDisplayActivity.class);
        intent.putExtra(ImageDisplayActivity.EXTRA_FULL_IMAGE_URL, imageUrl);
        context.startActivity(intent);
    }

    @Override
    public void onButtonClicked(View view, int pageNumber){
        Log.i(LOG_TAG, "onButtonClicked started");
        checkForConnection(String.valueOf(pageNumber));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        Log.i(LOG_TAG, "MainFragment onViewCreated");

        initRecyclerView(view);

        mainPresenter.attachView(this);
        mainPresenter.setView(this);
        mainPresenter.setContext(context);

        createAnimation(view);
    }

    public void createAnimation(View view){
        animationView = view.findViewById(R.id.animation_view);
        animationView.setAnimation(R.raw.preloader);
    }

    @Override
    public void onResume() {
        super.onResume();

        String defaultPageNumber = context.getString(R.string.start_page_name);
        checkForConnection(defaultPageNumber);
        MainPresenter.pageNumber = Integer.parseInt(defaultPageNumber);
    }

    public void checkForConnection(String pageNumber) {
        if (UtilityClass.checkNetworkConnectivity(context)) {
            Log.i(LOG_TAG, "Internet Connection Available");
            prepareAPICall(pageNumber);
        }
        else{
            Log.i(LOG_TAG, "MainFragment offline");
            createAlertDialog(context);
        }
    }

    protected void prepareAPICall(String pageNumber){
        Log.i(LOG_TAG, "prepareAPICall");

        mAdapterRecycler.clearAdapterData();
        mAdapterRecycler.notifyDataSetChanged();

        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();

        mainPresenter.requestAPICall(pageNumber);
    }

    @Override
    public void stopLoadingAnimation() {
        animationView.cancelAnimation();
        animationView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void updateRecyclerView(List<String> imageUrlList) {
        Log.i(LOG_TAG, "MainFragment updateRecyclerView");

        stopLoadingAnimation();

        mAdapterRecycler.setAdapterData(imageUrlList);
        mAdapterRecycler.notifyDataSetChanged();
    }

    @Override
    public void onPause(){
        super.onPause();
        clearResources();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mainPresenter.detachView();

        clearResources();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void clearResources(){
        Log.i(LOG_TAG, "MainFragment clearResources");

        clearAlertDialog();
    }
}
