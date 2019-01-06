package com.rustero.units;


import com.rustero.tools.Tools;


public class FilmProfile implements Comparable<FilmProfile> {
    public String name;
    public int width, height, bitrate;
    public boolean chosen;

    public FilmProfile() {
        width = 640;
        height = 480;
        bitrate = 1000;
    }


    @Override
    public int compareTo(FilmProfile o) {
        int result = 0;
        if (o.height < height)
            result = 1;
        else if (o.height > height)
            result = -1;
        else if (o.width < width)
            result = 1;
        else if (o.width > width)
            result = -1;
        return result;
    }


    void copy(FilmProfile aFrom) {
        name = aFrom.name;
        width = aFrom.width;
        height = aFrom.height;
        bitrate = aFrom.bitrate;
        chosen = aFrom.chosen;
    }


    public String blendResolution() {
        return width + "x" + height;
    }


    public void parseResolution(String aReso) {
        int i = aReso.indexOf("x");
        if (i < 0) return;
        String ws = aReso.substring(0, i);
        String hs = aReso.substring(i+1);
        int wi = Tools.parseInt(ws);
        if (wi > 0)
            width = wi;
        int he = Tools.parseInt(hs);
        if (he > 0)
            height = he;
    }

}
