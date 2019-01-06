package com.rustero.units;


public class FilmItem implements Comparable<FilmItem> {


    public long size;
    public String name, ext, folder, bytes, date, path;
    public FilmMeta meta;


    public int compareTo(FilmItem aFiit) {
        if (this.name != null)
            return this.name.toLowerCase().compareTo(aFiit.name.toLowerCase());
        else
            throw new IllegalArgumentException();
    }


}

