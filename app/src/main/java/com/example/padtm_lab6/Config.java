package com.example.padtm_lab6;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

public class Config extends SQLiteOpenHelper {
    private static final int
            DATABASE_VERSION = 1;

    public Config(Context context) {
        super(context, "AppConfig",
                null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String DATABASE_CREATE =
                "create table config " +
                        "(_id integer primary key autoincrement," +
                        "privKey blob not null," +
                        "pubKey string not null);";

        database.execSQL(DATABASE_CREATE);

        addKeys(database);
    }


    private void addKeys(SQLiteDatabase db) {

        KeyPair keys = generateKeys();
        ContentValues values = new ContentValues();

        values.put("privKey", keys.getPrivate().getEncoded());

        values.put("pubKey", Base64.encodeToString(keys.getPublic().getEncoded(), Base64.DEFAULT));
        db.insert("config", null, values);
    }

    public static KeyPair generateKeys() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");

            keygen.initialize(512);
            keyPair = keygen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return keyPair;
    }

    public String getPublic() {
        try {

            SQLiteDatabase db =
                    this.getReadableDatabase();
            Cursor cursor =
                    db.query("config",
                            new String[]{"pubKey",},
                            null, null,
                            null, null,
                            null, null);
            if (cursor != null)
                cursor.moveToFirst();
            db.close();
            return cursor.getString(0);
        } catch (Exception ex) {
        }
        return null;
    }

    private PrivateKey getPrivate() {
        SQLiteDatabase db =
                this.getReadableDatabase();
        Cursor cursor =
                db.query("config",
                        new String[]{"privKey",},
                        null, null,
                        null, null,
                        null, null);
        if (cursor != null)
            cursor.moveToFirst();

        PrivateKey privateKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(cursor.getBlob(0)));
        } catch (Exception ex) {
        }
        db.close();
        return privateKey;
    }

    public String sign(String msg) {
        try {

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getPrivate());

            String sign = Base64.encodeToString(cipher.doFinal(msg.getBytes()), Base64.DEFAULT);
            return sign;
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS config");
        onCreate(db);
    }
}