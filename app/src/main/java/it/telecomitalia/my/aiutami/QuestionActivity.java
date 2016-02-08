/*******************************************************************************
 * This software is distributed under the following BSD license:
 *
 * Copyright (c) 2016, Marco Paoletti <mpao@me.com>, http://mpao.github.io
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package it.telecomitalia.my.aiutami;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Classe che rappresenta l'Activity per visualizzare lo schema domanda/risposta in cui l'utente
 * legge tutte le informazioni che gli interessano e interagisce votando o inserendo la domanda
 * tra i preferiti. Nella navigazione dell'applicazione è figlia di QuestionsListActivity
 */
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
