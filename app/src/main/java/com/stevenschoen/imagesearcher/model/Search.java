package com.stevenschoen.imagesearcher.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Search implements Parcelable {
    public Input input; // Not from APIs, details from this app

    public static class Input implements Parcelable {
        public String query;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.query);
        }

        public Input(String query) {
            this.query = query;
        }

        private Input(Parcel in) {
            this.query = in.readString();
        }

        public static final Creator<Input> CREATOR = new Creator<Input>() {
            public Input createFromParcel(Parcel source) {
                return new Input(source);
            }

            public Input[] newArray(int size) {
                return new Input[size];
            }
        };
    }

    public List<Image> images;
    public long totalResults;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.input, flags);
        dest.writeTypedList(images);
        dest.writeLong(totalResults);
    }

    public Search() {
        images = new ArrayList<>();
        totalResults = -1;
    }

    public Search(String query) {
        super();
        input = new Input(query);
    }

    private Search(Parcel in) {
        super();
        this.input = in.readParcelable(Input.class.getClassLoader());
        in.readTypedList(images, Image.CREATOR);
        this.totalResults = in.readLong();
    }

    public static final Parcelable.Creator<Search> CREATOR = new Parcelable.Creator<Search>() {
        public Search createFromParcel(Parcel source) {
            return new Search(source);
        }

        public Search[] newArray(int size) {
            return new Search[size];
        }
    };
}
