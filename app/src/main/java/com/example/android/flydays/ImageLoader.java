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

//todo: edit/add to the report -- custom loader was created for images in support v4 library, and loading images as bitmaps
// this changed in favour of Picassa, easy library that does the same and more in few lines of code. It also caches the images
// loading of the city image is initially slower than what was achieved by callbacks but with caching it works great next time
//todo: delete this whole class before submission

//code on how to work with bitmap and get a picture from a url inspired by primpap at https://stackoverflow.com/questions/3118691/android-make-an-image-at-a-url-equal-to-imageviews-image/3118966#3118966

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
