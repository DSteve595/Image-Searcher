package com.stevenschoen.imagesearcher;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.stevenschoen.imagesearcher.model.Image;
import com.stevenschoen.imagesearcher.model.Search;
import com.stevenschoen.imagesearcher.model.bing.BingImage;
import com.stevenschoen.imagesearcher.model.bing.BingSearchResponse;
import com.stevenschoen.imagesearcher.model.google.GoogleImage;
import com.stevenschoen.imagesearcher.model.google.GoogleSearchResponse;

import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

public class SearchInterface {
    public static enum Service {
        FakeGoogle, Google, Bing
    }

    public Service service;

    private FakeGoogleSearchInterface fakeGoogleSearchInterface;
    private GoogleSearchInterface googleSearchInterface;
    private BingSearchInterface bingSearchInterface;

    public SearchInterface(Service service) {
        this.service = service;

        switch (service) {
            case FakeGoogle:
                fakeGoogleSearchInterface = new FakeGoogleSearchInterface();
                break;
            case Google:
                RestAdapter.Builder googleBuilder = new RestAdapter.Builder()
                        .setEndpoint(GoogleSearchInterface.BASE_URL)
                        .setLogLevel(RestAdapter.LogLevel.BASIC)
                        .setRequestInterceptor(new RequestInterceptor() {
                            @Override
                            public void intercept(RequestFacade request) {
                                request.addQueryParam("key", GoogleSearchInterface.API_KEY);
                                request.addQueryParam("cx", GoogleSearchInterface.CUSTOM_SEARCH_ID);
                            }
                        });
                RestAdapter googleAdapter = googleBuilder.build();
                googleSearchInterface = googleAdapter.create(GoogleSearchInterface.class);
                break;
            case Bing:
                OkHttpClient httpClient = new OkHttpClient();
                httpClient.setAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Proxy proxy, Response response) throws IOException {
                        // "API keys are for hipsters and squares" - Microsoft
                        String credential = Credentials.basic(BingSearchInterface.API_KEY, BingSearchInterface.API_KEY);
                        return response.request().newBuilder()
                                .header("Authorization", credential)
                                .build();
                    }

                    @Override
                    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                        return null;
                    }
                });
                RestAdapter.Builder bingBuilder = new RestAdapter.Builder()
                        .setClient(new OkClient(httpClient))
                        .setEndpoint(BingSearchInterface.BASE_URL)
                        .setLogLevel(RestAdapter.LogLevel.BASIC);
                RestAdapter bingAdapter = bingBuilder.build();
                bingSearchInterface = bingAdapter.create(BingSearchInterface.class);
                break;
        }
    }

    public Search searchImages(String query) {
        switch (service) {
            case FakeGoogle:
                return buildSearch(fakeGoogleSearchInterface.search(), query);
            case Google:
                return buildSearch(googleSearchInterface.search(
                        query,
                        1,
                        GoogleSearchInterface.SEARCH_TYPE_IMAGE),
                        query);
            case Bing:
                // crossFingers();
                return buildSearch(bingSearchInterface.search(
                        Utils.wrapInBingQuotes(BingSearchInterface.SOURCE_IMAGE),
                        Utils.wrapInBingQuotes(query),
                        0,
                        BingSearchInterface.FORMAT_JSON),
                        query);
        }

        return null;
    }

    public Search nextPage(Search search, long firstIndex) {
//        Google starts 'startIndex' at 1, Bing starts 'skip' at 0
//        Starting at 0
        switch (service) {
            case FakeGoogle:
                break;
            case Google:
                Search newGoogleSearch = buildSearch(googleSearchInterface.search(
                        search.input.query,
                        firstIndex + 1,
                        GoogleSearchInterface.SEARCH_TYPE_IMAGE),
                        search.input.query);
                search.images.addAll(newGoogleSearch.images);
                break;
            case Bing:
                Search newBingSearch = buildSearch(bingSearchInterface.search(
                                Utils.wrapInBingQuotes(BingSearchInterface.SOURCE_IMAGE),
                        Utils.wrapInBingQuotes(search.input.query),
                        firstIndex,
                        BingSearchInterface.FORMAT_JSON),
                        search.input.query);
                search.images.addAll(newBingSearch.images);
                break;
        }

        return search;
    }

    private Search buildSearch(GoogleSearchResponse googleResponse, String query) {
        Search search = new Search(query);

        search.images = new ArrayList<>(googleResponse.items.length);
        for (GoogleImage image : googleResponse.items) {
            search.images.add(buildResult(image));
        }

        search.totalResults = googleResponse.searchInformation.totalResults;

        return search;
    }

    private Search buildSearch(BingSearchResponse bingResponse, String query) {
        Search search = new Search(query);

        search.images = new ArrayList<>(bingResponse.d.results[0].Image.length);
        for (BingImage image : bingResponse.d.results[0].Image) {
            search.images.add(buildImage(image));
        }

        search.totalResults = bingResponse.d.results[0].ImageTotal;

        return search;
    }

    private Image buildResult(GoogleImage googleImage) {
        Image image = new Image();

        image.title = googleImage.title;
        image.link = googleImage.link;
        image.thumbnailLink = googleImage.image.thumbnailLink;
        image.displayLink = googleImage.displayLink;
        image.mimeType = googleImage.mime;
        image.width = googleImage.image.width;
        image.height = googleImage.image.height;
        image.size = googleImage.image.byteSize;

        return image;
    }

    private Image buildImage(BingImage bingImage) {
        Image image = new Image();

        image.title = bingImage.Title;
        image.link = bingImage.MediaUrl;
        image.thumbnailLink = bingImage.Thumbnail.MediaUrl;
        image.displayLink = bingImage.SourceUrl;
        image.mimeType = bingImage.ContentType;
        image.width = bingImage.Width;
        image.height = bingImage.Height;
        image.size = bingImage.FileSize;

        return image;
    }
}
