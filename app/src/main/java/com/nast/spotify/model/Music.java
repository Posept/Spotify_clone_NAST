package com.nast.spotify.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {
    private String id;
    private String url;
    private String title;
    private String album;
    private String album_image;
    private long duration;
    private String artist;
    private String artist_id;
    private boolean playing;

    public Music(String i, String u, String t, String a, String a_img, long duration, String artist, String artist_id) {
        this.id = i;
        this.url = u;
        this.title = t;
        this.album = a;
        this.album_image = a_img;
        this.duration = duration;
        this.artist = artist;
        this.artist_id = artist_id;
        playing = false;
    }
    private Music(Parcel in) {
        url = in.readString();
        title = in.readString();
        album = in.readString();
        album_image = in.readString();
        duration = in.readLong();
        artist = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            return new Music(source);
        }
        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }

    };

    public String getArtist_id() {
        return artist_id;
    }

    public String getId() { return id; }

    public String getUri() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbum_image() {
        return album_image;
    }

    public long getDuration() {
        return duration;
    }

    public String getArtist() {
        return artist;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(title);
        parcel.writeString(album);
        parcel.writeString(album_image);
        parcel.writeLong(duration);
        parcel.writeString(artist);
    }
}
