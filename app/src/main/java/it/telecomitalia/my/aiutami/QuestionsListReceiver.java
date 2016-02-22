package it.telecomitalia.my.aiutami;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

/**
 * Receiver per tutte le componenti della applicazione che mostrano o elaborano
 * una lista di domande, sempre con lo stesso layout e le stesse informazioni, ma con
 * filtri differenti ( mie domande, categorie, preferiti... etc )
 */
public class QuestionsListReceiver extends BroadcastReceiver {

    private Activity activity;
    private SwipeRefreshLayout reference;

    /**
     * costruttore per receiver all'interno di una activity
     * @param activity context in cui viene creato il receiver
     */
    public QuestionsListReceiver(Activity activity){

        this(activity, null);

    }

    /**
     * costruttore per receiver all'interno di un fragment
     * @param activity activity che contiene il fragment
     * @param fragment riferimento al fragment creato da onCreateView
     */
    public QuestionsListReceiver(Activity activity, View fragment){

        this.activity = activity;
        if(fragment==null)
            reference = (SwipeRefreshLayout) activity.findViewById(R.id.swipeContainer);
        else
            reference = (SwipeRefreshLayout) fragment.findViewById(R.id.swipeContainer);

    }

    /**
     * Il receiver è in grado di intercettare tutti gli intent che vengono creati dal servizio
     * ApplicationService riguardo alle liste di domande.
     * @param context context di esecuzione
     * @param intent intent intercettato contenente la tipologia richiesta ed i dati
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(Context context, Intent intent) {

        String filter = null;
        // sono gli attributi di ApplicationService che riguardano parti dell'applicazione
        // che interessano le domande.
        for(String description : ApplicationServices.QUESTIONS_INTENT) {
            if (intent.hasExtra(description)) {
                filter = description;
                break;
            }
        }

        ArrayList<Question> list = (ArrayList<Question>)intent.getSerializableExtra(filter);
        drawList(list);
        // disegno la lista in ogni caso. se è null e non esiste salvataggio
        // apparirà scermata bianca, altrimenti la lista. Avviso che qualcosa
        // non è andato bene.
        if( intent.getBooleanExtra("isCached", false)  ){
            // se i dati arrivano dalla cache
            Snackbar.make(reference, activity.getResources().getString(R.string.error_refresh), Snackbar.LENGTH_LONG).show();
        }
        // se list è ancora null, o non ha elementi, metto una immagine per mandare il messaggio di
        // "contenitore vuoto". Al pull to refresh, se la situazione cambia, l'immagine scompare
        if( list==null || list.size()==0  ){
            reference.setBackground(ContextCompat.getDrawable(activity, R.drawable.background_null));
        }else{
            reference.setBackground(null);
        }
    }

    /**
     * Metodo per disegnare la lista di elementi all'interno della
     * RecyclerView definita nel layout. Viene eseguito quando activity
     * viene visualizzata, oppure quando occorre aggiornare la lista
     * @param list elementi racchiusi in una lista
     */
    public void drawList(ArrayList<Question> list){

        drawList(list, null);

    }

    /**
     * Metodo per disegnare la lista all'interno di un fragment, analogo al metodo
     * in overload utilizzato nelle Activity
     * @param list elementi racchiusi in una lista
     * @param layout riferimento al fragment
     */
    public void drawList(ArrayList<Question> list, View layout){

        RecyclerView rv;
        int color = activity.getIntent().getIntExtra("color", R.color.primario_1);
        if( layout == null ) {
            rv = (RecyclerView) activity.findViewById(R.id.recyclerview);
        }else {
            rv = (RecyclerView) layout.findViewById(R.id.recyclerview);
        }

        rv.setLayoutManager(new LinearLayoutManager(activity));
        rv.setAdapter( new QuestionsListAdapter(activity, color, list) );
        reference.setRefreshing(false);

    }

}
