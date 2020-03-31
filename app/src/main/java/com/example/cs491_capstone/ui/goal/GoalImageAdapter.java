package com.example.cs491_capstone.ui.goal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs491_capstone.GoalDataBaseHelper;
import com.example.cs491_capstone.R;

import java.util.List;
import java.util.Locale;

public class GoalImageAdapter extends RecyclerView.Adapter<GoalImageAdapter.ImageViewHolder> {
    private GoalImageAdapter.OnItemClickListener mListener;//CUSTOM ON CLICK LISTENER
    private Context context;
    private List<Goal> goals;
    private PackageManager packageManager;


    public GoalImageAdapter(Context context, List<Goal> goals) {
        this.context = context;
        this.goals = goals;
        packageManager = context.getPackageManager();
    }


    @NonNull
    @Override
    public GoalImageAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.goal_card_layout, parent, false);

        return new GoalImageAdapter.ImageViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Goal goal = goals.get(position);
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

        } else {
            holder.appName.setVisibility(View.GONE);
            holder.icon.setVisibility(View.GONE);
        }
        long value = goal.getUnlocks() / 60000;
        String formattedVal = "0";
        if (value < 0) {
            holder.unlocks.setText(formattedVal);
        } else {
            int hours = (int) (value / (60) % 24);
            int minutes = (int) (value % 60);

            if (hours == 0) {
                formattedVal = String.format(Locale.ENGLISH, "%d%s", minutes, "m");
            } else {
                formattedVal = String.format(Locale.ENGLISH, "%d%s%d%s", hours, "h", minutes, "m");
            }
            holder.usage.setText(formattedVal);
        }
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }


    void setOnItemClickListener(GoalImageAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    //CUSTOM ONCLICK LISTENER INTERFACE
    public interface OnItemClickListener {
        void onItemClick(int position, CardView v);
    }
    ////END CUSTOM ONCLICK LISTENER INTERFACE

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView id;
        TextView date;
        TextView appName;
        TextView usage;
        TextView unlocks;
        ImageView icon;
        CardView card;


        public ImageViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            id = itemView.findViewById(R.id.id);
            icon = itemView.findViewById(R.id.icon);
            date = itemView.findViewById(R.id.date);
            appName = itemView.findViewById(R.id.appName);
            usage = itemView.findViewById(R.id.usage);
            unlocks = itemView.findViewById(R.id.unlocks);
            card = itemView.findViewById(R.id.card);


            //INITIALIZE ON CLICK LISTENER IN VIEW HOLDER
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position, card);

                    }
                }
            });
        }
    }
}
