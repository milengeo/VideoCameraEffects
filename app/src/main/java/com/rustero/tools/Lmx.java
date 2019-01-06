package com.rustero.tools;

public class Lmx {


    private StringBuffer mCode, mNode, mItem;


    public Lmx() {
        mCode = new StringBuffer();
        mNode = new StringBuffer();
        mItem = new StringBuffer();
    }


    public Lmx(String aCode) {
        mCode = new StringBuffer(aCode);
        mNode = new StringBuffer(aCode);
        mItem = new StringBuffer(aCode);
    }



    public String getCode() {
        if (mCode.length() == 0) {
            pushNode("node");
        }
        if (mCode.indexOf("<lmx>") != 0) {
            String code = mCode.toString();
            mCode.setLength(0);
            mCode.append("<lmx>" + code + "</lmx>");
        }
        return mCode.toString();
    }



    public String getNode() {
        return mNode.toString();
    }



    public boolean isEmpty() {
        return (mItem.length() == 0);
    }



    public void pushNode(String aName) {
        if (mNode.length() == 0)
            if (mItem.length() > 0)
                pushItem("item");
        mCode.append("<" + aName + ">" + mNode + "</" + aName + ">");
        mNode.setLength(0);
    }



    public String pullNode(String aName) {
        mNode.setLength(0);
        String s;
        int b1, e1, b2, e2;

        s = "<";
        s += aName;
        s += ">";
        b1 = mCode.indexOf(s);
        if (b1 < 0) return "";
        e1 = b1 + s.length();

        s = "</";
        s += aName;
        s += ">";
        b2 = mCode.indexOf(s);
        if (b2 < 0) return "";
        e2 = b2 + s.length();

        mNode.append(mCode.substring(e1, b2));
        mCode.delete(b1, e2);

        mItem.setLength(0);
        mItem.append(mNode);

        return mNode.toString();
    }



    public void pushItem(String aName) {
        mNode.append("<" + aName + mItem + "/>");
        mItem.setLength(0);
    }



    public String pullItem(String aName) {
        mItem.setLength(0);
        String s;
        int b1, e1, b2, e2;

        s = "<";
        s += aName;
        s += " ";
        b1 = mNode.indexOf(s);
        if (b1 < 0) return "";
        e1 = b1 + s.length();

        s = "/>";
        b2 = mNode.indexOf(s);
        if (b2 < 0) return "";
        e2 = b2 + s.length();

        mItem.append(mNode.substring(e1, b2));
        mNode.delete(b1, e2);

        return mItem.toString();
    }






    public void addStr(String aName, String aValue) {
        mItem.append(" " + aName + "=\"" + aValue + "\"");
    }


    public void addInt(String aName, int aValue) {
        mItem.append(" " + aName + "=\"" + aValue + "\"");
    }


    public void addLong(String aName, long aValue) {
        mItem.append(" " + aName + "=\"" + aValue + "\"");
    }


    public void addFlt(String aName, float aValue) {
        mItem.append(" " + aName + "=\"" + aValue + "\"");
    }


    public void addBln(String aName, boolean aValue) {
        String val = "0";
        if (aValue) val = "1";
        mItem.append(" " + aName + "=\"" + val + "\"");
    }




    public String getStr(String aName) {
        String result = "";
        String s;
        int b1, e1, b2, e2;

        s = aName + "=\"";
        b1 = mItem.indexOf(s);
        if (b1 < 0) return "";
        e1 = b1 + s.length();

        b2 = mItem.indexOf("\"", e1);
        if (b2 < 0) return "";
        e2 = b2 + 1;

        result = mItem.substring(e1, b2);
        return result;
    }



    public int getInt(String aName) {
        int result = 0;
        String str = getStr(aName);
        try {
            result = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }



    public long getLong(String aName) {
        long result = 0;
        String str = getStr(aName);
        try {
            result = Long.parseLong(str);
        } catch (NumberFormatException e) {
            result = 0;
        }
        return result;
    }




    public boolean getBln(String aName) {
        boolean result = false;
        String str = getStr(aName);
        if (str.equals("1"))
            result = true;
        return result;
    }



    public float getFlt(String aName) {
        float result = 0f;
        String str = getStr(aName);
        try {
            result = Float.parseFloat(str);
        } catch (NumberFormatException e) {
            result = 0f;
        }
        return result;
    }



    public static boolean selfTest1() {
        Lmx lmx;
        lmx = new Lmx();
        lmx.addStr("_str", "1 1 1");
        lmx.addInt("_int", 111);
        lmx.addFlt("_flt", 2.2f);
        lmx.addBln("_bln", true);
        String txt = lmx.getCode();

        lmx = new Lmx(txt);
        String s = lmx.getStr("_str");
        if (!s.equals("1 1 1"))
            return false;

        int i = lmx.getInt("_int");
        if (i != 111)
            return false;

        float f = lmx.getFlt("_flt");
        if (f != 2.2f)
            return false;

        boolean b = lmx.getBln("_bln");
        if (b != true)
            return false;

        return true;
    }



    public static boolean selfTest2() {
        Lmx lmx;
        lmx = new Lmx();
        lmx.pushNode("node1");

        lmx.addStr("_str", "1 1 1");
        lmx.addInt("_int", 111);
        lmx.addFlt("_flt", 2.2f);
        lmx.addBln("_bln", true);
        lmx.pushItem("_item");

        lmx.pushNode("node2");
        String txt = lmx.getCode();

        lmx = new Lmx(txt);
        lmx.pullNode("node1");
        String tag = lmx.getNode();

        lmx.pullNode("node2");
        lmx.pullItem("_item");

        String s = lmx.getStr("_str");
        if (!s.equals("1 1 1"))
            return false;

        int i = lmx.getInt("_int");
        if (i != 111)
            return false;

        float f = lmx.getFlt("_flt");
        if (f != 2.2f)
            return false;

        boolean b = lmx.getBln("_bln");
        if (b != true)
            return false;

        txt += "_";
        return true;
    }


}
