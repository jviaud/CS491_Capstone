package com.example.cs491_capstone.ui.goal.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs491_capstone.App;
import com.example.cs491_capstone.GoalDataBaseHelper;
import com.example.cs491_capstone.R;
import com.example.cs491_capstone.ui.goal.Goal;
import com.example.cs491_capstone.ui.goal.activities.EditGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoalImageAdapter extends RecyclerView.Adapter<GoalImageAdapter.ImageViewHolder> {
    public List<ImageViewHolder> holderList;
    private Context context;
    private List<Goal> goals;
    private PackageManager packageManager;


    public GoalImageAdapter(Context context, List<Goal> goals) {
        this.context = context;
        this.goals = goals;
        packageManager = context.getPackageManager();
        holderList = new ArrayList<>();


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

    public void collapseAll() {
        //expandableLayout
        // collapse(holder.expandableLayout);
        for (ImageViewHolder holder : holderList) {
            collapse(holder.expandableLayout);
        }

    }

    @NonNull
    @Override
    public GoalImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.goal_card_layout, parent, false);

        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {
        holderList.add(holder);
        final Goal goal = goals.get(position);
        holder.id.setText(goal.getId());
        holder.date.setText(App.dateFormater(goal.getDate(), "EEEE, MMMM dd, yyyy"));

        if (goal.getType().equals(GoalDataBaseHelper.GOAL_APP)) {
            String packageName = goal.getPackageName();
            try {
                ApplicationInfo ai = packageManager.getApplicationInfo(packageName, 0);
                holder.appName.setText(ai.loadLabel(packageManager).toString());
                holder.icon.setImageDrawable(ai.loadIcon(packageManager));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            String name = "Phone Goal";

            holder.appName.setText(name);
            holder.icon.setImageResource(R.drawable.ic_goal_default);
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

        boolean passed = goal.isPassed();
        holder.topPanel.setBackgroundResource(passed ? R.color.passed : R.color.failed);
        holder.status.setText(passed ? "PASSED" : "FAILED");

        holder.usageStatus.setImageResource(goal.isUsagePassed() ? R.drawable.ic_tick : R.drawable.ic_close);

        holder.unlockStatus.setImageResource(goal.isUnlockPassed() ? R.drawable.ic_tick : R.drawable.ic_close);


        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(context, "MENU CLICK", Toast.LENGTH_SHORT).show();
                PopupMenu popupMenu = new PopupMenu(context, holder.menu);
                popupMenu.inflate(R.menu.card_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.edit_card:
                                //Toast.makeText(context, "DELETE", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, EditGoal.class);
                                intent.putExtra("GOAL", goal);
                                context.startActivity(intent);
                                break;
                            case R.id.delete_card:
                                //Toast.makeText(context, "EDIT", Toast.LENGTH_SHORT).show();
                                goals.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, getItemCount());
                                //TODO REMOVE FROM DATABASE SHOW UNDO LOGIC
                                App.goalDataBase.remove(goal.getId());
                                break;
                            case R.id.show_all_card:
                                //Toast.makeText(context, "SHOW ALL", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView id, date, appName, usage, unlocks, status;
        ImageView icon, usageStatus, unlockStatus;
        CardView card;
        ConstraintLayout expandableLayout, topPanel;
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
            topPanel = itemView.findViewById(R.id.topPanel);
            status = itemView.findViewById(R.id.status);
            usageStatus = itemView.findViewById(R.id.usage_status);
            unlockStatus = itemView.findViewById(R.id.unlock_status);

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
