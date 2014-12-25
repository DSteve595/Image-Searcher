package com.stevenschoen.imagesearch;

import com.stevenschoen.imagesearch.model.SearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;

public interface SearchInterface {
    public static final String BASE_URL = "https://www.googleapis.com/customsearch";
    public static final String API_KEY = "AIzaSyCCuxxVLzm2sZP-adhRNYKeSck1mMMgsAM";
    public static final String CUSTOM_SEARCH_ID = "006276545175240274241:4xzxdv12bjw";

    public static final String SEARCH_TYPE_IMAGE = "image";

    @GET("/v1")
    public SearchResponse search(@Query("q") String query, @Query("searchType") String searchType);
}