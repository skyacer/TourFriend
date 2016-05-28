package com.elong.tourpal.ui.supports.album;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elong.tourpal.R;
import com.elong.tourpal.application.Env;
import com.elong.tourpal.imageasyncloader.impl.LoadImageTask;
import com.elong.tourpal.imageasyncloader.view.CustomImageView;
import com.elong.tourpal.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by zhitao.xu on 2015/3/30.
 */
public class GalleryPagerAdapter extends PagerAdapter {
    private static final boolean DEBUG = Env.DEBUG;
    private static final String TAG = GalleryPagerAdapter.class.getSimpleName();
    private Handler mHandler = new Handler();
    private static final int CACHE_VIEW_SIZE = 4;
    private int mSize = 0;
    private View[] mViews = new View[4];
    private List<String> mPhotoPaths;
    private List<String> mThumbPaths;
    private Context mContext;
    BitmapCache mPhotoCache;

    public GalleryPagerAdapter(Context c, List<String> photoPathsOrUrls) {
        mContext = c;
        mPhotoPaths = photoPathsOrUrls;
        mPhotoCache = new BitmapCache();
        mViews[0] = LayoutInflater.from(c).inflate(R.layout.gallery_pager, null);
        mViews[1] = LayoutInflater.from(c).inflate(R.layout.gallery_pager, null);
        mViews[2] = LayoutInflater.from(c).inflate(R.layout.gallery_pager, null);
        mViews[3] = LayoutInflater.from(c).inflate(R.layout.gallery_pager, null);
    }

    public GalleryPagerAdapter(Context c, List<String> photoPathsOrUrls, List<String> thumbPaths) {
        this(c, photoPathsOrUrls);
        mThumbPaths = thumbPaths;
    }

    private String getThumbPath(CustomImageView civ, int position) {
        String result = null;
        if (mThumbPaths != null && mThumbPaths.size() > position) {
            result = mThumbPaths.get(position);
            if (Utils.isUrl(result) && civ != null) {
                result = civ.convertUrlToPath(result);
            }
        }
        return result;
    }

    @Override
    public int getCount() {
        return mPhotoPaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = mViews[position % CACHE_VIEW_SIZE];
        container.addView(v);
        final CustomImageView iv = (CustomImageView) v.findViewById(R.id.gallery_image);

        String filePath = mPhotoPaths.get(position);
        if (Utils.isUrl(filePath)) {
            iv.setImageUrl(filePath);
            String thumbPath = getThumbPath(iv, position);
            if (!TextUtils.isEmpty(thumbPath)) {
                LoadImageTask task = new LoadImageTask(thumbPath);
                boolean bingo = task.doLoadFromCache();
                BitmapDrawable drawable = null;
                if (!bingo) {
                    drawable = com.elong.tourpal.imageasyncloader.other.Utils.readBitmapDrawable(mContext, thumbPath);
                } else {
                    drawable = task.getLoadResult();
                }
                iv.setImageDrawable(drawable);
            }
            iv.loadImageFromUrl();
        } else {
            iv.setImagePath(filePath);
            iv.loadImageFromFile();
        }

        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageView iv = (ImageView) mViews[position % CACHE_VIEW_SIZE].findViewById(R.id.gallery_image);
        iv.setImageDrawable(null);
        container.removeView(mViews[position % CACHE_VIEW_SIZE]);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
