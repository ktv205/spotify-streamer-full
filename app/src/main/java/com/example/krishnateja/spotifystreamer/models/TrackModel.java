package com.example.krishnateja.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by krishnateja on 6/2/2015.
 */
public class TrackModel implements Parcelable {

    String trackName;
    String albumName;
    String smallImage;
    String largeImage;
    String preview;

    public TrackModel() {
    }

    public TrackModel(Parcel source) {
        trackName = source.readString();
        albumName = source.readString();
        smallImage = source.readString();
        largeImage = source.readString();
        preview = source.readString();
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(trackName);
        dest.writeString(albumName);
        dest.writeString(smallImage);
        dest.writeString(largeImage);
        dest.writeString(preview);

    }

    public static final Parcelable.Creator<TrackModel> CREATOR = new Parcelable.Creator<TrackModel>() {

        @Override
        public TrackModel createFromParcel(Parcel source) {
            return new TrackModel(source);
        }

        @Override
        public TrackModel[] newArray(int size) {
            return new TrackModel[size];
        }
    };
}
