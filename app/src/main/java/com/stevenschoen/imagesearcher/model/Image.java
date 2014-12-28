package com.stevenschoen.imagesearcher.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    public String title;
    public String link;
    public String thumbnailLink;
    public String displayLink;
    public String mimeType;
    public long width, height;
    public long size;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.link);
        dest.writeString(this.thumbnailLink);
        dest.writeString(this.displayLink);
        dest.writeString(this.mimeType);
        dest.writeLong(this.width);
        dest.writeLong(this.height);
        dest.writeLong(this.size);
    }

    public Image() { }

    private Image(Parcel in) {
        this.title = in.readString();
        this.link = in.readString();
        this.thumbnailLink = in.readString();
        this.displayLink = in.readString();
        this.mimeType = in.readString();
        this.width = in.readLong();
        this.height = in.readLong();
        this.size = in.readLong();
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
