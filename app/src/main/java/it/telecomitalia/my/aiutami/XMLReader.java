package it.telecomitalia.my.aiutami;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLReader {

    private DocumentBuilder db;

    public XMLReader() throws GodzillioniDiXMLExceptions{

        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }catch (ParserConfigurationException e){
            throw new GodzillioniDiXMLExceptions();
        }
    }

    /**
     * Metodo in overload per ottenere oggetto Document a partire da una stringa XML, tipicamente
     * un webservices
     * @param xml Stringa XML da cui partire che verrà converita in InputStream
     * @return oggetto Document
     * @throws GodzillioniDiXMLExceptions
     */
    @SuppressWarnings("unused")
    public Document getXMLData(String xml) throws GodzillioniDiXMLExceptions{

        try {
            return getXMLData(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        }catch (UnsupportedEncodingException e){
            throw new GodzillioniDiXMLExceptions();
        }

    }

    /**
     * Metodo per ottenere oggetto Document
     * @param is InputStream XML da cui partire, tipicamente un file xml
     * @return oggetto Document
     * @throws GodzillioniDiXMLExceptions
     */
    public Document getXMLData(InputStream is) throws GodzillioniDiXMLExceptions {

        try {
            return db.parse(is);
        }catch (IOException | SAXException e){
            throw new GodzillioniDiXMLExceptions();
        }

    }

    /**
     * Metodo per ricavare la lista degli oggetti indicati come parametro, all'interno del file xml.
     * è importante che la classe che si vuole utilizzare come tipo per gli elementi della List,
     * abbia un costruttore che accetta come parametro un oggetto org.w3c.dom.Element, altrimenti viene lanciata
     * una NoSuchMethodException attraverso la classe GodzillioniDiXMLExceptions
     * @param document Document creato attraverso il metodo getXMLData
     * @param classType Classe degli oggetti che popoleranno la List
     * @return List con elementi di classe Object
     * @throws GodzillioniDiXMLExceptions
     */
    public List<Object> getObjectsList(Document document, Class<?> classType)
            throws GodzillioniDiXMLExceptions {

        try {

            List<Object> list = new ArrayList<>();
            NodeList nodes = document.getDocumentElement().getChildNodes();
            Constructor c = classType.getConstructor(Element.class);
            for (int i = 0; i < nodes.getLength(); i++) {
                list.add(c.newInstance( nodes.item(i) ));
            }
            //non sono riuscito ad utilizzare i generics :( todo
            return list;

        }catch (NoSuchMethodException e){
            Log.e("NoSuchMethodException",
                    "La classe deve implementare almeno un costruttore che abbia come parametro un oggetto Element");
            throw new GodzillioniDiXMLExceptions();
        }catch (Exception e){
            throw new GodzillioniDiXMLExceptions();
        }

    }

    /**
     * XML ha davvero troppe troppe eccezioni. Con questa classe, tento di
     * raccogliere tutte quelle che non mi interessa catturare nel normale flusso di esecuzione
     */
    public class GodzillioniDiXMLExceptions extends Exception{

        public GodzillioniDiXMLExceptions(){
            super();
        }

    }

}
