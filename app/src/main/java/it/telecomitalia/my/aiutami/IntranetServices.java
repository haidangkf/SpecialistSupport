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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.controls.PasswordExpiredControl;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class IntranetServices extends IntentService {

    // Configurazione
    private static final String LDAP              = "10.173.83.114";//"directory.services.external.local";
    private static final String DN                = "OU=dipendenti,OU=TelecomItalia,O=Telecom Italia Group";
    private static final String[] wifiAbilitate   = {"CONSULENTI","DIPENDENTI","TELECOM"};
    private static final String APN               = "intelecomitalia.tim.it";
    private static final String LOG               = "IntranetServices";

    public static final String SERVER            = "10.38.105.34";
    public static final String PATH              = "/aiutami/";
    public static final String DIRSERVICES       = "webservices/";
    public static final String CERTNAME          = "ngdc.crt";
    public static final int TIMEOUT              = 10; //10 secondi

    public static final String UPDATE             = "UPDATE";
    public static final String SEARCH             = "SEARCH";
    public static final String AUTH               = "AUTH";
    public static final String PROFILING          = "PROFILING";

    /** Classe interna per la definizione dei webservices da interrogare. In questo modo è più agevole effettuare
     * variazioni a seconda dei cambiamenti avvenuti sul webserver. */
    protected class WebServices{

        public static final String PROFILING      = "userprofile.php";
        public static final String UPDATE         = "versioning.json";

    }

    /** Costruttore di default */
    public IntranetServices() {
        super("IntranetServices");
    }

    /**
     * Metodo di classe per ottenere il PATH completo di dove risiedono i webservices.
     * in questo modo posso utilizzare IntranetServices come appoggio ad altri servizi
     * che sono specifici per una determinata applicazione
     * @return Stringa che rappresenta una URL
     */
    public static String getWebservicesURL(){
        return "https://" + SERVER + PATH + DIRSERVICES;
    }

    /**
     * Controlla se la Intranet &egrave; raggiungibile
     * @param c Context in cui viene richiesto il controllo, tipicamente una Activity
     * @return Booleano che indica lo stato della connessione
     */
    public static boolean isConnected(Context c) {
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connManager.getActiveNetworkInfo();
        return activeInfo != null && activeInfo.isConnected();
    }

    /**
     * Controlla se il WiFi è attivo e collegato e se la rete a cui si è connessi è una rete
     * in grado di fornire l'accesso alla Intranet. I SSID di tali reti sono definiti nella proprietà
     * di classe wifiAbilitate.
     * @return Booleano che indica lo stato della connessione
     */
    public static boolean isWiFiConnected(Context c){
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connManager.getActiveNetworkInfo();
        if( activeInfo != null && activeInfo.isConnected() ) {
            boolean wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            if( wifiConnected && !Arrays.asList(wifiAbilitate).contains(activeInfo.getExtraInfo().replace("\"","")) ){
                //connesso in wifi con reti aziendali
                return true;
            }
        }
        return false;
    }

    /**
     * Controlla che la connessione mobile sia fornita dall'APN aziendale e che quindi
     * l'accesso alla Intranet sia garantito.
     * @return Booleano che indica lo stato della connessione
     */
    public static boolean isLTEConnected(Context c){
        ConnectivityManager connManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connManager.getActiveNetworkInfo();
        if( activeInfo != null && activeInfo.isConnected() ) {
            boolean mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            if( mobileConnected && activeInfo.getExtraInfo().equalsIgnoreCase(APN) ){
                //connesso con 4G
                return true;
            }
        }
        return false;
    }

    /** Applica all' oggetto OkHttpClient i certificati SSL */
    public static void enableSSL(OkHttpClient c, InputStream cert) throws IOException, CertificateException, KeyStoreException,
                                                  KeyManagementException, NoSuchAlgorithmException{
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(cert);
        Certificate ca;
        ca = cf.generateCertificate(caInput);
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, tmf.getTrustManagers(), null);
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };

        c.setHostnameVerifier(allHostsValid);
        c.setConnectTimeout(TIMEOUT, TimeUnit.SECONDS);
        c.setSslSocketFactory(sslcontext.getSocketFactory());
    }

    /** metodo per REST get sul Server configurato. Usa SSL, pertanto è necessario importare nella directory assets il
     * certificato pubblico del webserver, e configurare adeguatamente il nome del certificato stesso nei parametri di
     * configurazione.
     * @return il contenuto della pagina web in formato stringa */
    protected String getRequest(String url) throws IOException, CertificateException, KeyStoreException,
                                         KeyManagementException, NoSuchAlgorithmException {

        OkHttpClient client = new OkHttpClient();
        enableSSL(client, getAssets().open(CERTNAME));
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();

    }

    /**
     * Questo metodo viene eseguito attraverso startService di una activity. Nell' intent che viene preso
     * come parametro, deve esserci una stringa "service" che identifica l'azione da svolgere. Tale valore deve
     * essere necessariamente uno definito dalle proprietà della classe, altrimenti viene lanciata una eccezione
     * UnsupportedOperationException attraverso il metodo serviceNull
     * @param intent Intent passato dalla Activity richiedente con l'operazione richiesta.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        final String service = intent.getStringExtra("service");
        switch (service) {
            case IntranetServices.UPDATE:
                serviceCheckUpdate(); break;
            case IntranetServices.SEARCH:
                serviceSearchByPhoneNumber(intent.getStringExtra("telefono")); break;
            case IntranetServices.AUTH:
                serviceUserAuth(intent.getStringExtra("matricola"), intent.getStringExtra("password")); break;
            case IntranetServices.PROFILING:
                @SuppressWarnings("unchecked")
                HashMap<String, String> userInfo = (HashMap<String, String>)intent.getSerializableExtra("userProperties");
                serviceProfileFromUID(userInfo);
                break;

            default:
                serviceNull();
        }
    }

    /**
     * Si connette ad un file json remoto e confronta attraverso il versionCode, la versione installata e la versione
     * fornita dal json. Se quella sul server ha un versionCode maggiore, allora è un aggiornamento e quindi va scaricato.
     * Una volta ottenuto il file, lancia un intent con il path di tale file
     */
    private void serviceCheckUpdate(){
        Intent localIntent = new Intent(getPackageName()+"."+ IntranetServices.UPDATE);
        int runningVersion;

        try {
            String response = this.getRequest("https://" + SERVER + PATH + WebServices.UPDATE);

            JSONObject current = new JSONObject(response).getJSONObject("current");
            int currentVersion = current.getInt("build");
            String currentAPK  = current.getString("apk");
            PackageInfo info   = getPackageManager().getPackageInfo(getPackageName(), 0);
            runningVersion     = info.versionCode;

            if( currentVersion > runningVersion ){
                File downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                OkHttpClient client = new OkHttpClient();
                enableSSL(client, getAssets().open(CERTNAME));
                Request request = new Request.Builder().url("https://" + SERVER + PATH + currentAPK).build();

                File outputFile = new File(downloadPath, currentAPK);
                // se esistono download parziali o vecchi, li elimino.
                if( outputFile.delete() ) Log.i(LOG, "file eliminato");
                InputStream input = client.newCall(request).execute().body().byteStream();
                FileOutputStream myapk = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int count;
                while ((count = input.read(buffer)) != -1) { myapk.write(buffer, 0, count); }
                myapk.close();
                input.close();
                localIntent.putExtra("apk", outputFile.getAbsolutePath() );
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    /**
     * Autenticazione LDAPSSL attraverso il numero di telefono. Per questioni di sicurezza, il numero
     * di telefono deve essere passato al metodo esclusivamente leggendolo dallo stato del telefono che
     * sta utilizzando l'applicazione. Se tale numero non è leggibile dalla SIM, è richiesta l'autenticazione
     * con password. In questo caso viene salvato in locale il telefonico e i successivi login, avverranno
     * attraverso questo metodo.
     * Se il numero di telefono viene trovato sul tree ldap, lancia un intent che contiene la
     * matricola dell'utente che deve essere gestito da un BroadcasterReceiver
     */
    private void serviceSearchByPhoneNumber(String phoneNumber){
        Intent localIntent = new Intent(getPackageName()+"."+ IntranetServices.SEARCH);
        try{
            // https://docs.ldap.com/ldap-sdk/docs/javadoc/com/unboundid/util/ssl/SSLUtil.html
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
            LDAPConnection connection = new LDAPConnection(sslSocketFactory, LDAP, 636); //389 porta normale, 636 SSL
            SearchResult searchResult = connection.search(DN, SearchScope.SUB, "(mobile=" + phoneNumber + ")");
            if(searchResult.getEntryCount()>0){
                //questa è la lista degli elementi trovati, servisse mai...
                List<SearchResultEntry> elements = searchResult.getSearchEntries();
                // creo un oggetto utente che raccoglie le proprietà di LDAP
                HashMap<String, String> userProperties = getLdapProperties(elements);
                localIntent.putExtra("userProperties", userProperties);
                // return elements.get(0); SearchResultEntry che identifica utente
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    /**
     * Autenticazione LDAPSSL attraverso matricola e password. Controlla su LDAP le credenziali dell'utente e
     * ne permette la prosecuzione solo se autorizzato.
     * @param matricola Matricola di dominio
     * @param password Password di dominio
     */
    private void serviceUserAuth(String matricola, String password){
        Intent localIntent = new Intent(getPackageName()+"."+ IntranetServices.AUTH);
        try{
            // https://docs.ldap.com/ldap-sdk/docs/javadoc/com/unboundid/util/ssl/SSLUtil.html
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
            LDAPConnection connection = new LDAPConnection(sslSocketFactory, LDAP, 636); //389 porta normale, 636 SSL
            BindResult bindResult = connection.bind(new SimpleBindRequest("uid=" + matricola + "," + DN, password));
            if(bindResult.getMessageID()==1){
                PasswordExpiredControl pwdScaduta = PasswordExpiredControl.get(bindResult);
                if(pwdScaduta==null){
                    // se è NULL, la password è ancora valida e procedo alla ricerca dei miei dati
                    SearchResult searchResult = connection.search(DN, SearchScope.SUB, "(uid=" + matricola + ")");
                    List<SearchResultEntry> elements = searchResult.getSearchEntries();
                    // creo un oggetto utente che raccoglie le proprietà di LDAP
                    HashMap<String, String> userProperties = getLdapProperties(elements);
                    localIntent.putExtra("userProperties", userProperties);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    private HashMap<String,String> getLdapProperties(List<SearchResultEntry> properties){

        HashMap<String, String> userProperties = new HashMap<>();
        userProperties.put("matricola", properties.get(0).getAttribute("uid").getValue());
        userProperties.put("nome", properties.get(0).getAttribute("displayName").getValue());             //paoletti marco
        userProperties.put("struttura", properties.get(0).getAttribute("tigFamProf").getValue());          //Network
        userProperties.put("bacino", properties.get(0).getAttribute("department").getValue());            //OA.AS/C.OP
        userProperties.put("sede", properties.get(0).getAttribute("l").getValue());                       // firenze
        userProperties.put("descrizione", properties.get(0).getAttribute("descrizStruttura").getValue()); //Operation & ...
        userProperties.put("telefono", properties.get(0).getAttribute("mobile").getValue());
        return userProperties;

    }

    /**
     * Una volta che ho accertato l'indentità dell'utente con uno dei metodi legati a LDAP, devo accertarmi
     * che tale utente sia abilitato alla applicazione e, nel caso lo sia, reperire tutti i dati di
     * profilazione che mi servono al funzionamento. Per farlo ho la necessità di connettermi ad un
     * database per l'applicazione in cui i dati sono gestiti da me stesso. Per farlo, occorre creare
     * un webservice, tipicamente una server page in php o asp, che prende i dati dal database su richiesta
     * dell'applicazione e me li fornisce in formato JSON. Altre soluzione sono valide, sta alla fantasia,
     * alle risorse, e esigenze dello sviluppatore.
     */
    private void serviceProfileFromUID(HashMap<String, String> userInfo){
       /* perchè non metti tutti nei metodi LDAP ? Facendo così usi due intent e sembra di giocare a
        * ping pong ! Vero, ma preferisco tenere separata la profilazione utente, poichè varia di volta
        * in volta. Inoltre trovo che in un diagramma di flusso sia concettualmente più chiaro cosa
        * avviene. In ultimo, anche se più complesso, trovo il codice maggiormente leggibile. Infatti
        * se devo apportare modifiche alla autenticazione locale, devo ricordarmi di farlo poi per
        * entrambi i metodi LDAP. Tenerlo separato invece, lo rende più facile da manutere*/
        String matricola = userInfo.get("matricola");
        Intent localIntent = new Intent(getPackageName()+"."+ IntranetServices.PROFILING);
        try{

            String esito = getRequest("https://" + SERVER + PATH + DIRSERVICES +WebServices.PROFILING + "?m=" + matricola);
            if( !esito.isEmpty() ){
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.USERINFO), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                //salvo gli attributi da LDAP...
                editor.putString("matricola", matricola);
                editor.putString("nome", userInfo.get("nome"));
                editor.putString("struttura", userInfo.get("struttura"));
                editor.putString("bacino", userInfo.get("bacino"));
                editor.putString("sede", userInfo.get("sede"));
                editor.putString("descrizione", userInfo.get("descrizione"));
                //... e quelli dalla profilazione locale. in questa app, alcuni sono duplicati delle informazioni di
                // LDAP ma preferisco tenerli così per una migliore manutenzione su DB locale
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc       = db.parse(new ByteArrayInputStream( esito.getBytes("UTF-8") ));
                Element node       = doc.getDocumentElement(); // matricola unica, nodo unico ! user è root xml
                String profilo     = node.getElementsByTagName("profilo").item(0).getTextContent().trim();
                String indirizzo   = node.getElementsByTagName("indirizzo").item(0).getTextContent().trim();
                String citta       = node.getElementsByTagName("citta").item(0).getTextContent().trim();
                String settore     = node.getElementsByTagName("struttura").item(0).getTextContent().trim();
                editor.putString("profilo", profilo );
                editor.putString("settore", settore );
                editor.putString("citta", citta );
                editor.putString("indirizzo", indirizzo );
                editor.apply();
                localIntent.putExtra("esito", true);
            }else{
                localIntent.putExtra("esito", false);
            }
            localIntent.putExtra("networkError", false);
        }catch (Exception e){
            e.printStackTrace();
            localIntent.putExtra("networkError", true);
        }finally {
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }
    }

    private void serviceNull(){

        throw new UnsupportedOperationException("Servizio non implementato");

    }
}
