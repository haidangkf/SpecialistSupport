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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;

public class IntranetReceiver extends BroadcastReceiver {

    private Activity activity;
    public static HashMap<String, String> userInfo2;

    public IntranetReceiver(Activity _activity){
        activity = _activity;
    }

    @SuppressWarnings("unchecked")
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction().replace(context.getPackageName() + ".", "");
        switch (action) {
            case IntranetServices.UPDATE:
                /* una volta terminato il download, attraverso un intent lancio il file che ho
                 * appena scaricato in modo da installare immediatamente l'aggiornamento come
                 * specificato qui
                 * http://stackoverflow.com/questions/4967669/android-install-apk-programmatically
                 * Avrei potuto inserire questo codice nel servizio, ma per mantenere il
                 * pattern delle operazioni, lo ho inserito qui, */
                if (intent.getStringExtra("apk") != null) {
                    String path = intent.getStringExtra("apk");
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(i);
                }
                break;

            case IntranetServices.PROFILING:
                ((Welcome) activity).loginResult( intent.getBooleanExtra("esito",false), intent.getBooleanExtra("networkError",false) );
                break;

            case IntranetServices.AUTH:
                HashMap<String, String> userInfo = (HashMap<String, String>)intent.getSerializableExtra("userProperties");
                ((Welcome) activity).loginResult(userInfo != null, userInfo);
                break;

            case IntranetServices.SEARCH:
                userInfo2 = (HashMap<String, String>)intent.getSerializableExtra("userProperties");
                activity.findViewById(R.id.cambiaUtente).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.autoLogin).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.loadingPanelPhone).setVisibility(View.GONE);
                if( userInfo2!=null )
                    ((TextView)activity.findViewById(R.id.nomeCognome)).setText(userInfo2.get("nome"));
                else
                    // una view a caso, tanto non mi serve
                    ((Welcome) activity).cambiaUtente( activity.findViewById(R.id.imageView2) );
                break;
        }
    }

    public void InizializzaReceivers(){
        IntranetReceiver rec = new IntranetReceiver(activity);
        IntentFilter intentFilter;
        //todo receiver per aggiornamenti ** IMPORTANTE ** commentare e disattivare se in OAStore
        intentFilter = new IntentFilter( activity.getPackageName()+"."+ IntranetServices.UPDATE );
        LocalBroadcastManager.getInstance(activity).registerReceiver(rec, intentFilter);
        // receiver per ricerca numero di telefono
        intentFilter = new IntentFilter( activity.getPackageName()+"."+IntranetServices.SEARCH );
        LocalBroadcastManager.getInstance(activity).registerReceiver(rec, intentFilter);
        // receiver per ricerca con matricola e password
        intentFilter = new IntentFilter( activity.getPackageName()+"."+IntranetServices.AUTH );
        LocalBroadcastManager.getInstance(activity).registerReceiver(rec, intentFilter);
        // receiver per la profilazione della matricola
        intentFilter = new IntentFilter( activity.getPackageName()+"."+IntranetServices.PROFILING );
        LocalBroadcastManager.getInstance(activity).registerReceiver(rec, intentFilter);
    }
}
