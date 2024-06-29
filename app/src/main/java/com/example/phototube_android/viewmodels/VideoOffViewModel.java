package com.example.phototube_android.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phototube_android.classes.Video;
import com.example.phototube_android.repository.VideoRepository;
import com.example.phototube_android.response.ApiResponse;
import com.example.phototube_android.response.TokenResponse;

import java.util.List;

public class VideoOffViewModel extends ViewModel {
    private VideoRepository videoRepository;

    private MutableLiveData<ApiResponse<List<Video>>> VideoData;
    private MutableLiveData<ApiResponse<Video>> singleVideoData;
    private MutableLiveData<ApiResponse<List<Video>>> userVideosData;


    private MutableLiveData<ApiResponse<TokenResponse>> tokenData;
    public VideoOffViewModel() {
        this.videoRepository = new VideoRepository();
        this.singleVideoData = new MutableLiveData<>();
        this.userVideosData = new MutableLiveData<>();
        this.VideoData = new MutableLiveData<>();
        this.tokenData = new MutableLiveData<>();
    }

    public VideoRepository getVideoRepository() {
        return videoRepository;
    }

    public MutableLiveData<ApiResponse<Video>> getSingleVideoData() {
        return singleVideoData;
    }

    public MutableLiveData<ApiResponse<List<Video>>> getUserVideosData() {
        return userVideosData;
    }

    public MutableLiveData<ApiResponse<TokenResponse>> getTokenData() {
        return tokenData;
    }

    public MutableLiveData<ApiResponse<List<Video>>> getVideoData()
    {
        return this.VideoData;
    }

    public void getVideos()
    {
        videoRepository.getVideos(VideoData);
    }

    public void getVideo(String userId, String videoId)
    {
        videoRepository.getVideo(userId, videoId, singleVideoData);
    }

    public void getUserVideos(String userId) {
        videoRepository.getUserVideos(userId, userVideosData);
    }

}
