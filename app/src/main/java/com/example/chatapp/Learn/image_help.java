package com.example.chatapp.Learn;

import android.net.Uri;

public class image_help {
    private String name;

private Uri uri;

    public image_help(String name, Uri uri) {
        this.name = name;
        this.uri = uri;
    }

    public image_help() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
