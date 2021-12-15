package com.nast.spotify.model;

import java.util.List;

public class AlbumNew {
    private String title;
    private String img_url;
    private List<String> artists;

    public AlbumNew(String title,String img_url,List<String> artists) {
        this.title = title;
        this.img_url = img_url;
        this.artists= artists;
    }

    public String getTitle() {return title;}

    public String getImg_url() {return img_url;}

    public List<String> getArtists() {return  artists;}
}
