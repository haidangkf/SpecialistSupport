package it.telecomitalia.my.aiutami;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CategoriesAdapter extends BaseAdapter {

    private Context context;
    private String[] obj;

    private class ViewHolder {

        public TextView text;

        public ViewHolder(View view){
            text = (TextView) view.findViewById(R.id.textView);
        }
    }

    public CategoriesAdapter(Context _context, String[] _obj) {
        super();
        context = _context;
        obj = _obj;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent){

        ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if( rowView == null) {
            rowView = inflater.inflate(R.layout.category_element, parent, false);
            holder = new ViewHolder(rowView);
            rowView.setTag(holder);
        }else{
            holder = (ViewHolder) rowView.getTag();
        }

        rowView.setSelected( false );

        String capitalized = obj[position].substring(0, 1).toUpperCase() + obj[position].substring(1);
        holder.text.setText( capitalized );
        return rowView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public Object getItem(int arg0) {
        return obj[arg0];
    }

    @Override
    public int getCount() {
        return obj.length;
    }

}
