package com.example.cs491_capstone.ui.intro;

import android.animation.Animator;
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
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.services.NotificationListener;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
import static com.example.cs491_capstone.ui.intro.IntroManager.pager;

public class NotificationPermissionFragment extends Fragment {
    /**
     * The animation view to show whether the permission has been successfully granted or failed
     */
    private LottieAnimationView animationView;
    /**
     * button the starts the permission activity
     */
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
        String permissionText = "Notification Listener";
        permissionView.setText(permissionText);

        TextView descriptionView = view.findViewById(R.id.description);
        String descriptionText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et " +
                "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea " +
                "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        descriptionView.setText(descriptionText);


        animationView = view.findViewById(R.id.animation_view);

        grantPermission_btn = view.findViewById(R.id.button);
        grantPermission_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //THIS IS SET TO TRUE SINCE THE BUTTON HAS BEEN USED ONCE
                buttonClicked = true;
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).setFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //EVERY TIME THE INTRO SCREEN RESUMES CHECK IF THE PERMISSION HAS BEEN GRANTED
        //SINCE THE PERMISSIONS SETTINGS SCREEN OPENS A NEW ACTIVITY IT IS BEST TO CHECK IN ON RESUME
        boolean notification_permission = NotificationListener.connectedNL;
        /**
         * boolean representing the notification permission
         */
        if (notification_permission & buttonClicked) {
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
        if (buttonClicked & !notification_permission) {
            //WE CHANGE THE SOURCE FILE AND START THE ANIMATION
            animationView.setAnimation(R.raw.failure);
            animationView.playAnimation();
        }
    }
}
