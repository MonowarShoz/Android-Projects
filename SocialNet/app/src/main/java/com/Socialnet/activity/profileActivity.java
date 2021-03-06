package com.Socialnet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.Socialnet.Model.User;
import com.Socialnet.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import Adapter.ProfileViewPagerAdapter;
import REST.APIClient;
import REST.services.UserInterface;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class profileActivity extends AppCompatActivity {

    @BindView(R.id.profile_cover)
    ImageView profileCover;
    @BindView(R.id.profile_image)
    CircleImageView profileImage;
    @BindView(R.id.profile_option_btn)
    Button profileOptionBtn;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.ViewPager_profile)
    ViewPager ViewPagerProfile;

    ProfileViewPagerAdapter profileViewPagerAdapter;
    /*

   0 = profile is still loading
   1=  two people are friends ( unfriend )
   2 = this person has sent friend request to another friend ( cancel sent requeset )
   3 = this person has received friend request from another friend  (  reject or accept request )
   4 = people are unkown ( you can send requeset )
   5 = own profile
    */
    int current_state= 0;
    String uid = "0";
    String profileUrl="",coverUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //for hiding status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra("uid");

        ButterKnife.bind(this);

        profileViewPagerAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager(),1);

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(profileActivity.this,MainActivity.class));
            }
        });
        ViewPagerProfile.setAdapter(profileViewPagerAdapter);

        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equalsIgnoreCase(uid)){
            // UID is matched , we are going to load our own profile
            current_state = 5;
            profileOptionBtn.setText("Edit profile");
            loadProfile();
        }else{
            //load other users here
        }
    }

    private void loadProfile() {
        UserInterface userInterface = APIClient.getAPiClient().create(UserInterface.class);
        Map<String,String >params = new HashMap<>();
        params.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
        Call<User> call = userInterface.loadownProfile(params);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.body()!=null){
                    profileUrl = response.body().getProfileUrl();
                    coverUrl = response.body().getCoverUrl();
                    collapsingToolbar.setTitle(response.body().getName());

                    if(!profileUrl.isEmpty()){
                        Picasso.with(profileActivity.this).load(profileUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(profileActivity.this).load(profileUrl).into(profileImage);

                            }
                        });

                    }
                    if(!coverUrl.isEmpty()){
                        Picasso.with(profileActivity.this).load(coverUrl).networkPolicy(NetworkPolicy.OFFLINE).into(profileCover, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(profileActivity.this).load(coverUrl).into(profileCover);

                            }
                        });

                    }



                }else {
                    Toast.makeText(profileActivity.this,"Something went wrong ... Please Try later ",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(profileActivity.this,"Something went wrong ... Please Try later ",Toast.LENGTH_LONG).show();

            }
        });

    }
}