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

import android.support.annotation.NonNull;

import org.w3c.dom.Element;
import java.io.Serializable;

/**
 * Classe che definisce un oggetto che rappresenta una categoria. Implementa Serializable
 * per poter essere trasferita da una activity all'altra con un intent
 */
public class Category implements Serializable, Comparable<Category> {

    private String name;
    private String color;
    private boolean inEvidenza;
    private String descrizione;

    public Category(Element xml){

        name  = xml.getElementsByTagName("name").item(0).getTextContent();
        color = xml.getElementsByTagName("color").item(0).getTextContent();
        inEvidenza = Boolean.parseBoolean(xml.getElementsByTagName("inevidenza").item(0).getTextContent());
        descrizione = xml.getElementsByTagName("descrizione").item(0).getTextContent();

    }

    /**
     * La classe implementa Comparable per ordinare gli elementi in base al tag XML "inevidenza".
     * Quelli con tale tag impostato a true, vengono messi in cima alla lista
     * @param a Oggetto da confrontare
     * @return int per ordinamento
     */
    @Override
    public int compareTo(@NonNull Category a) {
        int propertyObjA = a.inEvidenza ? 1 : 0;
        int propertyObjB = this.inEvidenza ? 1 : 0;
        return propertyObjA - propertyObjB;
    }

    public String getName(){
        return name;
    }

    public String getColor(){
        return color;
    }

    public String getDescrizione(){
        return descrizione;
    }
}
