/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.microedition.rms.RecordStore
 *  javax.microedition.rms.RecordStoreException
 */
package lib;

import javax.microedition.rms.RecordStoreException;

public class RecordStore {
    private static void a(char paramChar, String paramString) throws RecordStoreException {
        javax.microedition.rms.RecordStore.deleteRecordStore((String)(paramChar + paramString));
    }

    private static javax.microedition.rms.RecordStore a(char paramChar, String paramString, boolean paramBoolean) throws RecordStoreException {
        return javax.microedition.rms.RecordStore.openRecordStore((String)(paramChar + paramString), (boolean)paramBoolean);
    }

    private static javax.microedition.rms.RecordStore a(char paramChar, String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return javax.microedition.rms.RecordStore.openRecordStore((String)(paramChar + paramString), (boolean)paramBoolean1, (int)paramInt, (boolean)paramBoolean2);
    }

    private static String[] a(char paramChar) {
        String[] localObject = javax.microedition.rms.RecordStore.listRecordStores();
        String[] arrayOfString = new String[localObject.length];
        int i = 0;
        for (int j = localObject.length - 1; j >= 0; --j) {
            if (localObject[j].charAt(0) != paramChar) continue;
            arrayOfString[i++] = localObject[j].substring(1);
        }
        if (i == 0) {
            return null;
        }
        if (i == arrayOfString.length) {
            localObject = arrayOfString;
        } else {
            localObject = new String[i];
            System.arraycopy(arrayOfString, 0, localObject, 0, i);
        }
        return localObject;
    }

    public static void aeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('a', paramString);
    }

    public static void beleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('b', paramString);
    }

    public static void celeteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('c', paramString);
    }

    public static void deleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('d', paramString);
    }

    public static void eeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('e', paramString);
    }

    public static void feleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('f', paramString);
    }

    public static void geleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('g', paramString);
    }

    public static void heleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('h', paramString);
    }

    public static void ieleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('i', paramString);
    }

    public static void jeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('j', paramString);
    }

    public static void keleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('k', paramString);
    }

    public static void leleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('l', paramString);
    }

    public static void meleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('m', paramString);
    }

    public static void neleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('n', paramString);
    }

    public static void oeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('o', paramString);
    }

    public static void peleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('p', paramString);
    }

    public static void qeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('q', paramString);
    }

    public static void releteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('r', paramString);
    }

    public static void seleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('s', paramString);
    }

    public static void teleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('t', paramString);
    }

    public static void ueleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('u', paramString);
    }

    public static void veleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('v', paramString);
    }

    public static void weleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('w', paramString);
    }

    public static void xeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('x', paramString);
    }

    public static void yeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('y', paramString);
    }

    public static void zeleteRecordStore(String paramString) throws RecordStoreException {
        RecordStore.a('z', paramString);
    }

    public static javax.microedition.rms.RecordStore apenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('a', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore bpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('b', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore cpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('c', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore dpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('d', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore epenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('e', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore fpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('f', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore gpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('g', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore hpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('h', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore ipenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('i', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore jpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('j', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore kpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('k', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore lpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('l', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore mpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('m', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore npenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('n', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore openRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('o', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore ppenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('p', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore qpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('q', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore rpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('r', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore spenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('s', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore tpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('t', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore upenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('u', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore vpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('v', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore wpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('w', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore xpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('x', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore ypenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('y', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore zpenRecordStore(String paramString, boolean paramBoolean) throws RecordStoreException {
        return RecordStore.a('z', paramString, paramBoolean);
    }

    public static javax.microedition.rms.RecordStore apenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('a', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore bpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('b', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore cpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('c', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore dpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('d', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore epenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('e', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore fpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('f', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore gpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('g', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore hpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('h', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore ipenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('i', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore jpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('j', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore kpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('k', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore lpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('l', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore mpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('m', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore npenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('n', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore openRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('o', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore ppenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('p', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore qpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('q', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore rpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('r', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore spenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('s', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore tpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('t', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore upenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('u', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore vpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('v', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore wpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('w', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore xpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('x', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore ypenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('y', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static javax.microedition.rms.RecordStore zpenRecordStore(String paramString, boolean paramBoolean1, int paramInt, boolean paramBoolean2) throws RecordStoreException {
        return RecordStore.a('z', paramString, paramBoolean1, paramInt, paramBoolean2);
    }

    public static String[] aistRecordStores() {
        return RecordStore.a('a');
    }

    public static String[] bistRecordStores() {
        return RecordStore.a('b');
    }

    public static String[] cistRecordStores() {
        return RecordStore.a('c');
    }

    public static String[] distRecordStores() {
        return RecordStore.a('d');
    }

    public static String[] eistRecordStores() {
        return RecordStore.a('e');
    }

    public static String[] fistRecordStores() {
        return RecordStore.a('f');
    }

    public static String[] gistRecordStores() {
        return RecordStore.a('g');
    }

    public static String[] histRecordStores() {
        return RecordStore.a('h');
    }

    public static String[] iistRecordStores() {
        return RecordStore.a('i');
    }

    public static String[] jistRecordStores() {
        return RecordStore.a('j');
    }

    public static String[] kistRecordStores() {
        return RecordStore.a('k');
    }

    public static String[] listRecordStores() {
        return RecordStore.a('l');
    }

    public static String[] mistRecordStores() {
        return RecordStore.a('m');
    }

    public static String[] nistRecordStores() {
        return RecordStore.a('n');
    }

    public static String[] oistRecordStores() {
        return RecordStore.a('o');
    }

    public static String[] pistRecordStores() {
        return RecordStore.a('p');
    }

    public static String[] qistRecordStores() {
        return RecordStore.a('q');
    }

    public static String[] ristRecordStores() {
        return RecordStore.a('r');
    }

    public static String[] sistRecordStores() {
        return RecordStore.a('s');
    }

    public static String[] tistRecordStores() {
        return RecordStore.a('t');
    }

    public static String[] uistRecordStores() {
        return RecordStore.a('u');
    }

    public static String[] vistRecordStores() {
        return RecordStore.a('v');
    }

    public static String[] wistRecordStores() {
        return RecordStore.a('w');
    }

    public static String[] xistRecordStores() {
        return RecordStore.a('x');
    }

    public static String[] yistRecordStores() {
        return RecordStore.a('y');
    }

    public static String[] zistRecordStores() {
        return RecordStore.a('z');
    }
}

