package it.telecomitalia.my.aiutami;

import org.w3c.dom.Element;

public class Category {

    private String name;
    private String color;

    public Category(Element xml){

        name  = xml.getElementsByTagName("name").item(0).getTextContent();
        color = xml.getElementsByTagName("color").item(0).getTextContent();

    }

    public String getName(){
        return name;
    }

    public String getColor(){
        return color;
    }
}
