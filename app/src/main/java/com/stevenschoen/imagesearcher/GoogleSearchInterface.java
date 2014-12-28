package com.stevenschoen.imagesearcher;

import com.stevenschoen.imagesearcher.model.google.GoogleSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;

public interface GoogleSearchInterface {
    public static final String BASE_URL = "https://www.googleapis.com/customsearch";
    public static final String API_KEY = "AIzaSyCCuxxVLzm2sZP-adhRNYKeSck1mMMgsAM"; // Allows all
//    public static final String API_KEY = "AIzaSyBxS4Ocy29KrmGZkr2uibqc78JBdtEyxz8"; // Android, not working
    public static final String CUSTOM_SEARCH_ID = "006276545175240274241:4xzxdv12bjw";

    public static final String SEARCH_TYPE_IMAGE = "image";

    @GET("/v1")
    public GoogleSearchResponse search(@Query("q") String query,
                                       @Query("start") long startIndex,
                                       @Query("searchType") String searchType);
}