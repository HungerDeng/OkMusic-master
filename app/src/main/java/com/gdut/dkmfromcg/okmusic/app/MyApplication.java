package com.gdut.dkmfromcg.okmusic.app;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import com.gdut.dkmfromcg.commonlib.app.BaseApp;
import com.gdut.dkmfromcg.okmusic.model.SongInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dkmFromCG on 2018/3/8.
 * function:
 */

public class MyApplication extends BaseApp {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private List<MediaMetadataCompat> getLocalMusic(Context context){
        List<MediaMetadataCompat> mediaMetadataList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, null);
        if (cursor == null) {
            return null;
        }
        while (cursor.moveToNext()) {
            String id = null;
            String uri = cursor.getString(cursor.getColumnIndex(SongInfo.DATA));
            String title = cursor.getString(cursor.getColumnIndex(SongInfo.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(SongInfo.ARTIST));
            long duration = cursor.getLong(cursor.getColumnIndex(SongInfo.DURATION));
            String albumTitle = cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM));
            String albumIconUri = getAlbumArtPicPath(context, cursor.getString(cursor.getColumnIndex(SongInfo.ALBUM_ID)));
            String writer = null;
            String composer = cursor.getString(cursor.getColumnIndex(SongInfo.COMPOSER));
            String date = null;
            String year = cursor.getString(cursor.getColumnIndex(SongInfo.YEAR));
            String trackNum = cursor.getString(cursor.getColumnIndex(SongInfo.TRACK));
            String trackNumInOriginalSource = null;
            MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                    //A String key for identifying the content. This value is specific to the
                    //service providing the content. If used, this should be a persistent
                    // unique key for the underlying content.
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                    //A Uri formatted String representing the content.
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, uri)
                    //The title of the media.
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    //The artist of the media.
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    //The duration of the media in ms.
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                    // 专辑名称
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumTitle)
                    // 专辑图片
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumIconUri)
                    // 作词
                    .putString(MediaMetadataCompat.METADATA_KEY_WRITER, writer)
                    //作曲
                    .putString(MediaMetadataCompat.METADATA_KEY_COMPOSER, composer)
                    //The date the media was created or published.
                    .putString(MediaMetadataCompat.METADATA_KEY_DATE, date)
                    //The year the media was created or published as a long.
                    .putString(MediaMetadataCompat.METADATA_KEY_YEAR, year)
                    //The track number for the media.
                    .putString(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNum)
                    //The number of tracks in the media's original source
                    .putString(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, trackNumInOriginalSource)
                    .build();
            mediaMetadataList.add(metadata);
        }
        cursor.close();
        return mediaMetadataList;
    }

    //根据专辑 id 获得专辑图片保存路径
    private synchronized String getAlbumArtPicPath(Context context, String albumId) {

        if (TextUtils.isEmpty(albumId)) {
            return null;
        }

        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String imagePath = null;
        Uri uri = Uri.parse("content://media" + MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.getPath() + "/" + albumId);

        Cursor cur = context.getContentResolver().query(uri, projection, null, null, null);
        if (cur == null) {
            return null;
        }

        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            imagePath = cur.getString(0);
        }
        cur.close();
        return imagePath;
    }

}
