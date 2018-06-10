package com.gdut.dkmfromcg.playmusiclib.model;

import android.support.v4.media.MediaMetadataCompat;

import java.util.Iterator;

/**
 * Created by dkmFromCG on 2018/5/7.
 * function:
 */

public interface IMusicProviderSource {

    String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    Iterator<MediaMetadataCompat> iterator();
}
