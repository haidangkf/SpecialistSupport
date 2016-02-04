package it.telecomitalia.my.aiutami;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Classe che raccoglie gli elementi comuni di ogni Activity della applicazione, tipicamente il
 * menu della Toolbar, le funzioni per il logout e così via. Ogni nuova Activity deve estendere
 * ElementsForEveryActivity per poter avere gli elementi necessari
 */
public abstract class ElementsForEveryActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logout(){

        getSharedPreferences(getString(R.string.USERINFO), Context.MODE_PRIVATE).edit().clear().apply();
        startActivity(new Intent(this, Welcome.class ));
        finish();

    }

    public void insertDefaultFab(){

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Inserisco una nuova domanda\no chiedo supporto allo specialist", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
