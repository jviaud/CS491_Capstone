package com.example.cs491_capstone.ui.intro;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.cs491_capstone.R;
import com.novoda.spritz.Spritz;
import com.novoda.spritz.SpritzStep;
import com.pixelcan.inkpageindicator.InkPageIndicator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class IntroManager extends AppCompatActivity {
    public static LockableViewPager pager;
    private Spritz spritz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_manager);

        InkPageIndicator pageIndicator = findViewById(R.id.indicator);
        pager = findViewById(R.id.intro_pager);
        IntroPageAdapter adapter = new IntroPageAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        //pager.setAdapter(adapter);


        adapter.addFragment(new NotificationPermissionFragment(), "Notification Listener");
        adapter.addFragment(new UsagePermissionFragment(), "Usage Stats");
        adapter.addFragment(new GetStartedFragment(), "Get Started");

        pager.setSwipeable(false);
        pager.setAdapter(adapter);
        pageIndicator.setViewPager(pager);
        pager.setPageTransformer(true, new ZoomOutPageTransformer() {
        });


        LottieAnimationView animationView = findViewById(R.id.animation_view);
        spritz = Spritz
                .with(animationView)
                .withSteps(
                        new SpritzStep.Builder()
                                .withAutoPlayDuration(2550, TimeUnit.MILLISECONDS)
                                .withSwipeDuration(500, TimeUnit.MILLISECONDS)
                                .build(),
                        new SpritzStep.Builder()
                                .withAutoPlayDuration(3550, TimeUnit.MILLISECONDS)
                                .withSwipeDuration(500, TimeUnit.MILLISECONDS)
                                .build(),
                        new SpritzStep.Builder()
                                .withAutoPlayDuration(2000, TimeUnit.MILLISECONDS)
                                .build()
                )
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        spritz.attachTo(pager);
        spritz.startPendingAnimations();
    }

    @Override
    protected void onStop() {
        super.onStop();
        spritz.detachFrom(pager);
        super.onStop();
    }

    public static class IntroPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        IntroPageAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }


        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            titles.add(title);
        }
    }

    public static class LockableViewPager extends ViewPager {

        private boolean swipeable;
        private FixedSpeedScroller mScroller = null;

        public LockableViewPager(Context context) {
            super(context);
            init();
        }

        public LockableViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.swipeable = true;
            init();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (this.swipeable) {
                return super.onTouchEvent(event);
            }

            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (this.swipeable) {
                return super.onInterceptTouchEvent(event);
            }

            return false;
        }

        public void setSwipeable(boolean swipeable) {
            this.swipeable = swipeable;
        }


        /*
         * Override the Scroller instance with our own class so we can change the
         * duration
         */
        private void init() {
            try {
                Class<?> viewpager = ViewPager.class;
                Field scroller = viewpager.getDeclaredField("mScroller");
                scroller.setAccessible(true);
                mScroller = new FixedSpeedScroller(getContext(),
                        new DecelerateInterpolator());
                scroller.set(this, mScroller);
            } catch (Exception ignored) {
            }
        }

        /*
         * Set the factor by which the duration will change
         */
        public void setScrollDuration(int duration) {
            mScroller.setScrollDuration(duration);
        }

        private static class FixedSpeedScroller extends Scroller {

            private int mDuration = 500;

            public FixedSpeedScroller(Context context) {
                super(context);
            }

            FixedSpeedScroller(Context context, Interpolator interpolator) {
                super(context, interpolator);
            }

            public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
                super(context, interpolator, flywheel);
            }

            @Override
            public void startScroll(int startX, int startY, int dx, int dy, int duration) {
                // Ignore received duration, use fixed one instead
                super.startScroll(startX, startY, dx, dy, mDuration);
            }

            @Override
            public void startScroll(int startX, int startY, int dx, int dy) {
                // Ignore received duration, use fixed one instead
                super.startScroll(startX, startY, dx, dy, mDuration);
            }

            void setScrollDuration(int duration) {
                mDuration = duration;
            }
        }
    }
}
