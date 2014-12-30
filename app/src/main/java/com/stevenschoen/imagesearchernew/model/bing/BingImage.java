package com.stevenschoen.imagesearchernew.model.bing;

public class BingImage {
    public String Title;
    public String MediaUrl;
    public String SourceUrl;
    public String DisplayUrl;
    public long Width, Height;
    public long FileSize;
    public String ContentType;
    public Thumbnail Thumbnail;

    public static class Thumbnail {
        public String MediaUrl;
    }
}