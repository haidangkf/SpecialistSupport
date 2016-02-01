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

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<Object> list;
    Activity activity;

    public CategoriesAdapter(Activity activity, List<Object> list){

        this.list    = list;
        this.activity = activity;
    }

    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View view = inflater.inflate(R.layout.category_element, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(CategoriesAdapter.ViewHolder viewHolder, int position) {

        viewHolder.title.setText( ((Category)list.get(position)).getName() );
        viewHolder.background.setBackgroundColor(
                Color.parseColor(
                        ((Category)list.get(position)).getColor()
                )
        );

    }

    @Override
    public int getItemCount() {

        return list.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView background;

        public ViewHolder( View itemView) {

            super(itemView);
            title      = (TextView) itemView.findViewById(R.id.cat_title);
            background = (ImageView) itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activity.startActivity( new Intent(activity, QuestionList.class) );

                }
            });
        }

    }

}
