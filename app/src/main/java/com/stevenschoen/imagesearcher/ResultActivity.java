package com.stevenschoen.imagesearcher;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.stevenschoen.imagesearcher.model.ImageResult;

import java.util.List;

public class ResultActivity extends ActionBarActivity {

    public static final String EXTRA_IMAGES = "images";
    public static final String EXTRA_IMAGE_POSITION = "imagePosition";
    public static final String EXTRA_THUMBNAIL_BITMAP = "thumbnailBitmap";

    public static final String EXTRA_RESULT_IMAGE_RESULT = "imageResult";

    private List<ImageResult> images;
    private int imagePosition;
    private Bitmap thumbnailBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageresult_pager);

        if (getIntent() != null) {
            images = getIntent().getParcelableArrayListExtra(EXTRA_IMAGES);
            imagePosition = getIntent().getIntExtra(EXTRA_IMAGE_POSITION, 0);
            thumbnailBitmap = getIntent().getParcelableExtra(EXTRA_THUMBNAIL_BITMAP);
        } else {
            finish();
        }

        final ViewPager pager = (ViewPager) findViewById(R.id.imageresult_pager_pager);
        final PublicFragmentPagerAdapter pagerAdapter = new PublicFragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Bundle args = new Bundle();
                args.putParcelable(ResultFragment.EXTRA_IMAGE_RESULT, images.get(position));
                if (position == imagePosition) {
                    args.putParcelable(ResultFragment.EXTRA_THUMBNAIL_BITMAP, thumbnailBitmap);
                }
                return Fragment.instantiate(ResultActivity.this, ResultFragment.class.getName(), args);
            }

            @Override
            public int getCount() {
                return images.size();
            }
        };
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                setTitle(images.get(position).title);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        pager.setCurrentItem(imagePosition, false);

        View selectButton = findViewById(R.id.imageresult_pager_select);
        Utils.setupFab(selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResultFragment fragment = (ResultFragment)
                        pagerAdapter.getFragmentAtPosition(pager, pager.getCurrentItem());

                final ImageResult imageResult = fragment.getImageResult();
                finishWithResult(imageResult);
            }
        });
    }

    private void finishWithResult(ImageResult imageResult) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_IMAGE_RESULT, imageResult);

        setResult(RESULT_OK, intent);
        finish();
    }
}
