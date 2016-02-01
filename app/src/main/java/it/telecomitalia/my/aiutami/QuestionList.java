package it.telecomitalia.my.aiutami;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class QuestionList extends ElementsForEveryActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.insertDefaultFab();

    }

}
