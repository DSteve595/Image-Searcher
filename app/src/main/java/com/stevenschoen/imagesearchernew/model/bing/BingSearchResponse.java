package com.stevenschoen.imagesearchernew.model.bing;

public class BingSearchResponse {
    public BingInnerResponse d; // "What should we call the actual content that was asked for?
                                // "I know! 'd'!"
                                // "But why not just put that in the response?"
                                // "You're fired."

    public class BingInnerResponse {
        public BingInnerResult[] results; // "So this array, called 'results'.. it's going to
                                          //  contain the results, right?"
                                          // "No, you imbecile! It'll have just one object, and
                                          //  THAT object will have an array with all the results!"
                                          // "Then why have the original array in the first place?"
                                          // "You're fired, too."

        public class BingInnerResult {
            public long ImageTotal;
            public BingImage[] Image;
        }
    }
}
