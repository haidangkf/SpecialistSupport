package it.telecomitalia.my.aiutami;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private static OnItemClickListener listener;

    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View view = inflater.inflate(R.layout.category_element, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(CategoriesAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        viewHolder.text1.setText("Bacon");
        viewHolder.text2.setText("Bacon ipsum dolor amet pork belly meatball kevin spare ribs. Frankfurter swine corned beef meatloaf, strip steak.");
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener _listener) {
        listener = _listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text1;
        TextView text2;

        public ViewHolder(final View itemView) {

            super(itemView);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null)
                        listener.onItemClick( itemView, getLayoutPosition());
                }
            });
        }
    }

}
