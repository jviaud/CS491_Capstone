package com.example.cs491_capstone.ui.goal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs491_capstone.GoalDataBaseHelper;
import com.example.cs491_capstone.R;

import java.util.List;
import java.util.Locale;

public class GoalImageAdapter extends RecyclerView.Adapter<GoalImageAdapter.ImageViewHolder> {
    private Context context;
    private List<Goal> goals;
    private PackageManager packageManager;


    GoalImageAdapter(Context context, List<Goal> goals) {
        this.context = context;
        this.goals = goals;
        packageManager = context.getPackageManager();
    }

    private static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 4);
        v.startAnimation(a);
    }

    private static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density) * 4);
        v.startAnimation(a);
    }

    @NonNull
    @Override
    public GoalImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.goal_card_layout, parent, false);

        return new GoalImageAdapter.ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        final Goal goal = goals.get(position);
        holder.id.setText(goal.getId());
        holder.date.setText(goal.getDate());

        if (goal.getType().equals(GoalDataBaseHelper.GOAL_APP)) {
            String packageName = goal.getPackageName();
            try {
                ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
                holder.appName.setText(ai.loadLabel(packageManager).toString());
                holder.icon.setImageDrawable(ai.loadIcon(packageManager));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }


        long usageValue = goal.getUsage() / 60000;
        String formattedVal = "0";
        if (usageValue < 0) {
            holder.unlocks.setText(formattedVal);
        } else {
            int hours = (int) (usageValue / (60) % 24);
            int minutes = (int) (usageValue % 60);

            if (hours == 0) {
                formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
            } else {
                formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
            }
            holder.usage.setText(formattedVal);
        }

       holder.unlocks.setText(String.valueOf(goal.getUnlocks()));


        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "MENU CLICK", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView id;
        TextView date;
        TextView appName;
        TextView usage;
        TextView unlocks;
        ImageView icon;
        CardView card;
        ConstraintLayout expandableLayout;
        ImageView menu;


        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.id);
            icon = itemView.findViewById(R.id.icon);
            date = itemView.findViewById(R.id.date);
            appName = itemView.findViewById(R.id.appName);
            usage = itemView.findViewById(R.id.usage_value);
            unlocks = itemView.findViewById(R.id.unlocks_value);
            card = itemView.findViewById(R.id.card);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            menu = itemView.findViewById(R.id.menu);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Goal goal = goals.get(getAdapterPosition());
                    goal.setExpanded(!goal.isExpanded());

                    if (goal.isExpanded()) {
                        expand(expandableLayout);
                    } else {
                        collapse(expandableLayout);
                    }
                    //notifyItemChanged(getAdapterPosition());
                }
            });

        }
    }
}
