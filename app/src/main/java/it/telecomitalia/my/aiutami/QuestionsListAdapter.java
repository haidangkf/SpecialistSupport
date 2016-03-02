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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Controller per tutte le view che mandano a schermo una lista di domande, siano esse activity o
 * fragment creati dal drawer.
 */
public class QuestionsListAdapter extends RecyclerView.Adapter<QuestionsListAdapter.ViewHolder> {

    private ArrayList<Question> list;
    private Activity activity;
    private int color;

    /**
     * Costruttore utilizzato per colorare la toolbar con il colore della categoria a cui
     * appartengono le domande. Viene utilizzato nella QuestionsListActivity, per evidenziare
     * che le domande hanno tutte un denominatore comune
     * @param activity riferimento alla activity
     * @param color colore della categoria
     * @param list lista di dati
     */
    public QuestionsListAdapter(Activity activity, int color, ArrayList<Question> list){

        if(list!=null) Collections.sort(list);
        this.list     = list;
        this.activity = activity;
        this.color    = color;

    }

    @Override
    public QuestionsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View view = inflater.inflate(R.layout.question_element, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(QuestionsListAdapter.ViewHolder viewHolder, int position) {

        // titolo della domanda. e fin qui niente si strano :D
        viewHolder.domanda.setText( list.get(position).getDomanda() );

        // Sinossi della risposta, se presente. Altrimenti visualizzo solo data in cui è stata
        // posta la domanda e una evidenza del fatto che tale risposta manca.
        // Ricordati che tale data viene da XML ed è quella più recente del processo
        // ( domanda, o data della risposta, o data di una modifica ) e che anche la sinossi è
        // generata a partire dalla risposta inserita.
        boolean hasAnswer = !list.get(position).getSinossi().equals("");
        String sinossi = hasAnswer ?
                                list.get(position).getData() + " - " + list.get(position).getSinossi():
                                list.get(position).getData() + " - In attesa di risposta." ;
        SpannableString sColored = new SpannableString( sinossi );
        if( !hasAnswer ){
            sColored.setSpan( new ForegroundColorSpan( ContextCompat.getColor(activity, R.color.primario_2) ),
                    sinossi.indexOf("-")+1,
                    sinossi.length(), 0
            );
        }
        viewHolder.sinossi.setText( sColored );

        // Numero delle valutazioni alla risposta e immagine identificativa:
        // 0 - se c'è una risposta, ma non un voto
        // 1 - se manca la risposta ( quindi non è possibile votare )
        // 2 - ci sono voti e risposta, si spera il caso preponderante.
        Drawable star = ContextCompat.getDrawable(activity, R.drawable.ic_voto);
        if( list.get(position).getVoti()==0 ){
            // nei primi due casi evidenzio la cosa. mutate() importante, altrimenti cambiano TUTTE
            DrawableCompat.setTint( DrawableCompat.wrap(star).mutate(), ContextCompat.getColor(activity, R.color.grey_white) );
        }
        viewHolder.voti.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, star);
        viewHolder.voti.setText( String.valueOf( list.get(position).getVoti() ) );
        // categorie a cui appartiene la coppia domanda/risposta. una categoria esiste di sicuro
        // ed è quella inserita da utente alla creazione della domanda. Altre, possono essere state
        // inserite in fase di editing o di risposta. Le categorie sono presentate in label con il
        // colore che le identifica
        ArrayList<Category> categories = list.get(position).getCategories();
        for( Category c : categories){
            // per ogni elemento, crea textview con style associato chipText
            TextView t = new TextView(activity, null, R.attr.myChipStyle);
            t.setText(c.getName());
            // prendo il drawable
            Drawable ball = ContextCompat.getDrawable(activity.getBaseContext(), R.drawable.ball);
            // gli cambio colore in base al colore della categoria ( wrap serve su kitkat, su altri basta il drawable )
            //DrawableCompat.setTint(ball.mutate(), Color.parseColor(c.getColor()));
            DrawableCompat.setTint( DrawableCompat.wrap(ball).mutate(), Color.parseColor(c.getColor()) );
            // e lo piazzo alla sinistra del testo
            t.setCompoundDrawablesRelativeWithIntrinsicBounds(ball, null, null, null);
            // metto i margini. non è possibile inserirli in uno stile.
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            param.setMargins(0, 0, 10, 0); // left, top, right, bottom
            t.setLayoutParams(param);
            // ed infine aggiungo la view
            viewHolder.categories.addView(t);
        }

    }

    @Override
    public int getItemCount() {

        return list!=null ? list.size() : 0 ;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView domanda;
        TextView voti;
        TextView sinossi;
        LinearLayout categories;

        public ViewHolder( View itemView) {

            super(itemView);
            domanda     = (TextView)itemView.findViewById(R.id.domanda);
            sinossi     = (TextView)itemView.findViewById(R.id.risposta);
            voti        = (TextView)itemView.findViewById(R.id.n_voti);
            categories  = (LinearLayout)itemView.findViewById(R.id.categories);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(activity, QuestionActivity.class);
                    intent.putExtra("color", color);
                    intent.putExtra("title", "Titolo Domanda");
                    activity.startActivity(intent);

                }
            });
        }

    }
}
