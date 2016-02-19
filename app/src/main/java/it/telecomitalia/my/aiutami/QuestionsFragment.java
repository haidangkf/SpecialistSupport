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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Fragment per la visualizzazione degli elementi preferiti all'interno della
 * activity principale
 */
public class QuestionsFragment extends Fragment {

    ElementsForEveryActivity activity;
    View layout;
    ArrayList<Question> list;
    SwipeRefreshLayout refresh;
    QuestionsReceiver myreceiver;
    String whichFilter;


    public QuestionsFragment() {
        // Required empty public constructor
    }

    /**
     * Istanzia un nuovo oggetto con parametro. Tale parametro è una String, attributo pubblico di
     * ApplicationServices che identifica che tipologia di fragment istanziare e quindi che servizio
     * utilizzare. Resta sempre costante il fatto che sia un fragment che elenca oggetti Question.
     * @param filter attributo di ApplicationServices, vedi doc
     * @return nuova instanza di QuestionsFragment
     */
    public static QuestionsFragment newInstance(String filter){

        QuestionsFragment f = new QuestionsFragment();
        Bundle args = new Bundle();
        args.putString("filter", filter);
        f.setArguments(args);
        return f;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        activity = (ElementsForEveryActivity)getActivity();
        myreceiver = new QuestionsReceiver();

        // lista di domande, ma di che tipo ?
        whichFilter = getArguments().getString("filter");

        // carica dati da webservice, ma intanto se c'è un file in cache lo
        // voglio ! In questo modo in mancanza di rete appaiono subito i dati
        activity.sendIntentToService(activity, whichFilter);
        try {
            list = (ArrayList<Question>)(Object)activity.getStreamFromCachedFile(whichFilter, Question.class);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.f_home_page, container, false);
        refresh = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);

        // appena creata la view, intanto che carico i dati, mostro il refresh
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

                activity.sendIntentToService(activity, whichFilter);

            }
        });

        refresh.setColorSchemeResources(R.color.primario_2);

        // inserisce la lista di elementi
        drawList(list);

        return layout;

    }

    @Override
    public void onResume() {
        super.onResume();
        // aggiorna la View se arriva un intent quando il fragment è visibile all'utente
        activity.registerReceiver(myreceiver, new IntentFilter(whichFilter));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            activity.unregisterReceiver(myreceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Metodo per disegnare la lista di elementi all'interno della
     * RecyclerView definita nel layout. Viene eseguito quando activity
     * viene visualizzato, oppure quando occorre aggiornare la lista
     * @param list elementi racchiusi in una lista
     */
    public void drawList(final ArrayList<Question> list){

        RecyclerView rv = (RecyclerView) layout.findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(activity));
        rv.setAdapter( new QuestionsListAdapter(activity, list) );
        refresh.setRefreshing(false);

    }

    /**
     * Classe interna che definisce il receiver per gli intent lanciati dal
     * servizio.
     */
    public class QuestionsReceiver extends BroadcastReceiver {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {

            list = (ArrayList<Question>)intent.getSerializableExtra(whichFilter);
            drawList(list);
            // disegno la lista in ogni caso. se è null e non esiste salvataggio
            // apparirà scermata bianca, altrimenti la lista. Avviso che qualcosa
            // non è andato bene.
            if( intent.getBooleanExtra("isCached", false) ){
                // se i dati arrivano dalla cache
                Snackbar.make(refresh, getResources().getString(R.string.error_refresh), Snackbar.LENGTH_LONG).show();
            }
            // se list è ancora null, o non ha elementi, metto una immagine per mandare il messaggio di
            // "contenitore vuoto". Al pull to refresh, se la situazione cambia, l'immagine scompare
            if( list==null || list.size()==0 ){
                refresh.setBackground(ContextCompat.getDrawable(activity, R.drawable.background_null));
            }else{
                refresh.setBackground(null);
            }
        }

    }

}
