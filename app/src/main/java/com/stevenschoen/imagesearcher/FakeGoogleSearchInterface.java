package com.stevenschoen.imagesearcher;

import com.stevenschoen.imagesearcher.model.google.GoogleImage;
import com.stevenschoen.imagesearcher.model.google.GoogleSearchResponse;

public class FakeGoogleSearchInterface {

    private static final int ITEMS_COUNT = 10;

    public GoogleSearchResponse search() {
        GoogleSearchResponse response = new GoogleSearchResponse();

        makeItems(response);
        makeQueries(response);
        makeSearchInformation(response);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }

    private void makeItems(GoogleSearchResponse response) {
        GoogleImage[] items = new GoogleImage[ITEMS_COUNT];

        for (int i = 0; i < items.length; i++) {
            GoogleImage result = new GoogleImage();
            result.title = "Test result " + i;
            result.link = "http://www.breastcancerwellness.org/bcw/wp-content/uploads/2014/09/test.gif";
            result.displayLink = "www.test.com";
            result.mime = "image/gif";
            result.image = new GoogleImage.Image();
            result.image.height = 828;
            result.image.width = 1646;
            result.image.byteSize = 22544;
            result.image.thumbnailLink = "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQ71ukeTGCPLuClWd6MetTtQ0-0mwzo3rn1ug0MUnbpXmKnwNuuBnSWXHU";

            items[i] = result;
        }

        response.items = items;
    }

    private void makeQueries(GoogleSearchResponse response) {
        GoogleSearchResponse.GoogleQueries queries = new GoogleSearchResponse.GoogleQueries();

        GoogleSearchResponse.GoogleQueries.GooglePage currentPage = new GoogleSearchResponse.GoogleQueries.GooglePage();
        currentPage.count = ITEMS_COUNT;
        currentPage.startIndex = 1;
        queries.request[0] = currentPage;

        response.queries = queries;
    }

    private void makeSearchInformation(GoogleSearchResponse response) {
        GoogleSearchResponse.GoogleSearchInformation searchInformation = new GoogleSearchResponse.GoogleSearchInformation();

        searchInformation.totalResults = 7000000;

        response.searchInformation = searchInformation;
    }
}
