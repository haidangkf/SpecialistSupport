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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ApplicationServices extends IntentService {

    public static final String GETCATEGORIES  = "it.telecomitalia.my.aiutami.getCategories";
    public static final String GETQUESTIONS   = "it.telecomitalia.my.aiutami.getQuestions";
    public static final String GETFAVOURITES  = "it.telecomitalia.my.aiutami.getFavourites";
    public static final String GETMYQ         = "it.telecomitalia.my.aiutami.getMyQ";
    public static final String GETMYA         = "it.telecomitalia.my.aiutami.getMyA";

    /** Classe interna per la definizione dei webservices da interrogare. In questo modo è più agevole effettuare
     * variazioni a seconda dei cambiamenti avvenuti sul webserver. */
    private class WebServices{

        public static final String CATEGORIES  = "categories.php";
        public static final String QUESTIONS   = "questions.php";
        public static final String FAVOURITES  = "example_favs.xml";
        public static final String MYQ         = "example_myq.xml";
        public static final String MYA         = "example_mya.xml";

    }

    /**
     * Costruttore
     */
    public ApplicationServices() {
        super("ApplicationServices");
    }

    /**
     * Metodo principale per la gestione delle operazioni, agisce in base all'intent "application"
     * che riceve come parametro
     * @param intent azione da svolgere
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        final String service = intent.getStringExtra("application");
        switch (service) {

            case GETCATEGORIES : this.getCategories(); break;
            case GETQUESTIONS :
                this.getQuestions( intent.getIntExtra("filter",0) );
                break;
            case GETFAVOURITES : this.getFavourites(); break;
            case GETMYQ : this.getMyQuestions(); break;
            case GETMYA : this.getMyAnswers(); break;
            default:
                applicationNull(service);

        }
    }

    /**
     * Se viene lanciata un'operazione non supportata, ovvero non definita,
     * viene lanciata una eccezione
     * @param service operazione da eseguire
     */
    private void applicationNull(String service){

        throw new UnsupportedOperationException("Servizio non implementato: "+service);

    }

    /**
     * Metodo per collegarsi ad un webservice e scaricare dati XML. L'esito
     * viene trasformato in un ArrayList di oggetti e passato con un intent
     * a chi ha richiesto l'operazione
     * @param page parte finale della URL da interrogare
     * @param intentName nome dell'intent che viene passato
     * @param fileName nome del file salvato in cache
     * @param classType tipologia degli oggetti contenuti nella lista
     */
    private void getDataFromWebService(String page, String intentName, String fileName,  Class<?> classType){

        fileName = fileName != null ? fileName : intentName;

        OkHttpClient client  = new OkHttpClient();
        String url = IntranetServices.getWebservicesURL() + page;
        Intent localIntent = new Intent(intentName);
        ArrayList<?> list = null;
        XMLReader x;
        try {
            // prendo i dati dal webserver
            IntranetServices.enableSSL(client, getAssets().open(IntranetServices.CERTNAME));
            Request request = new Request.Builder().url(url).build();
            String response = client.newCall(request).execute().body().string();
            response = response.replace("\r\n","").replace("    ","");
            // tutto è andato bene, provo a fare il parse XML direttamente dello stream
            x = new XMLReader();
            list = x.getObjectsList(x.getXMLData(response), classType);
            // andata anche con XML, si mette a disposizione la lista
            localIntent.putExtra(intentName, list);
            // caching. ci arrivo solo se tutto è andato bene, altrimenti
            // è stata già sollevata un'eccezione ed il file non viene quindi toccato
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(response.getBytes());
            outputStream.close();

        } catch (Exception e) {
            // si spera che almeno una volta, l'operazione avvenga con successo. Ovvero che l'utente
            // abbia apn configurato bene e che sia coperto da segnale mobile. In questo caso mi sono
            // salvato il response del webservice ed in caso di errore mostro una schermata con i dati
            // della cache, invece che la schermata bianca di assenza connessione.
            boolean isCached = true;
            String cachedXML;
            try {
                FileInputStream readFile = openFileInput(fileName);
                BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(readFile) );
                StringBuilder stringBuilder = new StringBuilder();
                while ( (cachedXML = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(cachedXML);
                }
                readFile.close();
                x = new XMLReader();
                list = x.getObjectsList(x.getXMLData(stringBuilder.toString()), classType);

            }catch (Exception eFile){
                isCached = false;
            }
            // ho preso la cache, se esiste, e list è null oppure quello che ho letto dal file
            // salvato in precedentza
            localIntent.putExtra("isCached", isCached );
            localIntent.putExtra(intentName, list);

        } finally {
            sendBroadcast(localIntent);
        }

    }

    /**
     * Overload del metodo precedente, ha bisogno di un solo file in cache, e non molteplici
     * come nel caso delle categorie. In questo caso il nome del file prende il nome dell'intent
     * @param page parte finale della URL da interrogare
     * @param intentName nome dell'intent che viene passato
     * @param classType tipologia degli oggetti contenuti nella lista
     */
    private void getDataFromWebService(String page, String intentName, Class<?> classType){

        getDataFromWebService(page, intentName, null, classType);

    }

    /**
     * Metodo per collegarsi ad un webservice e scaricare la lista delle categorie. Tale lista
     * viene trasformata in un ArrayList di oggetti che rappresentano ogni entry e viene
     * passata con un intent a chi ha richiesto l'operazione
     */
    @SuppressWarnings("unchecked")
    private void getCategories() {

        getDataFromWebService(WebServices.CATEGORIES, GETCATEGORIES, Category.class);

    }

    /**
     * Metodo per collegarsi ad un webservice e scaricare la lista delle domande appartenenti ad
     * una categoria . Tale lista viene trasformata in un ArrayList di oggetti che rappresentano
     * ogni entry e viene passata con un intent a chi ha richiesto l'operazione. Per ogni categoria
     * viene creato un differente file in cache.
     * @param filter input per filtrare le domande in base alla categoria selezionata
     */
    private void getQuestions(int filter){

        String url      = WebServices.QUESTIONS+"?category=" + String.valueOf(filter);
        String fileName = GETQUESTIONS+String.valueOf(filter);
        getDataFromWebService(url, GETQUESTIONS, fileName, Question.class);

    }

    public void getFavourites(){

        getDataFromWebService(WebServices.FAVOURITES, GETFAVOURITES, Question.class);

    }

    public void getMyQuestions(){

        getDataFromWebService(WebServices.MYQ, GETMYQ, Question.class);

    }

    public void getMyAnswers(){

        getDataFromWebService(WebServices.MYA, GETMYA, Question.class);

    }
}
