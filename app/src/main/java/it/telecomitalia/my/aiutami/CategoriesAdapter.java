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

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private ArrayList<Category> list;
    private Activity activity;

    public CategoriesAdapter(Activity activity, ArrayList<Category> list){

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

        viewHolder.titleText = ( list.get(position) ).getName();
        viewHolder.color     = Color.parseColor( ( list.get(position) ).getColor());
        viewHolder.title.setText( viewHolder.titleText );
        viewHolder.background.setBackgroundColor( viewHolder.color );

    }

    @Override
    public int getItemCount() {

        return list!=null ? list.size() : 0 ;

    }

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
