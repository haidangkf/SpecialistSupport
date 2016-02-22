/*******************************************************************************
 * This software is distributed under the following BSD license:
 *
 * Copyright (c) 2016, Marco Paoletti <mpao@me.com>, http://mpao.github.io
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLReader {

    private DocumentBuilder db;

    /**
     * Costruttore di default
     * @throws GodzillioniDiXMLExceptions
     */
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
    public ArrayList<Object> getObjectsList(Document document, Class<?> classType)
            throws GodzillioniDiXMLExceptions {

        try {

            ArrayList<Object> list = new ArrayList<>();
            NodeList nodes = document.getDocumentElement().getChildNodes();
            Constructor c = classType.getConstructor(Element.class);
            for (int i = 0; i < nodes.getLength(); i++) {
                list.add(c.newInstance( nodes.item(i) ));
            }
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
