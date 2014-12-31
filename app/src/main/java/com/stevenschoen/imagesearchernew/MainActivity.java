package com.stevenschoen.imagesearchernew;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.stevenschoen.imagesearchernew.model.Image;
import com.stevenschoen.imagesearchernew.model.Search;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

public class MainActivity extends ActionBarActivity {

    private static final String STATE_SEARCH = "search";
    private static final String STATE_IMAGES = "images";

    private SearchInterface searchInterface;
    private SearchInterface.Service searchService;

    private Search currentSearch;

    private RecyclerView imagesGrid;
    private ImagesAdapter imagesAdapter;
    private ArrayList<Image> images;

    private ProgressBar imagesLoading;
    private TextView resultsCountText;
    private View resultsCountHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
        }
        setContentView(R.layout.main);

        searchService = SearchInterface.Service.Google;
        searchInterface = new SearchInterface(searchService);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_SEARCH)) {
                currentSearch = savedInstanceState.getParcelable(STATE_SEARCH);
            }
            if (savedInstanceState.containsKey(STATE_IMAGES)) {
                images = savedInstanceState.getParcelableArrayList(STATE_IMAGES);
            }
        }

        if (images == null) {
            images = new ArrayList<>();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        imagesGrid = (RecyclerView) findViewById(R.id.main_imagesgrid);
        imagesGrid.setItemAnimator(null);
        final RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 4);
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
        imagesAdapter.setMoreImagesCallback(new ImagesAdapter.MoreImagesCallback() {
            @Override
            public void onMoreImagesPressed() {
                nextPageAsync();
            }
        });
        imagesGrid.setAdapter(imagesAdapter);

        imagesLoading = (ProgressBar) findViewById(R.id.main_imagesloading);

        resultsCountText = (TextView) findViewById(R.id.main_resultscount);
        resultsCountHolder = findViewById(R.id.main_resultscount_holder);
        resultsCountHolder.animate().setInterpolator(new DecelerateInterpolator());
        if (currentSearch != null && currentSearch.totalResults != -1) {
            updateResultsCount(false);
        }

        EditText searchText = (EditText) findViewById(R.id.main_searchtext);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event == null || event.getAction() == KeyEvent.ACTION_DOWN) {
                    searchAsync(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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

    private void searchAsync(final String query) {
        new AsyncTask<Void, Void, Search>() {
            @Override
            protected void onPreExecute() {
                notifyAdapterEmpty();
                imagesLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Search doInBackground(Void... nothing) {
                try {
                    return searchInterface.searchImages(query);
                } catch (final RetrofitError e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
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
            protected void onPostExecute(Search search) {
                currentSearch = search;
                imagesLoading.setVisibility(View.GONE);
                if (search != null) {
                    if (search.totalResults != -1) {
                        updateResultsCount(true);
                    } else {
                        hideResultsCount(true);
                    }

                    if (search.images != null) {
                        notifyAdapterEmpty();
                        images.addAll(search.images);
                        imagesAdapter.notifyItemRangeInserted(0, search.images.size());
                    }
                } else {
                    hideResultsCount(true);
                }
            }
        }.execute();
    }

    private void nextPageAsync() {
        if (currentSearch != null) {
            new AsyncTask<Void, Void, Search>() {
                @Override
                protected Search doInBackground(Void... params) {
                    return searchInterface.nextPage(currentSearch, currentSearch.images.size());
                }

                @Override
                protected void onPostExecute(Search search) {
                    currentSearch = search;

                    if (search.images != null) {
                        int oldSize = images.size();
                        List<Image> subList = search.images.subList(oldSize, search.images.size());
                        images.addAll(oldSize, subList);
                        imagesAdapter.notifyItemRangeInserted(oldSize, subList.size());
                    }
                }
            }.execute();
        }
    }

    private void notifyAdapterEmpty() {
        int oldSize = images.size();
        int offset = images.isEmpty() ? 0 : 1;
        images.clear();
        imagesAdapter.notifyItemRangeRemoved(0, oldSize + offset);
    }

    private void updateResultsCount(boolean animate) {
        String friendlyCount = new DecimalFormat().format(currentSearch.totalResults);
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
        Ion.with(this).load(images.get(position).thumbnailLink).asBitmap().setCallback(new FutureCallback<Bitmap>() {
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
            final Image image = data.getParcelableExtra(ResultActivity.EXTRA_RESULT_IMAGE_RESULT);
            finishWithResult(image);
        }
    }

    private void finishWithResult(final Image imageResult) {
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
                Uri uri = FixedFileProvider.getUriForFile(
                        MainActivity.this,
                        "com.stevenschoen.imagesearchernew.fileprovider",
                        destinationFile);

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(uri, imageResult.mimeType);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(STATE_SEARCH, currentSearch);
        outState.putParcelableArrayList(STATE_IMAGES, images);
    }
}