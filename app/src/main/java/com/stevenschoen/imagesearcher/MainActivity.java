package com.stevenschoen.imagesearcher;

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
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.stevenschoen.imagesearcher.model.ImageResult;
import com.stevenschoen.imagesearcher.model.SearchResponse;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class MainActivity extends ActionBarActivity {

    private static final String STATE_IMAGES = "images";
    private static final String STATE_RESULTS_COUNT = "resultsCount";

    private static final int MAX_START_INDEX = 31;

    private SearchInterface searchInterface;

    private long resultsCount = -1;

    private RecyclerView imagesGrid;
    private ImagesAdapter imagesAdapter;
    private ArrayList<ImageResult> images = new ArrayList<>();

    private ProgressBar imagesLoading;
    private TextView resultsCountText;
    private View resultsCountHolder;

    public boolean fakeRequests = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.getInstance().setDebugMode(BuildConfig.DEBUG);
        Crashlytics.start(this);
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

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_IMAGES)) {
                images = savedInstanceState.getParcelableArrayList(STATE_IMAGES);
            }
            if (savedInstanceState.containsKey(STATE_RESULTS_COUNT)) {
                resultsCount = savedInstanceState.getLong(STATE_RESULTS_COUNT);
            }
        }

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

        resultsCountText = (TextView) findViewById(R.id.main_resultscount);
        resultsCountHolder = findViewById(R.id.main_resultscount_holder);
        resultsCountHolder.animate().setInterpolator(new DecelerateInterpolator());
        if (resultsCount != -1) {
            updateResultsCount(false);
        }

        EditText searchText = (EditText) findViewById(R.id.main_searchtext);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int oldSize = images.size();
                images.clear();
                imagesAdapter.notifyItemRangeRemoved(0, oldSize);
                searchAsync(v.getText().toString(), 1);
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
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchAsync(final String query, final int startIndex) {
        new AsyncTask<Void, Void, SearchResponse>() {
            @Override
            protected void onPreExecute() {
                if (images.isEmpty()) {
                    imagesLoading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected SearchResponse doInBackground(Void... nothing) {
                try {
                    return searchInterface.search(query, startIndex, SearchInterface.SEARCH_TYPE_IMAGE);
                } catch (final RetrofitError e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "API Error: " + e.getResponse().getReason(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;
            }

            @Override
            protected void onPostExecute(SearchResponse result) {
                imagesLoading.setVisibility(View.GONE);
                if (result != null) {
                    if (result.searchInformation != null) {
                        resultsCount = result.searchInformation.totalResults;
                        updateResultsCount(true);
                    } else {
                        hideResultsCount(true);
                    }

                    SearchResponse.Queries.Page currentPage = result.queries.getRequest();
                    SearchResponse.Queries.Page nextPage = result.queries.getNextPage();
                    int resultStartIndex = startIndex;

                    // TODO better page loading
                    if (currentPage != null && nextPage != null &&
                            resultStartIndex < MAX_START_INDEX &&
                            (nextPage.startIndex + nextPage.count < result.searchInformation.totalResults)) {
                        searchAsync(query, nextPage.startIndex);
                        resultStartIndex = currentPage.startIndex;
                    }

                    if (result.items != null) {
                        for (int i = 0; i < result.items.length; i++) {
                            int index = resultStartIndex + i - 1;
                            if (index == images.size()) {
                                images.add(result.items[i]);
                                imagesAdapter.notifyItemInserted(index);
                            } else {
                                images.set(index, result.items[i]);
                                imagesAdapter.notifyItemChanged(index);
                            }
                        }
                    }
                } else {
                    hideResultsCount(true);
                }
            }
        }.execute();
    }

    private void updateResultsCount(boolean animate) {
        String friendlyCount = new DecimalFormat().format(resultsCount);
        resultsCountText.setText(getString(R.string.x_results, friendlyCount));
        showResultsCount(animate);
    }

    private void showResultsCount(boolean animate) {
        float translationY = 0;
        if (animate) {
            resultsCountHolder.animate().translationY(translationY);
        } else {
            resultsCountHolder.setTranslationY(translationY);
        }
    }

    private void hideResultsCount(boolean animate) {
        float translationY = resultsCountText.getHeight() * -1;
        if (animate) {
            resultsCountHolder.animate().translationY(translationY);
        } else {
            resultsCountHolder.setTranslationY(translationY);
        }
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
                        "com.stevenschoen.imagesearcher.fileprovider",
                        destinationFile);

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, imageResult.mime);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(STATE_IMAGES, images);
        outState.putLong(STATE_RESULTS_COUNT, resultsCount);
    }
}