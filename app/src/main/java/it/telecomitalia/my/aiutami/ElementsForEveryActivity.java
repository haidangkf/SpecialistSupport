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

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Classe che raccoglie gli elementi comuni di ogni Activity della applicazione, tipicamente il
 * menu della Toolbar, le funzioni per il logout e così via. Ogni nuova Activity deve estendere
 * ElementsForEveryActivity per poter avere gli elementi necessari
 */
public abstract class ElementsForEveryActivity extends AppCompatActivity {

    /**
     * Menu sulla toolbar
     * @param menu Oggetto Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    /**
     * Gestisce i clicks sulla toolbar e sul menu. Per quanto riguarda la
     * navigatione tra activity, viene invece gestita dalla toolbar e dal
     * file manifest in cui deve essere definita la gerarchia delle activity
     * @param item elemento del menu
     * @return booleano
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    /**
     * Funzione per il logout dalla applicazione. Elimina i dati utente dalla SP
     * in cui sono definiti, lancia l'Activity per l'inserimento di user e password
     * e distrugge l'activity da cui viene eseguito
     */
    public void logout(){

        getSharedPreferences(getString(R.string.USERINFO), Context.MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, Welcome.class ));
        finish();

    }

    /**
     * Se invocato in una activity, gestisce il FAB con l'azione di default definita
     * proprio in questo metodo.
     */
    public void insertDefaultFab(){

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Inserisco una nuova domanda\no chiedo supporto allo specialist", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * Metodo comune a tutte le classi dell'applicazione per poter leggere i file messi in cache e
     * eventualmente utilizzarli al posto della connessione di rete.
     * @param filename nome del file in cache
     * @param className tipologia del nodo xml
     * @return ArrayList di oggetti di tipo className
     * @throws Exception errori IO o XML
     */
    public ArrayList<Object> getStreamFromCachedFile(String filename, Class<?> className) throws Exception{

        String cachedXML;
        FileInputStream readFile = openFileInput(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(readFile));
        StringBuilder stringBuilder = new StringBuilder();

        while ((cachedXML = bufferedReader.readLine()) != null) {
            stringBuilder.append(cachedXML);
        }
        readFile.close();

        XMLReader x = new XMLReader();
        return x.getObjectsList(x.getXMLData(stringBuilder.toString()), className);

    }
}
