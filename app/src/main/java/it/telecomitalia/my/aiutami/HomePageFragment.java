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

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

/**
 * Fragment che viene eseguito all'apertura della applicazione ed inserito
 * nella activity principale
 */
public class HomePageFragment extends Fragment {

    ElementsForEveryActivity activity;
    View layout;
    SwipeRefreshLayout refresh;
    ArrayList<Category> list;
    CategoriesReceiver myreceiver;

    /**
     * Costruttore di default
     */
    public HomePageFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        activity = (ElementsForEveryActivity)getActivity();
        myreceiver = new CategoriesReceiver();
        // carica categorie da webservice, ma intanto se c'è un file in cache lo
        // voglio ! In questo modo in mancanza di rete appaiono subito le categorie
        activity.sendIntentToService(activity, ApplicationServices.GETCATEGORIES);
        try {
            list = (ArrayList<Category>)(Object)activity.getStreamFromCachedFile(ApplicationServices.GETCATEGORIES, Category.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // aggiorna la View se arriva un intent quando il fragment è visibile all'utente
        activity.registerReceiver( myreceiver, new IntentFilter(ApplicationServices.GETCATEGORIES));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            activity.unregisterReceiver( myreceiver );
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

                activity.sendIntentToService(activity, ApplicationServices.GETCATEGORIES);

            }
        });

        refresh.setColorSchemeResources(R.color.primario_2);

        // inserisce la lista di elementi
        drawList(list);

        return layout;
    }

    /**
     * Metodo per disegnare la lista di elementi all'interno della
     * RecyclerView definita nel layout. Viene eseguito quando il fragment
     * viene visualizzato, oppure quando occorre aggiornare la lista
     * @param list elementi racchiusi in una lista
     */
    public void drawList(final ArrayList<Category> list){

        RecyclerView rv = (RecyclerView) layout.findViewById(R.id.recyclerview);
        // la recyclerview voglio sia un gridlayout a due colonne...
        GridLayoutManager layoutManager = new GridLayoutManager(activity, 2);
        // ... che varia però ogni tanto, per rompere la monotonia :)
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if( position == list.size()-1 & (position-1) % 3 == 0 ){
                    // se ultimo elemento occupa la colonna di sx singolarmente,
                    // ovvero posizioni 1,4,7,10,13, ... (position-1) % 3 == 0
                    return 2;
                }else
                    return 2 - (position % 3 >= 1 ? 1 : 0);
            }
        });
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(new CategoriesAdapter(activity, list));
        refresh.setRefreshing(false);

    }



    /**
     * Classe interna che definisce il receiver per gli intent lanciati dal
     * servizio.
     */
    public class CategoriesReceiver extends BroadcastReceiver {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {

            list = (ArrayList<Category>)intent.getSerializableExtra(ApplicationServices.GETCATEGORIES);
            drawList(list);
            // disegno la lista in ogni caso. se è null e non esiste salvataggio
            // apparirà scermata bianca, altrimenti la lista. Avviso che qualcosa
            // non è andato bene.
            if( intent.getBooleanExtra("isCached", false) || list==null ){
                // se i dati arrivano dalla cache oppure list è ancora null...
                if ( isAdded() ){
                    Snackbar.make(refresh, getResources().getString(R.string.error_refresh), Snackbar.LENGTH_LONG).show();
                }
            }

        }

    }
}

