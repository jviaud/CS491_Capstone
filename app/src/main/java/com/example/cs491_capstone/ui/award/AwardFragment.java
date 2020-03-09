package com.example.cs491_capstone.ui.award;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cs491_capstone.R;

import java.util.ArrayList;
import java.util.List;

public class AwardFragment extends Fragment {
    ArrayList<Award> awards;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_awards, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        generateData();

        RecyclerView recyclerView = view.findViewById(R.id.award_recycler);
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), awards);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);

    }

    public void generateData() {
        int[] icons = {R.drawable.award_streak_1hr, R.drawable.award_streak_2hr, R.drawable.award_streak_6hr, R.drawable.award_streak_12hr, R.drawable.award_streak_18hr, R.drawable.award_streak_24hr};
        String[] descriptions = {"Stay off for 1 hr", "Stay off for 2 hr", "Stay off for 6 hr", "Stay off for 12 hr", "Stay off for 18 hr", "Stay off for 24 hr"};
        String[] names = {"1hr Streak", "2hr Streak", "6hr Streak", "12hr Streak", "18hr Streak", "24hr Streak"};

        awards = new ArrayList<>();

        for (int i = 0; i < icons.length; i++) {
            awards.add(new Award(names[i], descriptions[i], icons[i], true));
        }

    }


    public static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private Context context;
        private List<Award> awards;

        public RecyclerAdapter(Context context, List<Award> awards) {
            this.context = context;
            this.awards = awards;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.award_card_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            holder.name.setText(awards.get(position).name);
            holder.icon.setImageResource(awards.get(position).getIcon());

            if (awards.get(position).lock) {
                holder.lock.setVisibility(View.VISIBLE);
            }
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "" + awards.get(position).name, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return awards.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView name;
            ImageView icon;
            CardView card;
            ImageView lock;


            ViewHolder(@NonNull View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.name);
                icon = itemView.findViewById(R.id.icon);
                card = itemView.findViewById(R.id.award_card);
                lock = itemView.findViewById(R.id.lock);

            }
        }

    }

}
