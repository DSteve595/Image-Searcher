package com.stevenschoen.imagesearch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.stevenschoen.imagesearch.model.ImageResult;
import com.stevenschoen.imagesearch.model.SearchResponse;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class MainActivity extends ActionBarActivity {

    private SearchInterface searchInterface;

    private RecyclerView imagesGrid;
    private ImagesAdapter imagesAdapter;
    private ArrayList<ImageResult> images;

    private ProgressBar imagesLoading;

    public boolean fakeRequests = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (fakeRequests && BuildConfig.DEBUG) {
            searchInterface = new FakeSearchInterface();
        } else {
            RestAdapter.Builder restAdapterBuilder = new RestAdapter.Builder()
                    .setEndpoint(SearchInterface.BASE_URL)
                    .setLogLevel(RestAdapter.LogLevel.BASIC)
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addQueryParam("key", SearchInterface.API_KEY);
                            request.addQueryParam("cx", SearchInterface.CUSTOM_SEARCH_ID);
                        }
                    });
            RestAdapter restAdapter = restAdapterBuilder.build();
            searchInterface = restAdapter.create(SearchInterface.class);
        }

        images = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        imagesGrid = (RecyclerView) findViewById(R.id.main_imagesgrid);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
        imagesGrid.setLayoutManager(layoutManager);
        imagesAdapter = new ImagesAdapter(images);
        imagesAdapter.setOnItemClickListener(new ImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                openImageResult(position, view);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                finishWithResult(images.get(position));
            }
        });
        imagesGrid.setAdapter(imagesAdapter);

        imagesLoading = (ProgressBar) findViewById(R.id.main_imagesloading);

        EditText searchText = (EditText) findViewById(R.id.main_searchtext);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchAsync(v.getText().toString());
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchAsync(final String query) {
        new AsyncTask<Void, Void, SearchResponse>() {
            @Override
            protected void onPreExecute() {
                imagesGrid.setVisibility(View.GONE);
                imagesLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected SearchResponse doInBackground(Void... nothing) {
                return searchInterface.search(query, SearchInterface.SEARCH_TYPE_IMAGE);
            }

            @Override
            protected void onPostExecute(SearchResponse result) {
                imagesLoading.setVisibility(View.GONE);
                if (result != null) {
                    imagesGrid.setVisibility(View.VISIBLE);
                    images.clear();
                    if (result.items != null) {
                        Collections.addAll(images, result.items);
                    }
                    imagesAdapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    private void openImageResult(final int position, final View viewToZoomFrom) {
        Ion.with(this).load(images.get(position).image.thumbnailLink).asBitmap().setCallback(new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                Intent resultIntent = new Intent(MainActivity.this, ResultActivity.class);
                resultIntent.putParcelableArrayListExtra(ResultActivity.EXTRA_IMAGES, images);
                resultIntent.putExtra(ResultActivity.EXTRA_IMAGE_POSITION, position);
                resultIntent.putExtra(ResultActivity.EXTRA_THUMBNAIL_BITMAP, result);

                Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(viewToZoomFrom,
                        0, 0,
                        viewToZoomFrom.getWidth(), viewToZoomFrom.getHeight()).toBundle();

                ActivityCompat.startActivityForResult(MainActivity.this, resultIntent, 0, options);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            final ImageResult imageResult = data.getParcelableExtra(ResultActivity.EXTRA_RESULT_IMAGE_RESULT);
            finishWithResult(imageResult);
        }
    }

    private void finishWithResult(final ImageResult imageResult) {
        File destinationFolder = new File(getFilesDir(), "result");
        destinationFolder.mkdir();
        try {
            FileUtils.cleanDirectory(destinationFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String filename = imageResult.title + "." + MimeTypeMap.getFileExtensionFromUrl(imageResult.link);
        final File destinationFile = new File(destinationFolder, filename);
        Ion.with(this).load(imageResult.link).write(destinationFile).setCallback(new FutureCallback<File>() {
            @Override
            public void onCompleted(Exception e, File result) {
                Uri uri = FileProvider.getUriForFile(
                        MainActivity.this,
                        "com.stevenschoen.imagesearch.fileprovider",
                        destinationFile);

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, imageResult.mime);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}