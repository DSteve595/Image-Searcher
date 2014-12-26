package com.stevenschoen.imagesearcher;

import com.stevenschoen.imagesearcher.model.ImageResult;
import com.stevenschoen.imagesearcher.model.SearchResponse;

public class FakeSearchInterface implements SearchInterface {

    private static final int ITEMS_COUNT = 10;

    @Override
    public SearchResponse search(String query, int startIndex, String searchType) {
        SearchResponse response = new SearchResponse();

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

    private void makeItems(SearchResponse response) {
        ImageResult[] items = new ImageResult[ITEMS_COUNT];

        for (int i = 0; i < items.length; i++) {
            ImageResult result = new ImageResult();
            result.title = "Test result " + i;
            result.link = "http://www.breastcancerwellness.org/bcw/wp-content/uploads/2014/09/test.gif";
            result.displayLink = "www.test.com";
            result.mime = "image/gif";
            result.image = new ImageResult.Image();
            result.image.height = 828;
            result.image.width = 1646;
            result.image.byteSize = 22544;
            result.image.thumbnailLink = "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQ71ukeTGCPLuClWd6MetTtQ0-0mwzo3rn1ug0MUnbpXmKnwNuuBnSWXHU";

            items[i] = result;
        }

        response.items = items;
    }

    private void makeQueries(SearchResponse response) {
        SearchResponse.Queries queries = new SearchResponse.Queries();

        SearchResponse.Queries.Page currentPage = new SearchResponse.Queries.Page();
        currentPage.count = ITEMS_COUNT;
        currentPage.startIndex = 1;
        queries.request[0] = currentPage;

        response.queries = queries;
    }

    private void makeSearchInformation(SearchResponse response) {
        SearchResponse.SearchInformation searchInformation = new SearchResponse.SearchInformation();

        searchInformation.totalResults = 7000000;

        response.searchInformation = searchInformation;
    }
}
