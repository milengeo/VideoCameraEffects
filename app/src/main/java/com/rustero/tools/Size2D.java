package com.rustero.tools;




public final class Size2D implements Comparable<Size2D> {
    public int x, y;



	public Size2D() {
	}


	public Size2D(int aWidth, int aHeight) {
		x = aWidth;
		y = aHeight;
	}


	public Size2D(Size2D other) {
		x = other.x;
		y = other.y;
	}


	public boolean isZero() {
		if (0 == x) return true;
		if (0 == y) return true;
		return false;
	}


	public boolean isPortrait() {
		if (y > x)
			return true;
		else
			return false;
	}


	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof Size2D) {
			Size2D other = (Size2D) obj;
			return x == other.x && y == y;
		}
		return false;
	}


    @Override
    public int compareTo(Size2D o) {
		if (o == null) return 0;
        int result = 0;
        if (o.y < y)
            result = 1;
        else if (o.y > y)
            result = -1;
        else if (o.x < x)
            result = 1;
        else if (o.x > x)
            result = -1;
        return result;
    }


    public String toText() {
        String result = x + "x" + y;
        return result;
    }




    static public Size2D parseText(String aResolution) {
        String wi="", he="";
        Size2D result = new Size2D(0, 0);
        int p = aResolution.indexOf("x");
        if (p > 0) {
            wi = aResolution.substring(0, p);
            he = aResolution.substring(p+1);
            try {
                result.x = Integer.parseInt(wi);
                result.y = Integer.parseInt(he);
            } catch (Exception ex) {} ;
        }
        return result;
    }



    public boolean isAbove(int aWidth, int aHeight) {
        if (x > aWidth) return true;
        if (y > aHeight) return true;
        return false;
    }




}
