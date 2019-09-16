package com.proyek.rahmanjai.eatit.Model;

/**
 * Created by rahmanjai on 01/04/2018.
 */

public class Adds {
    private String Image;

    public Adds() {
    }

    public Adds(String name, String image, String description, String price, String discount, String menuId) {
        Image = image;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

}
