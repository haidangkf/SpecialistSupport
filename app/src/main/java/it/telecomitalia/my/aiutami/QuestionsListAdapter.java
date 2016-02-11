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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Controller per QuestionsListActivity
 */
public class QuestionsListAdapter extends RecyclerView.Adapter<QuestionsListAdapter.ViewHolder> {

    private ArrayList<Question> list;
    private Activity activity;
    private int color;

    public QuestionsListAdapter(Activity activity, int color, ArrayList<Question> list){

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

        viewHolder.domanda.setText( list.get(position).getDomanda() );
        viewHolder.voti.setText( String.valueOf( list.get(position).getVoti() ) );
        String sinossi = list.get(position).getData() + " - " + list.get(position).getSinossi();
        viewHolder.sinossi.setText( sinossi );
        ArrayList<Category> categories = list.get(position).getCategories();
        for( Category c : categories){
            TextView t = new TextView(activity, null, R.style.chipText);
            t.setText(c.getName());
            viewHolder.categories.addView(t);
        }

    }

    @Override
    public int getItemCount() {

        return list.size();

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
