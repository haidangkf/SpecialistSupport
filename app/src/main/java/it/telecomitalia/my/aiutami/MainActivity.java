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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ElementsForEveryActivity implements NavigationView.OnNavigationItemSelectedListener{

    CollapsingToolbarLayout ctl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // definizione della toolbar e dei suoi movimenti
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ctl = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);

        // il FAB compie azione di default definita nella super classe ElementsForEveryActivity
        this.insertDefaultFab();

        // definizione comportamenti del drawer laterale
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // definizione per la navigazione nel menu laterale
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // viene inserito il fragment che fa da homepage alla applicazione
        HomePageFragment fragment = new HomePageFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();

        // cambio il titolo della Toolbar in base al tipo di fragment inserito
        ctl.setTitle("Categorie");
        // aggiorno le informazioni nel header del drawer, inserendo proprietà utente
        navigationHeaderinfo();
        // calcolo, e visualizzo eventuali notifiche nel drawer
        setAllMenuCounters();
    }

    @Override
    public void onBackPressed() {
        // comportamento "alla Feedly": se il drawer è chiuso, aprilo
        // per mostrarlo all'utente ed ad un secondo back, ovvero quando il
        // drawer è aperto, chiudi l'applicazione
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if ( !drawer.isDrawerOpen(GravityCompat.START) ) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        String title = null;
        if (id == R.id.nav_home) {
            title = "Categorie";
            fragment = new HomePageFragment();
        } else if (id == R.id.nav_search) {
        } else if (id == R.id.nav_favs) {
            title = item.getTitle().toString();
            fragment = QuestionsListFragment.newInstance(ApplicationServices.GETFAVOURITES);
        } else if (id == R.id.nav_notifications) {
        } else if (id == R.id.nav_question) {
            title = item.getTitle().toString();
            fragment = QuestionsListFragment.newInstance(ApplicationServices.GETMYQ);
        } else if (id == R.id.nav_answer) {
            title = item.getTitle().toString();
            fragment = QuestionsListFragment.newInstance(ApplicationServices.GETMYA);
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.cat_1) {

        } else if (id == R.id.cat_2) {

        }
        if(fragment!=null) {
            getFragmentManager().beginTransaction().replace(R.id.main_container, fragment).commit();
            ctl.setTitle(title);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Metodo per l'inserimento delle informazioni utente all'interno
     * del header del drawer.
     */
    public void navigationHeaderinfo(){

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.USERINFO), Context.MODE_PRIVATE);
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);

        TextView name     = (TextView)header.findViewById(R.id.nomeCognome);
        TextView details  = (TextView)header.findViewById(R.id.descrizione);
        ImageView img     = (ImageView)header.findViewById(R.id.profilo);

        if( sharedPref.contains("matricola") ){
            String profilo = sharedPref.getString("profilo", null);
            if( profilo!=null){
                if( profilo.equals("Specialist") ) img.setImageResource(R.drawable.ic_nav_user_spec);
                if( profilo.equals("User") ) img.setImageResource(R.drawable.ic_nav_user_normal);
                if( profilo.equals("Guest") ) img.setImageResource(R.drawable.ic_nav_user_resp);
            }

            name.setText( sharedPref.getString("nome", null) );
            details.setText( sharedPref.getString("bacino", null) );
        }

    }

    /**
     * Metodo che esegue per ogni voce il calcolo delle notifiche
     * e prepara l'interno menù della applicazione
     */
    private void setAllMenuCounters(){

        int test = (int)(Math.random() * ( 17 - 2 ));
        setMenuCounter(R.id.nav_notifications, test );

    }

    /**
     * Metodo che prepara esteticamente la visualizzazione delle notifiche
     * se presenti
     * @param itemId elemento del menu
     * @param count numero delle notifiche da visualizzare
     */
    private void setMenuCounter(@IdRes int itemId, int count) {

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        MenuItem menuItem = navigationView.getMenu().findItem(itemId);
        TextView view = (TextView) menuItem.getActionView();
        String value = null;
        if( count > 0 ){
            value = String.valueOf(count);
            menuItem.setIcon(R.drawable.ic_menu_notifications_alert);
        }
        view.setText(value);
    }
}
