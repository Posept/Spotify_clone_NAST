package com.nast.spotify.model;

import android.graphics.Bitmap;

public class ArtistSearch {

    private String name;
    private Bitmap image;

    public ArtistSearch(String name, Bitmap img) {
        this.name = name;
        this.image = img;
    }
    public String getName() {return name;}

    public Bitmap getImage() {return image;}
}
