package it.telecomitalia.my.aiutami;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class QuestionActivity extends ElementsForEveryActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.question);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra("title");
        int color    = getIntent().getIntExtra("color", R.color.primario_1);
        getSupportActionBar().setTitle(title);
        toolbar.setBackgroundColor(color);
        ctl.setBackgroundColor(color);
        ctl.setContentScrimColor(color);
        ctl.setStatusBarScrimColor(color);
        this.insertDefaultFab();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            // perché ? perché con il back della toolbar non si ripristina
            // il CollapsingToolbarLayout della activity precedente. In questo
            // modo invece colori e titoli vengono correttamente visualizzati.
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


}
