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
import android.content.Intent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import java.util.ArrayList;

public class ApplicationServices extends IntentService {

    public static final String GETCATEGORIES = "it.telecomitalia.my.aiutami.getCategories";

    public ApplicationServices() {
        super("ApplicationServices");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String service = intent.getStringExtra("application");
        switch (service) {

            case GETCATEGORIES : this.getCategories(); break;
            default:
                applicationNull(service);

        }
    }

    private void applicationNull(String service){

        throw new UnsupportedOperationException("Servizio non implementato: "+service);

    }

    @SuppressWarnings("unchecked")
    private void getCategories() {

        OkHttpClient client  = new OkHttpClient();
        String url = IntranetServices.getWebservicesURL() + "categories.xml";
        Intent localIntent = new Intent(GETCATEGORIES);
        ArrayList<Category> list = null;
        try {
            // prendo i dati dal webserver
            IntranetServices.enableSSL(client, getAssets().open(IntranetServices.CERTNAME));
            Request request = new Request.Builder().url(url).build();
            String response = client.newCall(request).execute().body().string();
            response = response.replace("\r\n","").replace("    ","");
            // caching ?
            //FileOutputStream outputStream = openFileOutput("categories", Context.MODE_PRIVATE);
            //outputStream.write(response.getBytes());
            //outputStream.close();
            // tutto è andato bene, provo a fare il parse XML direttamente dello stream
            XMLReader x = new XMLReader();
            list = (ArrayList<Category>)(Object)x.getObjectsList(x.getXMLData(response), Category.class);
            // andata anche con XML, si mette a disposizione la lista
            localIntent.putExtra("categories", list);

        } catch (Exception e) {
            localIntent.putExtra("categories", list);

        } finally {
            sendBroadcast(localIntent);
        }

    }
}
