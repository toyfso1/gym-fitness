package com.example.gymfitness.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gymfitness.R;
import com.example.gymfitness.data.entities.Exercise;
import com.example.gymfitness.data.entities.Workout;
import com.example.gymfitness.data.entities.WorkoutLog;
import com.example.gymfitness.databinding.FragmentExerciseDetailBinding;
import com.example.gymfitness.helpers.FavoriteHelper;
import com.example.gymfitness.helpers.ProgressTrackHelper;
import com.example.gymfitness.retrofit.AdsServices;
import com.example.gymfitness.utils.UserData;
import com.example.gymfitness.viewmodels.SharedViewModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

public class ExerciseDetailFragment extends Fragment {

    private FragmentExerciseDetailBinding binding;
    private SharedViewModel sharedViewModel;
    private String level;
    private String urlVideo;
    private ExoPlayer player;
    private Dialog progressDialog;
    private ProgressTrackHelper progressTrackHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_exercise_detail, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        AdsServices.loadFullscreenADS(getContext());
        return binding.getRoot();
    }

    private void loadData() {
        sharedViewModel.getExerciseSelected().observe(getViewLifecycleOwner(), exercise -> {
            Glide.with(binding.thumbnail.getContext())
                    .load(exercise.getExerciseThumb())
                    .placeholder(R.drawable.woman_helping_man_gym)
                    .error(R.drawable.woman_helping_man_gym)
                    .into(binding.thumbnail);
            binding.exerciseName.setText(exercise.getExercise_name());
            binding.duration.setText(exercise.getDuration() + " Seconds");
            binding.rep.setText(exercise.getRep() + " Rep");
            binding.level.setText(exercise.getLevel());
            urlVideo = exercise.getLink();
            level = exercise.getLevel();
        });
        sharedViewModel.getSelected().observe(getViewLifecycleOwner(), workout -> {
            Log.d("okeemoi",workout.getWorkout_name());
        });
    }

    private void playVideo(String url) {
        if (url != null && !url.isEmpty()) {
            player = new ExoPlayer.Builder(getContext()).build();
            player.addListener(new ExoPlayer.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == ExoPlayer.STATE_READY && progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            });
            binding.videoView.setPlayer(player);
            player.setMediaItem(MediaItem.fromUri(url));
            player.prepare();
            player.play();
        }
    }
    private void saveProgress() {
        progressTrackHelper = new ProgressTrackHelper();
        Workout selectedValue = sharedViewModel.getSelected().getValue();
        Exercise exerciseSelectedValue = sharedViewModel.getExerciseSelected().getValue();

        if (selectedValue != null && exerciseSelectedValue != null) {
            try {
                progressTrackHelper.SaveProgress(selectedValue, exerciseSelectedValue,getContext());
                Log.d("ProgressTrackHelper", "Progress saved successfully");
            } catch (Exception e) {
                Log.e("ProgressTrackHelper", "Error saving progress", e);
            }
        } else {
            Log.w("ProgressTrackHelper", "Selected value, exercise selected value, or activity is null");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.play.setOnClickListener(v -> {
            sharedViewModel.increaseCountEx();
            sharedViewModel.getCountEx().observe(getViewLifecycleOwner(), count -> {
                if(count % 3 == 0)
                {
                    Log.d("helloooooooooooooo","Quang cao di em oi");
                    AdsServices.showADSFullscreen(getContext());
                }
                Log.d("helloooooooooooooo",String.valueOf(count));
            });
            binding.cardView.setVisibility(View.GONE);
            binding.headerLayout.setBackgroundColor(Color.parseColor("#FF000000"));
            binding.videoView.setVisibility(View.VISIBLE);
            // Show custom loading dialog
            progressDialog = new Dialog(getContext());
            progressDialog.setContentView(R.layout.custom_progess_dialog);
            progressDialog.setCancelable(false); // Optional
            progressDialog.show();
            playVideo(urlVideo);

            // save progess
            saveProgress();

            // congratulation navigate
        });


        // set favorite
        Exercise exerciseFavorite = sharedViewModel.getExerciseSelected().getValue();
        binding.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteHelper.setFavorite(exerciseFavorite,v.getContext(), binding.star);
            }
        });

        FavoriteHelper.checkFavorite(exerciseFavorite, getContext(), binding.star);
        // Initialize the Mobile Ads SDK
        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.e("Test", "SDK initialized successfully");
            }
        });
        AdsServices.showBannerAds(binding.adView, getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(level);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}