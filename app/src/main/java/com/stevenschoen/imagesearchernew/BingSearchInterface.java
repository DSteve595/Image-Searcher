package com.stevenschoen.imagesearchernew;

import com.stevenschoen.imagesearchernew.model.bing.BingSearchResponse;

import retrofit.http.GET;
import retrofit.http.Query;

public interface BingSearchInterface {
    public static final String BASE_URL = "https://api.datamarket.azure.com/Bing/Search/v1";
    public static final String API_KEY = "IzzjGEoDbc6kUVakkclWmrAYURcokmSrevY8bzm+R+U";

    public static final String SOURCE_IMAGE = "image";
    public static final String FORMAT_JSON = "json";

    @GET("/Composite")
    public BingSearchResponse search(@Query("Sources") String sources,
                                     @Query("Query") String query,
                                     @Query("$skip") long skip,
                                     @Query("$format") String format);
}