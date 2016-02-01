package it.telecomitalia.my.aiutami;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public class HomePageFragment extends Fragment {

    AppCompatActivity activity;

    public HomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity)getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout     = inflater.inflate(R.layout.f_home_page, container, false);
        RecyclerView rv = (RecyclerView) layout.findViewById(R.id.recyclerview);

        //todo da webserver o salvataggio locale
        String xml = "<categories>" +
                "<category>" +
                "<name>Topolino</name>" +
                "<color>#dcc89b</color>" +
                "<important>false</important>" +
                "<image/>" +
                "<elements>8</elements>" +
                "</category>" +

                "<category>" +
                "<name>Paperino</name>" +
                "<color>#b4d28c</color>" +
                "<important>false</important>" +
                "<image/>" +
                "<elements>7</elements>" +
                "</category>" +

                "<category>" +
                "<name>Pluto</name>" +
                "<color>#ff825a</color>" +
                "<important>false</important>" +
                "<image/>" +
                "<elements>6</elements>" +
                "</category>" +

                "<category>" +
                "<name>Pippo</name>" +
                "<color>#aaa5c8</color>" +
                "<important>false</important>" +
                "<image/>" +
                "<elements>18</elements>" +
                "</category>" +

                "<category>" +
                "<name>Altra categoria</name>" +
                "<color>#dc8200</color>" +
                "<important>false</important>" +
                "<image/>" +
                "<elements>2</elements>" +
                "</category>" +

                "</categories>";

        try {

            XMLReader x     = new XMLReader();
            List<Object> l  = x.getObjectsList(x.getXMLData(xml), Category.class);
            rv.setLayoutManager(new LinearLayoutManager(activity));
            rv.setAdapter(new CategoriesAdapter(activity, l));

        }catch (XMLReader.GodzillioniDiXMLExceptions e){
            e.printStackTrace();
        }

        return layout;
    }

}
