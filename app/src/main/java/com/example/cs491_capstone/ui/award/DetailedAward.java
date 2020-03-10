package com.example.cs491_capstone.ui.award;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.cs491_capstone.R;

public class DetailedAward extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.award_details_activity);


        Intent intent = getIntent();
        Award award = intent.getParcelableExtra("award");

        ImageView icon = findViewById(R.id.icon);
        ImageView close = findViewById(R.id.btn_close);
        TextView name = findViewById(R.id.name);
        TextView description = findViewById(R.id.description);
        ConstraintLayout layout = findViewById(R.id.constraint_overlay);


        name.setText(award.getName());
        description.setText(award.getDescription());
        icon.setImageResource(award.getIcon());

        layout.setOnContextClickListener(new View.OnContextClickListener() {
            @Override
            public boolean onContextClick(View v) {
                finish();
                return true;
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void clicked(View view) {
        finishAfterTransition();
    }
}
