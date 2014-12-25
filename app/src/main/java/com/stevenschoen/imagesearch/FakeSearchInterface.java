package com.stevenschoen.imagesearch;

import com.stevenschoen.imagesearch.model.ImageResult;
import com.stevenschoen.imagesearch.model.SearchResponse;

public class FakeSearchInterface implements SearchInterface {

    @Override
    public SearchResponse search(String query, String searchType) {
        SearchResponse response = new SearchResponse();
        response.items = new ImageResult[10];
        for (int i = 0; i < response.items.length; i++) {
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

            response.items[i] = result;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }
}
