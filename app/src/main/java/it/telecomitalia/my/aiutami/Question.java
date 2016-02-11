package it.telecomitalia.my.aiutami;

import android.support.annotation.NonNull;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.ArrayList;

/**
 * Classe che definisce un oggetto che rappresenta una domanda all'interno della lista
 * visualizzata per una determinata categoria.
 */
public class Question implements Comparable<Question>{

    private String domanda;
    private int voti;
    private String data;
    private String sinossi;
    private NodeList catNodes;

    public Question(Element xml){

        domanda     = xml.getElementsByTagName("domanda").item(0).getTextContent();
        voti        = Integer.parseInt(xml.getElementsByTagName("voti").item(0).getTextContent());
        data        = xml.getElementsByTagName("data").item(0).getTextContent();
        sinossi     = xml.getElementsByTagName("sinossi").item(0).getTextContent();
        catNodes    = xml.getElementsByTagName("categories").item(0).getChildNodes();

    }

    /**
     * La classe implementa Comparable per ordinare gli elementi in base al tag XML "voti".
     * In questo modo, le risposte con più ritorni positivi, vengono messe in cima alla lista
     * @param a Oggetto da confrontare
     * @return int per ordinamento
     */
    @Override
    public int compareTo(@NonNull Question a) {
        return a.voti - this.voti;
    }

    public String getDomanda(){
        return domanda;
    }

    public int getVoti(){
        return voti;
    }

    public String getData(){
        // todo definire il formato e quindi anche l'oggetto
        return data;
    }

    public String getSinossi(){
        return sinossi;
    }

    public ArrayList<Category> getCategories(){

        ArrayList<Category> list = new ArrayList<>();
        for (int i = 0; i < catNodes.getLength(); i++) {
            list.add( new Category( (Element)catNodes.item(i) ));
        }
        return list;

    }

}
