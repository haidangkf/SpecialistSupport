/*******************************************************************************
 * This software is distributed under the following BSD license:
 *
 * Copyright (c) 2015, Marco Paoletti <mpao@me.com>, http://mpao.github.io
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import java.io.File;
import java.util.HashMap;

/* Activity che viene lanciata dal manifest file con il click sull'icona. Si occupa di
* autenticare l'utente o, se esistono dei dati utenti, di lanciare l'activity principale
* dell'applicazione. */

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        /* Per l'applicazione, ho creato due classi da riutilizzare per svolgere tutte o quasi
        * le operazioni che richiedono una connessione alla intranet. IntranetOperations le
        * svolte attraverso un servizio e, una volta conclusa, lancia un broadcast message che
        * deve essere raccolto dal relativo receiver. Creo quindi tale receiver e lo predispongo
        * all'ascolto dei broadcast messages della classe IntranetOperations.*/
        new IntranetReceiver(this).InizializzaReceivers();
        /* Se l'applicazione si aggiorna in automatico scaricando files dal server, ovvero non
        * utilizza altri sistemi come ad esempio l'OAStore, devo ricordarmi di cancellare i files
        * scaricati per non disturbare. Perchè lo faccio in onCreate() ? Se hai dubbi guarda il
        * progetto Prototipo su Github e il relativo diagramma di flusso che ne spiega il funzionamento,
        * ma in poche parole funziona cosi: in onStart cerco e scarico l'aggiornamento e lo mando in
        * esecuzione. Una volta terminata l'installazione, l'applicazione va in onDestroy per poi
        * essere rilanciata. Viene eseguito quindi onCreate che fa pulizia dei files che ho appena
        * finito di utilizzare. */
        Welcome.updateDeleteFiles(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        /* Metodo statico  che ricerca sul server un eventuale aggiornamento con il supporto di
         * IntranetOperations. Vedi più avanti la definizione del metodo. Lo utilizzo all'interno di
         * onStart in modo che l'utente sia obbligato a fare l'aggiornamento, in quanto questo viene
         * riproposto ogni volta. Se volessi permettere all'utente di posticipare l'update, mi
         * basta spostare questa richiesta in onCreate, cosi che venga richiesto solo una volta. */
        Welcome.updateCheck(this);
        /* autologin leggendo e cercando il numero di telefono su LDAP.
        * tristemente inutile visto che al momento le nostre SIM non hanno questa informazione.
        * soluzione: baro. Il primo login l'utente lo fa con la password, quando
        * cerco su LDAP, mi prendo il numero di telefono e lo salvo in un SharedPreference
        * differente da quello del profilo di login. Quando rilancio l'applicazione,
        * controllo la sua esistenza et voilà, login senza password ;) */
        SharedPreferences sp = getSharedPreferences(getString(R.string.TELEFONO), Context.MODE_PRIVATE);
        /* il numero di telefono, che credo sarà sempre null, a meno di aggiornamenti */
        TelephonyManager tMng = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        /* se ho già fatto un login, sp("telefono") esiste, altrimenti null o tMng( null :( ) */
        String numeroTelefono =
                sp.contains("telefono") ? sp.getString("telefono", null) : tMng.getLine1Number().replace("+39", "");
        /* se ho trovano un numero di telefono, evito di fare il
        * login con la password e lancio la ricerca di tale numero sull'albero LDAP sempre attraverso
        * IntranetOperations. Nel frattempo che aspetto il risultato, nell'activity setto come
        * visibile il layout di autologin, nascondendo i pulsanti e mostrando un loading. Una volta che
        * il receiver riceverà il risultato, agirò di coneguenza, vedi i commenti nel Receiver, ma di
        * base se ho trovato un utente, mostro i tasti, se non lo ho trovato mostro il layout con
        * la possibilità di inserire user e password */
        if( numeroTelefono!=null && !numeroTelefono.equals("") ){
            Intent i = new Intent(this, IntranetServices.class);
            i.putExtra("service", IntranetServices.SEARCH);
            i.putExtra("telefono", numeroTelefono);
            startService(i);
            /* mostro il layout che mi serve */
            findViewById(R.id.userAndPassword).setVisibility(View.GONE);
            findViewById(R.id.phoneLogin).setVisibility(View.VISIBLE);
            /* nascondo i pulsanti in modo che l'utente non interagisca finchè il servizio sta
             * girando in background e mostro un loading */
            findViewById(R.id.cambiaUtente).setVisibility(View.GONE);
            findViewById(R.id.autoLogin).setVisibility(View.GONE);
            findViewById(R.id.loadingPanelPhone).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        /* Quando l'applicazione parte, oppure esce dallo stato di pausa, controllo che esistano
         * salvate le USERINFO, ovvero che l'utente abbia fatto il login. Tali info infatti vengono
         * create da IntranetOperations se il login è andato a buon fine. Insomma, se l'utente è
         * loggato, mostro direttamente l'activity principale della applicazione distruggendo Welcome con
         * finish(), in modo che con il tasto back, l'utente esca dall'applicazione come ci si
         * aspetterebbe. Quindi, se esistono le USERINFO, quando clicco su icona della applicazione,
         * la prima activity che vedrò sarà MainActivity, altrimenti Welcome. */
        SharedPreferences userInfo = getBaseContext().getSharedPreferences(getString(R.string.USERINFO), Context.MODE_PRIVATE);
        if( userInfo.contains("matricola") ){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            /* se l'activity è stata messa in pausa o stoppata, non voglio rimanga la password inserita */
            TextView p = (TextView)findViewById(R.id.password);
            p.setText("");
        }
    }

    public static boolean updateDeleteFiles(Activity a){
        /* Ecco il metodo statico per cancellare i files di installazione. Ottengo il path della
        * directory che il sistema utilizza come directoy per i downloads */
        File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        /* mi passo ogni file contenuto nella directory, se il nome contiente il package name della
         * applicazione, lo cancello. Ricordarsi quindi che i files di aggiornamento sul server _devono_
         * essere nominati con il package name. Il metodo ritorna un booleano per evitare un warning, ma
         * di fatto il risultato del metodo non lo utilizzo. */
        try{
            File[] listFile = downloadPath.listFiles();
            for( File f : listFile){
                if( f.toString().contains(a.getPackageName()) ) {
                    return f.delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void updateCheck(Activity a){
        /* Metodo per eseguire update della applicazione: eseguo come servizio IntranetOperations
         * passandogli il parametro di update. Quando il servizio avrà terminato, l'activity coglierà
         * il broadcast message ed eseguirà l'update. */
        Intent i = new Intent(a, IntranetServices.class);
        i.putExtra("service", IntranetServices.UPDATE);
        a.startService(i);
    }

    public void login(View v){
        /* Metodo che viene eseguito dalla View, quando clicco il tasto Entra. Se la tastiera è
         * sullo schermo, la nascondo. Se la tastiera non è a schermo, si ha una nullpointerexception,
         * quindi mi assicuro che ci sia, prima di nasconderla
         * http://stackoverflow.com/questions/3400028/close-virtual-keyboard-on-button-press */
        InputMethodManager k = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        k.hideSoftInputFromWindow((null == getCurrentFocus()) ?
                null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        /* mi leggo i campi e faccio le opportune analisi */
        String m = ((TextView)findViewById(R.id.user)).getText().toString();
        String p = ((TextView)findViewById(R.id.password)).getText().toString();
        if( m.equals("") || p.equals("") )
            /* se non ho inserito nulla nei campi  */
            loginError(1); // campi vuoti
        else{
            /* se esistono userid e password, */
            if( IntranetServices.isConnected(this) ){
                if( IntranetServices.isWiFiConnected(this) )
                    loginError(3); // wifi non aziendale
                else if( !IntranetServices.isLTEConnected(this) )
                    loginError(4); // apn non aziendale
                else
                /* se tutte le connessioni sono corrette, e quindi posso raggiungere il
                 * server, eseguo il metodo per la ricerca su LDAP */
                    ldapAuthentication(m,p); // Entra pure :)
            }else
                loginError(2); // nessuna connessione
        }
    }

    public void loginError(int error){
        /* a seconda dell'errore che ottengo dal login, mostro snackbar che lo descrive */
        switch (error){
            case 1:
                // matricola e password non inseriti
                buildMessage(getString(R.string.error1)).show();
                break;
            case 2:
                // matricola e password non inseriti
                buildMessage(getString(R.string.error2)).show();
                break;
            case 3:
                // empty connessione alla intranet
                buildMessage(getString(R.string.error3)).setAction(getString(R.string.error_button1), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).show();
                break;
            case 4:
                // empty connessione alla intranet
                buildMessage(getString(R.string.error4)).setAction(getString(R.string.error_button2), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APN_SETTINGS));
                    }
                }).show();
                break;
            case 6:
                // utente non presente nel db locale o non abilitato alla applicazione
                buildMessage(getString(R.string.error6)).show();
                break;
        }
    }

    private void ldapAuthentication(String m, String p){
        /* metodo per la ricerca su LDAP. Nascondo i pulsanti, mostro il loading... */
        findViewById(R.id.btnLogin).setVisibility(View.GONE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        /* e lancio intent a IntranetOperations che fa tuto il lavoro */
        Intent i = new Intent(this, IntranetServices.class);
        i.putExtra("service", IntranetServices.AUTH);
        i.putExtra("matricola", m);
        i.putExtra("password", p);
        this.startService(i);
    }

    public void loginResult(boolean esito, HashMap<String, String> m){
        /* quando il servizio finisce, il receiver esegue questo metodo. I pulsanti sono
         * ancora disabilitati, e il loading ancora a video. */
        findViewById(R.id.btnLogin).setVisibility(View.GONE);
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        if(esito){
            /* Se l'esito è andato a buon fine, quindi userid e password sono corretti, la prima
            * cosa che faccio è salvarmi il numero di cellulare dal tree LDAP in modo che da ora in
            * poi, sia possibile il login automatico. */
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.TELEFONO), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("telefono", m.get("telefono"));
            editor.apply();
            /* OK, sei abilitato su LDAP, ma lo sei anche per l'applicazione ? e con quale profilo ?
            * Dopo aver contattato il server LDAP, contatto il server locale di appoggio su cui risiede il
            * database della applicazione, sempre attraverso IntranetOperations, e aspetto la profilatura
            * dell'utente. Solito giochino, aspetto il receiver. */
            Intent i = new Intent(this, IntranetServices.class);
            i.putExtra("service", IntranetServices.PROFILING);
            i.putExtra("userProperties", m);
            this.startService(i);
        }else{
            /* La matricola non esiste, oppure la password è errata. Inutile proseguire, rimettiamo
             * le view a posto, mostrando nuovamente i pulsanti e mostro uno snackbar di avviso in
             * modo che l'utente possa agire di conseguenza. */
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            findViewById(R.id.btnLogin).setVisibility(View.VISIBLE);
            buildMessage(getString(R.string.error5)).show();
        }
    }

    public void loginResult(boolean esito, boolean networkError){
        /* metodo che analizza il risultato della profilatura, viene eseguito dal receiver
        * se l'esito è andato a buon fine, vuol dire che sono abilitato sul database della applicazione,
        * posso cominciare ad usarla. I dati di utenza sono salvati, faccio partire la MainActivity,
        * l'applicazione può cominciare ad essere eseguita.*/
        if( esito ){
            Intent i = new Intent(this, MainActivity.class);
            /* Welcome deve sparire dalla history, dal backstack. MainActivity viene lanciata quindi
            * con il flag noHistory. Lo faccio in questo modo e non utilizzando android:noHistory="true"
            * all'interno del manifest file perchè in alcuni casi Welcome deve rimanere nel backstack,
            * come ad esempio quando dallo SnackBar apro l'activity per disattivare il WiFi o
            * cambiare l'APN.*/
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            finish();
        }else{
            /* Esito negativo! O non sono abilitato, oppure il server di appoggio non è raggiungibile
             * per qualche problema. */
            if( findViewById(R.id.phoneLogin).getVisibility()==View.VISIBLE ){
                /* se questo metodo è eseguito dal layout autologin, mostro i relativi
                * pulsanti e nascondo il loader. Il layout è visibile dopo che sono stato
                * autenticato su LDAP */
                findViewById(R.id.cambiaUtente).setVisibility(View.VISIBLE);
                findViewById(R.id.autoLogin).setVisibility(View.VISIBLE);
                findViewById(R.id.loadingPanelPhone).setVisibility(View.GONE);
            }
            if( findViewById(R.id.userAndPassword).getVisibility()==View.VISIBLE ){
                /* se questo metodo è eseguito dal layout useid e password faccio
                * altrettanto. */
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                findViewById(R.id.btnLogin).setVisibility(View.VISIBLE);
            }
            /* Ok non sono stato autenticato, le ragioni me la fornisce il booleano networkError
             * ricavato dal receiver. Mostro snackbar per avvisare l'utente */
            if( networkError ){
                // server down
                buildMessage(getString(R.string.error7)).show();
            }else
                // utente non abilitato
                buildMessage(getString(R.string.error6)).show();
        }
    }

    private Snackbar buildMessage(String s) {
        /* come si costruisce uno snackbar con un action button ? eccolo qui, con la definizione dei
        * colori e dell'estetica dell'avviso. Se devo cambiare qualcosa, per adattarlo ad un nuovo
        * template, è qui che devo mettere le mani.
        * se è errore di WiFi o APN, ovvero uno di quei casi in cui lo Snackbar ha un
        * action button per permettere all'utente di intervenire, metto il tempo a
        * indefinito, in modo che sia facilmente cliccabile l'action button. */
        int time = ( s.equals(getString(R.string.error3)) | s.equals(getString(R.string.error4)) ) ?
                Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG;
        /* Output per lo Snackbar */
        Snackbar message = Snackbar.make(findViewById(R.id.layout), s, time);
        View snackBarView = message.getView();
        message.setActionTextColor(Color.RED);
        snackBarView.setBackgroundColor(Color.BLACK);
        TextView text = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        TextView button = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_action);
        button.setTextSize(15);
        button.setTypeface(null, Typeface.BOLD);
        text.setTextSize(15);
        text.setTextColor(Color.WHITE);
        return message;
    }

    public void cambiaUtente(View v){
        /* metodo che viene eseguito dalla view, solo se il numero di telefono è stato riconosciuto e sono
        * pronto a fare login senza inserire la password. in questo caso, posso cambiare utente, cioè
        * mostrare il layout per inserire userid e password */
        View a = findViewById(R.id.phoneLogin);
        View b = findViewById(R.id.userAndPassword);
        /* il cambio di layout avviene con una gradevole animazione :) */
        Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        a.setAnimation(animFadeOut);
        b.setAnimation(animFadeIn);
        a.setVisibility(View.GONE);
        b.setVisibility(View.VISIBLE);
        findViewById(R.id.btnLogin).setVisibility(View.VISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        /* cambiando utente, mi premuro di cancellare la sharedPreferences in cui è salvato il
         * numero di telefono. Cancello tutto, l'unica informazione contenuta è il numero di telefono. */
        getSharedPreferences(getString(R.string.TELEFONO), Context.MODE_PRIVATE).edit().clear().apply();
    }

    public void loginWithPhoneNumber(View v){
        /* Metodo che viene eseguito dal layout autologin, quando clicco il tasto entra. Avendo il numero di
        * telefono, so già chi è l'utente attraverso LDAP. Insomma, essere qui equivale ad avere già inserito
        * password, essere stati riconosciuti da LDAP, aver tutte le informazioni utente fornite da
        * IntranetOperations e dal receiver: l'unica cosa che manca è controllare che l'utente sia abilitato,
        * e lo faccio esattamente come per il layout userid e password...*/
        findViewById(R.id.cambiaUtente).setVisibility(View.GONE);
        findViewById(R.id.autoLogin).setVisibility(View.GONE);
        findViewById(R.id.loadingPanelPhone).setVisibility(View.VISIBLE);
        /*... attraverso loginResult, che eseguo con true ( come detto è come se fossi già passato da
        * autenticazione LDAP, e come secondo parametro tutte le informazioni utente che possiede il
        * receiver, tra le quali c'è la matricola con cui eseguire la profilatura sul database dell'app. */
        loginResult(true, IntranetReceiver.userInfo2);
    }
}
