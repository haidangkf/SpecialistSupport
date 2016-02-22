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

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import java.util.ArrayList;

/**
 * Classe che rappresenta l'Activity per la visualizzazione di una lista di elementi
 * che rappresentano le domande appartenenti ad una determinata categoria.
 * Nella navigazione dell'applicazione è figlia di MainActivity, con il fragment HomePage
 */
public class QuestionsListActivity extends ElementsForEveryActivity {

    int color;
    ArrayList<Question> list;
    SwipeRefreshLayout refresh;
    QuestionsListReceiver myreceiver;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_list);
        myreceiver = new QuestionsListReceiver(this);

        // Toolbar e navigazione
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // personalizzazioni toolbar, titolo e colori
        String title = getIntent().getStringExtra("title");
        color = getIntent().getIntExtra("color", R.color.primario_1);
        final int id = getIntent().getIntExtra("filter", 0);

        getSupportActionBar().setTitle(title);
        toolbar.setBackgroundColor(color);
        ctl.setBackgroundColor(color);
        ctl.setContentScrimColor(color);
        ctl.setStatusBarScrimColor(color);

        // inserimento fab
        this.insertDefaultFab();

        // carica dati da webservice, ma intanto se c'è un file in cache lo
        // voglio ! In questo modo in mancanza di rete appaiono subito i dati
        sendIntentToService(this, ApplicationServices.GETQUESTIONS, id);
        try {
            list = (ArrayList<Question>)(Object)getStreamFromCachedFile(
                    ApplicationServices.GETQUESTIONS+String.valueOf(id),
                    Question.class
            );
        }catch (Exception e){
            e.printStackTrace();
        }

        // appena creata la view, intanto che carico i dati, mostro il refresh
        refresh = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
            }
        });

        // azione per il pull to refresh
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                sendIntentToService(QuestionsListActivity.this, ApplicationServices.GETQUESTIONS, id);

            }
        });

        refresh.setColorSchemeResources(R.color.primario_2);

        // inserisce la lista di elementi
        myreceiver.drawList(list);
    }

    @Override
    public void onResume() {
        super.onResume();
        // aggiorna la View se arriva un intent quando il fragment è visibile all'utente
        registerReceiver(new QuestionsListReceiver(this), new IntentFilter(ApplicationServices.GETQUESTIONS));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(new QuestionsListReceiver(this));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
