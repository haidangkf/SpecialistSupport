package it.telecomitalia.my.aiutami;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class QuestionsListAdapter extends RecyclerView.Adapter<QuestionsListAdapter.ViewHolder> {

    private Activity activity;
    private int color;

    public QuestionsListAdapter(Activity activity, int color){

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

        //viewHolder.imageView.settint

    }

    @Override
    public int getItemCount() {

        return 40;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder( View itemView) {

            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image);
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
