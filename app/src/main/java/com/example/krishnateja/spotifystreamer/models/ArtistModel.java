package com.example.krishnateja.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by krishnateja on 6/1/2015.
 */
public class ArtistModel implements Parcelable {

    String name;
    String image;
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArtistModel() {
    }

    public ArtistModel(Parcel source) {
        name = source.readString();
        image = source.readString();
        id=source.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(image);
        dest.writeString(id);
    }

    public static final Parcelable.Creator<ArtistModel> CREATOR = new Parcelable.Creator<ArtistModel>() {
        @Override
        public ArtistModel createFromParcel(Parcel source) {
            return new ArtistModel(source);
        }

        @Override
        public ArtistModel[] newArray(int size) {
            return new ArtistModel[size];
        }
    };
}
