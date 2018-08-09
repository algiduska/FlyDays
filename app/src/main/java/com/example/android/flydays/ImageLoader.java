package com.example.android.flydays;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class ImageLoader extends android.support.v4.content.AsyncTaskLoader<Bitmap> {

    private String Uri;
    private Bitmap bitmap;

    public ImageLoader(Context context, String url) {
        super(context);
        this.Uri = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Bitmap loadInBackground() {
        URL url;
        try {
            url = new URL(Uri);
        } catch (MalformedURLException exception) {
            return null;
        }

        try {
            bitmap = BitmapFactory.decodeStream((InputStream)
                    url.getContent());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
