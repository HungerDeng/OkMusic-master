/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gdut.dkmfromcg.playmusiclib.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import com.gdut.dkmfromcg.commonlib.util.log.Logger;
import com.gdut.dkmfromcg.commonlib.util.network.CheckNetwork;
import com.gdut.dkmfromcg.playmusiclib.MusicService;
import com.gdut.dkmfromcg.playmusiclib.R;

/**
 * Base activity for activities that need to show a playback control fragment when media is playing.
 * 继承BasePlayMusicActivity的 XML布局必须要有PlaybackControlsFragment,其id为 fragment_playback_controls
 */

public class BasePlayMusicActivity extends AppCompatActivity implements IMediaBrowserProvider {

    private static final String TAG = "BasePlayMusicActivity";

    private MediaBrowserCompat mMediaBrowser;
    private PlaybackControlsFragment mControlsFragment;

    /**
     * 连接媒体浏览服务成功后的回调接口
     */
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Logger.d(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        Logger.e(TAG, "could not connect media controller" + Logger.getStackTraceString(e));
                        hidePlaybackControls();
                    }
                }
            };

    // Callback that ensures that we are showing the controls
    /**
     * 媒体控制器控制播放过程中的回调接口
     * 这里主要是根据当前播放的状态决定PlaybackControlsFragment是否显示
     */
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        Logger.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " +
                                "hiding controls because state is " + state.getState());
                        hidePlaybackControls();
                    }
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        Logger.d(TAG, "mediaControllerCallback.onMetadataChanged: " +
                                "hiding controls because metadata is null");
                        hidePlaybackControls();
                    }
                }
            };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        //创建媒体浏览客户端（MediaBrowserCompat）
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mControlsFragment = (PlaybackControlsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_playback_controls);
        if (mControlsFragment == null) {
            throw new IllegalStateException("Mising fragment with id 'controls'. Cannot continue.");
        }
        hidePlaybackControls();
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCallback);
        }
        mMediaBrowser.disconnect();
    }


    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    protected void onMediaControllerConnected() {
        // empty implementation, can be overridden by clients.
    }

    /**
     * @param token
     * @throws RemoteException
     */
    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mMediaControllerCallback);

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            Logger.d(TAG, "connectionCallback.onConnected: " +
                    "hiding controls because metadata is null");
            hidePlaybackControls();
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }

        onMediaControllerConnected();
    }

    /**
     * Check if the MediaSession is active and in a "playback-able" state
     * (not NONE and not STOPPED).
     *
     * @return true if the MediaSession's state requires playback controls to be visible.
     */
    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    /**
     * 显示音频控制器fragment
     */
    protected void showPlaybackControls() {
        Logger.d(TAG, "showPlaybackControls");
        if (CheckNetwork.isNetworkConnected(this)) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                            R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                    .show(mControlsFragment)
                    .commit();
        }
    }

    /**
     * 隐藏音频控制器fragment
     */
    protected void hidePlaybackControls() {
        Logger.d(TAG, "hidePlaybackControls");
        getSupportFragmentManager().beginTransaction()
                .hide(mControlsFragment)
                .commit();
    }
}
