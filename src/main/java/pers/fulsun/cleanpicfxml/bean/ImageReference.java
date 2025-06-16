package pers.fulsun.cleanpicfxml.bean;

import lombok.Data;

@Data
public class ImageReference {
    String altText;
    String url;


    public ImageReference(String altText, String url) {
        this.altText = altText;
        this.url = url;
    }

}
