package com.proyek.rahmanjai.eatit;

import android.content.Context;
import android.media.Image;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;

import android.util.Log;
 
public class SlidingImage_Adapter extends PagerAdapter {

    private ArrayList<String> ImageUrls;

    private LayoutInflater inflater;
    private Context context;

    public SlidingImage_Adapter(Context context , ArrayList<String> ImageUrls) {
        this.context = context;
        this.ImageUrls = ImageUrls;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return ImageUrls.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.slidingimages_layout, view, false);

        assert imageLayout != null;
        final ImageView imageView = (ImageView) imageLayout
                .findViewById(R.id.image);

            final String url = ImageUrls.get(position);
            Picasso
                    .with(context)
                    .load(url)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            //Log.d("Picasso", "Image loaded from cache>>>" + url);
                        }

                        @Override
                        public void onError() {
                            //Log.d("Picasso", "Try again in ONLINE mode if load from cache is failed");
                            Picasso.with(context).load(url).into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    //Log.d("Picasso", "Image loaded from web>>>" + url);
                                }

                                @Override
                                public void onError() {
                                    //Log.d("Picasso", "Failed to load image online and offline, make sure you enabled INTERNET permission for your app and the url is correct>>>>>>>" + url);
                                }
                            });
                        }
                    });
        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}