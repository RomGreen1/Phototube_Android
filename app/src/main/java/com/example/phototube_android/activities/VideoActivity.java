
package com.example.phototube_android.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phototube_android.API.CommentOffApi;
import com.example.phototube_android.R;
import com.example.phototube_android.activities.EditVideoActivity;
import com.example.phototube_android.activities.FullscreenActivity;
import com.example.phototube_android.entities.UserManager;
import com.example.phototube_android.classes.Comment;
import com.example.phototube_android.classes.User;
import com.example.phototube_android.classes.Video;
import com.example.phototube_android.requests.LikeActionRequest;
import com.example.phototube_android.ui.adapters.CommentsAdapter;
import com.example.phototube_android.viewmodels.CommentInViewModel;
import com.example.phototube_android.viewmodels.CommentOffViewModel;
import com.example.phototube_android.viewmodels.VideoInViewModel;

import java.util.List;

public class VideoActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_EDIT_VIDEO = 1;
    private VideoView videoView;
    private TextView videoNameTextView, authorTextView, viewsTextView, timeAgoTextView;
    private EditText commentEditText;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private Video video;
    private ImageButton likeButton, dislikeButton;
    private TextView likeCountTextView;
    private ImageButton submitCommentButton;
    private static final int REQUEST_FULLSCREEN = 1;  // Request code for starting FullscreenActivity
    private boolean wasPlaying;
    private ImageButton fullscreenButton,shareButton,editButton;

    private String videoId;
    private VideoInViewModel videoInViewModel;

    private CommentInViewModel commentInViewModel;

    private CommentOffViewModel commentOffViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        Initialize();
        clickers();
    }

    private void Initialize()
    {
        videoView = findViewById(R.id.video_view);
        videoNameTextView = findViewById(R.id.videoNameTextView);
        authorTextView = findViewById(R.id.authorTextView);
        viewsTextView = findViewById(R.id.viewsTextView);
        timeAgoTextView = findViewById(R.id.timeAgoTextView);
        commentEditText  = findViewById(R.id.commentEditText);
        submitCommentButton = findViewById(R.id.sumbit_Comment_Button);
        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoId");


        videoNameTextView.setText(intent.getStringExtra("Title"));
        authorTextView.setText(intent.getStringExtra("createdBy"));
        viewsTextView.setText(intent.getStringExtra("videoViews"));
        timeAgoTextView.setText(intent.getStringExtra("videoDate"));
        Uri uri = Uri.parse("http://10.0.2.2:1324"+intent.getStringExtra("VideoUrl"));
        // Set up the VideoView to play the video
        videoView.setVideoURI(uri);
        videoView.start(); // Start playing automatically
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);


        videoView.setMediaController(mediaController);
         fullscreenButton = findViewById(R.id.fullscreenButton);
         shareButton = findViewById(R.id.shareButton);
         editButton = findViewById(R.id.edit_Video_Button);


        // Handle likes
        likeButton = findViewById(R.id.likeButton);
        dislikeButton = findViewById(R.id.dislikeButton);
        likeCountTextView = findViewById(R.id.likeCountTextView);



        List<Video.Like> videoLikes = (List<Video.Like>) intent.getSerializableExtra("videoLikes");
        updateLikes(videoLikes);


        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentOffViewModel = new ViewModelProvider(this).get(CommentOffViewModel.class);
        getComments();

        videoInViewModel = new ViewModelProvider(this).get(VideoInViewModel.class);
        commentInViewModel = new ViewModelProvider(this).get(CommentInViewModel.class);
    }

    private void getComments()
    {
        commentOffViewModel.getComments(videoId);
        commentOffViewModel.getCommentData().observe(this, commentResponse -> {
            if (commentResponse.isSuccess() && commentResponse.getData() != null) {
                commentsAdapter = new CommentsAdapter(this, commentResponse.getData(),commentInViewModel);
                commentsRecyclerView.setAdapter(commentsAdapter); // Handle comments
            } else {
                Toast.makeText(this, "Failed to load user data: " + commentResponse.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        showCommentsIfLogin();
    }

    private void updateLikes(List<Video.Like> videoLikes)
    {
        int likeCount = 0;
        boolean userLiked = false;
        boolean userDisliked = false;

        for (Video.Like like : videoLikes) {
            if ("like".equals(like.getAction())) {
                likeCount++;
                if (like.getUserId().equals(UserManager.getInstance().getUserId())) {
                    userLiked = true;
                }
            }
            else{
                if ("dislike".equals(like.getAction())) {
                    if (like.getUserId().equals(UserManager.getInstance().getUserId())) {
                        userDisliked = true;
                    }
                }
            }
        }
        likeCountTextView.setText(String.valueOf(likeCount));

        likeButton.setImageResource(userLiked ? R.drawable.thumb_up_filled : R.drawable.thumb_up_40px);
        dislikeButton.setImageResource(userDisliked ? R.drawable.thumb_down_filled : R.drawable.thumb_down_40px);
    }

    private void clickers(){
        // Fullscreen

        fullscreenButton.setOnClickListener(v -> {
            if (video.getVideoUrl() != null && video != null) {
                Intent intent = new Intent(VideoActivity.this, FullscreenActivity.class);
                intent.putExtra("videoPath", video.getVideoUrl());

                startActivityForResult(intent, REQUEST_FULLSCREEN); // Start FullscreenActivity with the request code
            } else {
                // Handle null or invalid data case
                Toast.makeText(VideoActivity.this, "Video data is not available.", Toast.LENGTH_SHORT).show();
            }
        });


        // share button

        shareButton.setOnClickListener(v -> {
            if (UserManager.getInstance().isLoggedIn()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = "Choose share option";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }
            Toast.makeText(VideoActivity.this, "You have to be logged in to share", Toast.LENGTH_SHORT).show();

        });

        //edit button

        editButton.setOnClickListener(view -> {

                Intent intent = new Intent(VideoActivity.this, EditVideoActivity.class);
                intent.putExtra("videoId", videoId); // Assuming videoId is the ID of the current video
                intent.putExtra("Title", videoNameTextView.getText()); // Assuming videoId is the ID of the current video
                intent.putExtra("VideoUrl", "http://10.0.2.2:1324"+getIntent().getStringExtra("VideoUrl")); // Assuming videoId is the ID of the current video
                startActivity(intent);

        });

        submitCommentButton.setOnClickListener(v -> {
            addComment();
            closeKeyboard();
        });

        likeButton.setOnClickListener(v -> handleLike());
        dislikeButton.setOnClickListener(v -> handleDislike());
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
            wasPlaying = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasPlaying) {
            videoView.start();
            wasPlaying = false;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_VIDEO && resultCode == RESULT_OK) {
//            updateVideoUI(); // This method resumes playback where it was paused
        }
        if (requestCode == REQUEST_FULLSCREEN && resultCode == RESULT_OK && data != null) {
            int position = data.getIntExtra("currentPosition", 0); // Retrieve the current position of the video
            if (videoView != null) {
                videoView.seekTo(position);
                videoView.start();
            }
        }
    }



    private void handleLike() {
        if (UserManager.getInstance().isLoggedIn()) {
            LikeActionRequest LAR = new LikeActionRequest("like");
            videoInViewModel.likeAction(videoId, LAR);
            videoInViewModel.getLikeActionLiveData().observe(this, likeResponse -> {
                if (likeResponse.isSuccess() && likeResponse.getData() != null) {

                   updateLikes(likeResponse.getData().getLikes());
                } else {
                    Toast.makeText(this, "Failed to load user data: " + likeResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            Toast.makeText((this), "You must login for like", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDislike() {
        if (UserManager.getInstance().isLoggedIn()) {
            LikeActionRequest LAR = new LikeActionRequest("dislike");
            videoInViewModel.likeAction(videoId, LAR);
            videoInViewModel.getLikeActionLiveData().observe(this, likeResponse -> {
                if (likeResponse.isSuccess() && likeResponse.getData() != null) {

                    updateLikes(likeResponse.getData().getLikes());
                } else {
                    Toast.makeText(this, "Failed to load user data: " + likeResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            Toast.makeText((this), "You must login for dislike", Toast.LENGTH_SHORT).show();
        }
    }





    private void addComment()
    {
        String commentText = commentEditText.getText().toString();
        if (!commentText.isEmpty()) {
            commentInViewModel.addComment(videoId,commentText);
            commentInViewModel.getAddCommentData().observe(this, commentResponse -> {
                if (commentResponse.isSuccess() && commentResponse.getData() != null) {
                   getComments();
                    // Clear the comment input field
                    commentEditText.setText("");
                    new Handler().postDelayed(() -> {
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                    }, 200); // Delay of 200 milliseconds
                } else {
                    Toast.makeText(this, "Failed to load user data: " + commentResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        }
        else{
            Toast.makeText((this), "You must write to comment", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCommentsIfLogin()
    {
        // if user logged in , show comment button
        if (UserManager.getInstance().isLoggedIn()) {
            commentEditText.setVisibility(View.VISIBLE);
            submitCommentButton.setVisibility(View.VISIBLE);
        } else {
            commentEditText.setVisibility(View.GONE);
            submitCommentButton.setVisibility(View.GONE);
        }
    }



}

