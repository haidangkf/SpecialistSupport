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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

/**
 * Classe che rappresenta l'Activity per la visualizzazione di una lista di elementi
 * che rappresentano le domande appartenenti ad una determinata categoria.
 * Nella navigazione dell'applicazione è figlia di MainActivity, con il fragment HomePage
 */
public class QuestionsListActivity extends ElementsForEveryActivity {

    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra("title");
        color    = getIntent().getIntExtra("color", R.color.primario_1);
        getSupportActionBar().setTitle(title);
        toolbar.setBackgroundColor(color);
        ctl.setBackgroundColor(color);
        ctl.setContentScrimColor(color);
        ctl.setStatusBarScrimColor(color);
        this.insertDefaultFab();

        creaLista();
    }

    public void creaLista(){

        String test = "<questions>" +
                "<question>" +
                "<domanda>Come posso porre una domanda all'interno della applicazione ?</domanda>" +
                "<voti>5</voti>" +
                "<data>11 febbraio 2015</data>" +
                "<sinossi>Questa è la risposta o una sua sinossi in qualche riga. pensare a far inserire un riassunto in fase di compilazione</sinossi>" +
                "<categories>" +
                "<category>" +
                "<name>Topolino</name>" +
                "<color>#006e78</color>" +
                "</category>" +
                "<category>" +
                "<name>Paperino</name>" +
                "<color>#82c85a</color>" +
                "</category>" +
                "<category>" +
                "<name>Pico de' Paperis</name>" +
                "<color>#CCC000</color>" +
                "</category>" +
                "</categories>" +
                "</question>" +
                "<question>" +
                "<domanda>Come posso porre una domanda all'interno della applicazione ?</domanda>" +
                "<voti>17</voti>" +
                "<data>2 febbraio 2015</data>" +
                "<sinossi>Questa è la risposta o una sua sinossi in qualche riga. pensare a far inserire un riassunto in fase di compilazione</sinossi>" +
                "<categories>" +
                "<category>" +
                "<name>Minni</name>" +
                "<color>#376ea5</color>" +
                "</category>" +
                "</categories>" +
                "</question>" +
                "</questions>";
        ArrayList<Question> list = null;
        try {
            XMLReader x = new XMLReader();
            list = (ArrayList<Question>) (Object) x.getObjectsList(x.getXMLData(test), Question.class);
        }catch (XMLReader.GodzillioniDiXMLExceptions e){
            e.printStackTrace();
        }
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter( new QuestionsListAdapter(this, color, list) );


    }

}
