package com.stevenschoen.imagesearchernew.model.google;

import android.os.Parcel;
import android.os.Parcelable;

public class GoogleImage implements Parcelable {
    public String title;
    public String link;
    public String displayLink;
    public String mime;
    public Image image;

    public static class Image implements Parcelable {
        public long height;
        public long width;
        public long byteSize;
        public String thumbnailLink;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.height);
            dest.writeLong(this.width);
            dest.writeLong(this.byteSize);
            dest.writeString(this.thumbnailLink);
        }

        public Image() { }

        private Image(Parcel in) {
            this.height = in.readLong();
            this.width = in.readLong();
            this.byteSize = in.readLong();
            this.thumbnailLink = in.readString();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.link);
        dest.writeString(this.displayLink);
        dest.writeString(this.mime);
        dest.writeParcelable(this.image, 0);
    }

    public GoogleImage() {
    }

    private GoogleImage(Parcel in) {
        this.title = in.readString();
        this.link = in.readString();
        this.displayLink = in.readString();
        this.mime = in.readString();
        this.image = in.readParcelable(Image.class.getClassLoader());
    }

    public static final Parcelable.Creator<GoogleImage> CREATOR = new Parcelable.Creator<GoogleImage>() {
        public GoogleImage createFromParcel(Parcel source) {
            return new GoogleImage(source);
        }

        public GoogleImage[] newArray(int size) {
            return new GoogleImage[size];
        }
    };
}