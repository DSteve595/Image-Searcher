package com.stevenschoen.imagesearcher.model;

public class SearchResponse {
    public ImageResult[] items;
    public Queries queries;
    public SearchInformation searchInformation;

    public static class Queries {
        public Page[] nextPage = new Page[1];
        public Page[] request = new Page[1];

        public static class Page {
            public int startIndex;
            public int count;
        }

        public Page getNextPage() {
            return nextPage[0];
        }

        public Page getRequest() {
            return request[0];
        }
    }

    public static class SearchInformation {
        public long totalResults;
    }
}
