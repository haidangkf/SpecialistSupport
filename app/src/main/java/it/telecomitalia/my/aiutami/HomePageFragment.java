package it.telecomitalia.my.aiutami;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public class HomePageFragment extends Fragment {

    AppCompatActivity activity;
    View layout;
    SwipeRefreshLayout refresh;
    ArrayList<Category> list;

    public HomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity)getActivity();

        sendIntentToService(ApplicationServices.GETCATEGORIES); // carica categorie da webservice

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(new MyLocalReceiver(), new IntentFilter(ApplicationServices.GETCATEGORIES));
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(new MyLocalReceiver());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.f_home_page, container, false);
        refresh = (SwipeRefreshLayout) layout.findViewById(R.id.swipeContainer);

        // appena creata la view, intanto che carico i dati, mostro il refresh
        refresh.post(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                sendIntentToService(ApplicationServices.GETCATEGORIES);

            }
        });
        refresh.setColorSchemeResources(R.color.primario_2);

        drawList(list);

        return layout;
    }

    public void drawList(ArrayList<Category> list){

        RecyclerView rv = (RecyclerView) layout.findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(activity));
        rv.setAdapter(new CategoriesAdapter(activity, list));
        refresh.setRefreshing(false);

    }

    private void sendIntentToService(String type){

        Intent i = new Intent(activity, ApplicationServices.class);
        i.putExtra("application", type);
        activity.startService(i);

    }

    public class MyLocalReceiver extends BroadcastReceiver {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {

            list = (ArrayList<Category>)intent.getSerializableExtra("categories");
            drawList(list);
            if ( isAdded() & list==null ){
                Snackbar.make(refresh, getResources().getString(R.string.error_refresh), Snackbar.LENGTH_LONG).show();
            }
        }

    }
}

