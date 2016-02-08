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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Classe per definire come viene visualizzata la lista delle categorie
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private ArrayList<Category> list;
    private Activity activity;

    /**
     * Costruttore
     * @param activity Activity che richiede la lista
     * @param list Oggetto contentente il risultato della richiesta al webservice
     */
    public CategoriesAdapter(Activity activity, ArrayList<Category> list){

        this.list    = list;
        this.activity = activity;

    }

    /**
     * Definizione del layout di un singolo elemento della lista
     * @param parent genitore della View
     * @param viewType tipo di View
     * @return ViewHolder
     */
    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View view = inflater.inflate(R.layout.category_element, parent, false);
        return new ViewHolder(view);

    }

    /**
     * Metodo per legare il Model con il Controller( questo Adapter )
     * @param viewHolder ViewHolder definito nell'adapter
     * @param position posizione dell'elemento nella lista
     */
    @Override
    public void onBindViewHolder(CategoriesAdapter.ViewHolder viewHolder, int position) {

        viewHolder.titleText = ( list.get(position) ).getName();
        viewHolder.color     = Color.parseColor( ( list.get(position) ).getColor());
        viewHolder.title.setText( viewHolder.titleText );
        viewHolder.background.setBackgroundColor( viewHolder.color );

    }

    /**
     * Metodo che definisce la lunghezza della lista di oggetti
     * @return lunghezza della lista
     */
    @Override
    public int getItemCount() {

        return list!=null ? list.size() : 0 ;

    }

    /**
     * Classe interna che fa da modello per i dati
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView background;
        int color;
        String titleText;

        public ViewHolder( View itemView) {

            super(itemView);
            title      = (TextView) itemView.findViewById(R.id.cat_title);
            background = (ImageView) itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(activity, QuestionsListActivity.class);
                    intent.putExtra("color", color);
                    intent.putExtra("title", titleText);
                    activity.startActivity(intent);

                }
            });
        }

    }

}
