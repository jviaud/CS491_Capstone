package com.example.cs491_capstone.ui.intro;

import android.animation.Animator;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.example.cs491_capstone.App;
import com.example.cs491_capstone.R;

import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
import static com.example.cs491_capstone.ui.intro.IntroManager.pager;

public class UsagePermissionFragment extends Fragment {

    private LottieAnimationView animationView;
    private Button grantPermission_btn;
    private boolean buttonClicked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.intro_slide_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView permissionView = view.findViewById(R.id.permission_title);
        String permissionText = "Usage Stats";
        permissionView.setText(permissionText);

        TextView descriptionView = view.findViewById(R.id.description);
        String descriptionText = "Usage Stats Manager allows our app to poll the system for apps that are being used." +
                "When polled our app will be able to see what apps were used in a specific time frame. " +
                "No other information is collected ";
        descriptionView.setText(descriptionText);

        animationView = view.findViewById(R.id.animation_view);


        grantPermission_btn = view.findViewById(R.id.button);
        grantPermission_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //THIS IS SET TO TRUE SINCE THE BUTTON HAS BEEN USED ONCE
                buttonClicked = true;
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).setFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //THE MAP IS POPULATED USING A VERY SMALL TIME FRAME BECAUSE WE DON'T ACTUALLY NEED THE CONTENTS OF THIS MAP
        //IT IS ONLY FOR TESTING THE USAGE_TIME PERMISSION
        UsageStatsManager usm = (UsageStatsManager) getContext().getSystemService(Context.USAGE_STATS_SERVICE);
        /*
          THIS USAGE_TIME MAP IS SOLELY TO TEST FOR THE USAGE_TIME PERMISSION
          IF THE PERMISSION ISN'T GRANTED THEN THIS MAP WILL BE NULL OTHERWISE IT WILL CONTAIN USAGE_TIME STATISTICS
         */
        Map<String, UsageStats> quick_usage = usm.queryAndAggregateUsageStats(System.currentTimeMillis() - 100000, System.currentTimeMillis());

        //App.PACKAGE_NAME REFERS TO THIS APP SO THIS IS ESSENTIALLY A SELF CHECK
        //App.PACKAGE_NAME

        boolean usage_permission = quick_usage.get(App.PACKAGE_NAME) != null;
        ///END PERMISSION CHECK AND PAGE CHANGE

        if (usage_permission & buttonClicked) {
            //GREY OUT THE BUTTON SO IT DOESN'T GET PRESSED AGAIN
            grantPermission_btn.setEnabled(false);

            //EVEN THOUGH THE ANIMATION VIEW POINTS TO THIS FILE BY DEFAULT, IF THE USER FAILS THE FIRST TIME THE ANIMATION VIEW WILL POINT TO THE WRONG ANIMATION
            //SOO WE SET IT BACK THE THE SUCCESS ANIMATION THEN PLAY IT
            animationView.setAnimation(R.raw.success);
            animationView.playAnimation();

            //WE ADD AN ANIMATION LISTENER SOO WE CAN CHANGE THE PAGE WHEN THE ANIMATION IS DONE
            animationView.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    //CHANGE TO THE NEXT PAGE
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        //THIS ON RESUME METHOD IS CALLED WHEN WE FIRST ENTER THE FRAGMENT SO A SIMPLE ELSE OR NEGATION WILL NOT WORK HERE
        //WE MUST FIRST CHECK TO SEE IF THE BUTTON HAS BEEN PRESSED AT LEAST ONCE, WE DO THIS BY CHECKING THE VALUE OF buttonClicked, WHICH WILL ONLY BE TRUE IF THE BUTTON HAS BEEN PRESSED
        if (buttonClicked & !usage_permission) {
            //WE CHANGE THE SOURCE FILE AND START THE ANIMATION
            animationView.setAnimation(R.raw.failure);
            animationView.playAnimation();
        }
    }
}
