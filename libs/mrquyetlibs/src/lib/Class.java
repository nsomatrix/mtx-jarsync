/*
 * Decompiled with CFR 0.152.
 */
package lib;

public class Class {
    public static java.lang.Class aorName(String paramString) {
        return Class.load("a.", paramString);
    }

    public static java.lang.Class borName(String paramString) {
        return Class.load("b.", paramString);
    }

    public static java.lang.Class corName(String paramString) {
        return Class.load("c.", paramString);
    }

    public static java.lang.Class dorName(String paramString) {
        return Class.load("d.", paramString);
    }

    public static java.lang.Class eorName(String paramString) {
        return Class.load("e.", paramString);
    }

    public static java.lang.Class forName(String paramString) {
        return Class.load("f.", paramString);
    }

    public static java.lang.Class gorName(String paramString) {
        return Class.load("g.", paramString);
    }

    public static java.lang.Class horName(String paramString) {
        return Class.load("h.", paramString);
    }

    public static java.lang.Class iorName(String paramString) {
        return Class.load("i.", paramString);
    }

    public static java.lang.Class jorName(String paramString) {
        return Class.load("j.", paramString);
    }

    public static java.lang.Class korName(String paramString) {
        return Class.load("k.", paramString);
    }

    public static java.lang.Class lorName(String paramString) {
        return Class.load("l.", paramString);
    }

    public static java.lang.Class morName(String paramString) {
        return Class.load("m.", paramString);
    }

    public static java.lang.Class norName(String paramString) {
        return Class.load("n.", paramString);
    }

    public static java.lang.Class oorName(String paramString) {
        return Class.load("o.", paramString);
    }

    public static java.lang.Class porName(String paramString) {
        return Class.load("p.", paramString);
    }

    public static java.lang.Class qorName(String paramString) {
        return Class.load("q.", paramString);
    }

    public static java.lang.Class rorName(String paramString) {
        return Class.load("r.", paramString);
    }

    public static java.lang.Class sorName(String paramString) {
        return Class.load("s.", paramString);
    }

    public static java.lang.Class torName(String paramString) {
        return Class.load("t.", paramString);
    }

    public static java.lang.Class uorName(String paramString) {
        return Class.load("u.", paramString);
    }

    public static java.lang.Class vorName(String paramString) {
        return Class.load("v.", paramString);
    }

    public static java.lang.Class worName(String paramString) {
        return Class.load("w.", paramString);
    }

    public static java.lang.Class xorName(String paramString) {
        return Class.load("x.", paramString);
    }

    public static java.lang.Class yorName(String paramString) {
        return Class.load("y.", paramString);
    }

    public static java.lang.Class zorName(String paramString) {
        return Class.load("z.", paramString);
    }

    public static java.lang.Class load(String paramString1, String paramString2) {
        try {
            return java.lang.Class.forName(paramString1 + paramString2);
        }
        catch (ClassNotFoundException localClassNotFoundException) {
            try {
                return java.lang.Class.forName(paramString2);
            }
            catch (ClassNotFoundException ex) {
                return null;
            }
        }
    }
}

