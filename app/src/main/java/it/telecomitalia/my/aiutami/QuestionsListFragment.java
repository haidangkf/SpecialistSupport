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
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Fragment per la visualizzazione degli elementi preferiti all'interno della
 * activity principale
 */
public class QuestionsListFragment extends Fragment {

    ElementsForEveryActivity activity;
    View layout;
    ArrayList<Question> list;
    SwipeRefreshLayout refresh;
    QuestionsListReceiver myreceiver;
    String whichFilter;


    public QuestionsListFragment() {
        // Required empty public constructor
    }

    /**
     * Istanzia un nuovo oggetto con parametro. Tale parametro è una String, attributo pubblico di
     * ApplicationServices che identifica che tipologia di fragment istanziare e quindi che servizio
     * utilizzare. Resta sempre costante il fatto che sia un fragment che elenca oggetti Question.
     * @param filter attributo di ApplicationServices, vedi doc
     * @return nuova instanza di QuestionsListFragment
     */
    public static QuestionsListFragment newInstance(String filter){

        QuestionsListFragment f = new QuestionsListFragment();
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

        return layout;

    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){

        super.onActivityCreated(savedInstanceState);
        myreceiver = new QuestionsListReceiver(activity, layout);
        // inserisce la lista di elementi
        myreceiver.drawList(list, layout);

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

}
