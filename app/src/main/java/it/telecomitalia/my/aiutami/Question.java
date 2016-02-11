package it.telecomitalia.my.aiutami;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.ArrayList;

/**
 * Classe che definisce un oggetto che rappresenta una domanda all'interno della lista
 * visualizzata per una determinata categoria.
 */
public class Question {

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

    @SuppressWarnings("unchecked")
    public ArrayList<Category> getCategories(){
        //The code splits the string on a delimiter defined as: zero or more whitespace, a literal comma, zero or more
        // whitespace which will place the words into the list and collapse any whitespace between the words and commas.
        //return Arrays.asList(strCat.split("\\s*,\\s*"));
        ArrayList<Category> list = new ArrayList<>();

            for (int i = 0; i < catNodes.getLength(); i++) {
                list.add( new Category( (Element)catNodes.item(i) ));
            }


        return list;

    }

}
