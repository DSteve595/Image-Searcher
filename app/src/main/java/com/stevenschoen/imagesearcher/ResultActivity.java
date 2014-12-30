package com.stevenschoen.imagesearcher;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.stevenschoen.imagesearcher.model.Image;

import java.util.List;

public class ResultActivity extends ActionBarActivity {

    public static final String EXTRA_IMAGES = "images";
    public static final String EXTRA_IMAGE_POSITION = "imagePosition";
    public static final String EXTRA_THUMBNAIL_BITMAP = "thumbnailBitmap";

    public static final String EXTRA_RESULT_IMAGE_RESULT = "imageResult";

    private ViewPager pager;

    private List<Image> images;
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

        pager = (ViewPager) findViewById(R.id.imageresult_pager_pager);
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
                finishWithResult(getCurrentImageResult());
            }
        });
    }

    public Image getCurrentImageResult() {
        return images.get(pager.getCurrentItem());
    }

    private void showDetailsDialog() {
        Image imageResult = getCurrentImageResult();

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .theme(Theme.DARK)
                .title(imageResult.title)
                .customView(R.layout.imageresult_details)
                .neutralText(R.string.close)
                .build();

        TextView linkText = (TextView) dialog.getCustomView().findViewById(R.id.imageresult_details_link);
        linkText.setText(imageResult.link);

        TextView siteText = (TextView) dialog.getCustomView().findViewById(R.id.imageresult_details_site);
        siteText.setText(imageResult.displayLink);

        TextView typeText = (TextView) dialog.getCustomView().findViewById(R.id.imageresult_details_type);
        typeText.setText(imageResult.mimeType);

        TextView sizeText = (TextView) dialog.getCustomView().findViewById(R.id.imageresult_details_size);
        sizeText.setText(Utils.humanReadableByteCount(imageResult.size, false));

        TextView resolutionText = (TextView) dialog.getCustomView().findViewById(R.id.imageresult_details_resolution);
        String resolution = imageResult.width + " x " + imageResult.height;
        resolutionText.setText(resolution);

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_result_details:
                showDetailsDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void finishWithResult(Image imageResult) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_IMAGE_RESULT, imageResult);

        setResult(RESULT_OK, intent);
        finish();
    }
}
