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
package com.gdut.dkmfromcg.playmusiclib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.gdut.dkmfromcg.commonlib.util.log.Logger;
import com.gdut.dkmfromcg.playmusiclib.control.PlayerManager;
import com.gdut.dkmfromcg.playmusiclib.model.MusicProvider;
import com.gdut.dkmfromcg.playmusiclib.util.CarHelper;
import com.gdut.dkmfromcg.playmusiclib.util.WearHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a MediaBrowser through a service. It exposes the media library to a browsing
 * client, through the onGetRoot and onLoadChildren methods. It also creates a MediaSession and
 * exposes it through its MediaSession.Token, which allows the client to create a MediaController
 * that connects to and send control commands to the MediaSession remotely. This is useful for
 * user interfaces that need to interact with your media session, like Android Auto. You can
 * (should) also use the same service from your app's UI, which gives a seamless playback
 * experience to the user.
 * <p>
 * To implement a MediaBrowserService, you need to:
 * <p>
 * <ul>
 * <p>
 * <li> Extend {@link android.service.media.MediaBrowserService}, implementing the media browsing
 * related methods {@link android.service.media.MediaBrowserService#onGetRoot} and
 * {@link android.service.media.MediaBrowserService#onLoadChildren};
 * <li> In onCreate, start a new {@link android.media.session.MediaSession} and notify its parent
 * with the session's token {@link android.service.media.MediaBrowserService#setSessionToken};
 * <p>
 * <li> Set a callback on the
 * {@link android.media.session.MediaSession#setCallback(android.media.session.MediaSession.Callback)}.
 * The callback will receive all the user's actions, like play, pause, etc;
 * <p>
 * <li> Handle all the actual music playing using any method your app prefers (for example,
 * {@link android.media.MediaPlayer})
 * <p>
 * <li> Update playbackState, "now playing" metadata and queue, using MediaSession proper methods
 * {@link android.media.session.MediaSession#setPlaybackState(android.media.session.PlaybackState)}
 * {@link android.media.session.MediaSession#setMetadata(android.media.MediaMetadata)} and
 * {@link android.media.session.MediaSession#setQueue(java.util.List)})
 * <p>
 * <li> Declare and export the service in AndroidManifest with an intent receiver for the action
 * android.media.browse.MediaBrowserService
 * <p>
 * </ul>
 * <p>
 * To make your app compatible with Android Auto, you also need to:
 * <p>
 * <ul>
 * <p>
 * <li> Declare a meta-data tag in AndroidManifest.xml linking to a xml resource
 * with a &lt;automotiveApp&gt; root element. For a media app, this must include
 * an &lt;uses name="media"/&gt; element as a child.
 * For example, in AndroidManifest.xml:
 * &lt;meta-data android:name="com.google.android.gms.car.application"
 * android:resource="@xml/automotive_app_desc"/&gt;
 * And in res/values/automotive_app_desc.xml:
 * &lt;automotiveApp&gt;
 * &lt;uses name="media"/&gt;
 * &lt;/automotiveApp&gt;
 * <p>
 * </ul>
 * <p>
 * function: 主要处理MediaSession的状态的变化,更新数据的变化(QueueManager随MediaSession数据的变化而变化),
 * 接受广播(汽车/通知栏),自身不在活跃状态时停止自身Service.
 */
public class MusicService extends MediaBrowserServiceCompat implements PlayerManager.PlayerServiceCallback {

    private static final String TAG = "MusicService";

    // Extra on MediaSession that contains the Cast device name currently connected to
    public static final String EXTRA_CONNECTED_CAST = "com.example.android.uamp.CAST_NAME";
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.example.android.uamp.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    /**
     * data
     */
    private MusicProvider mMusicProvider;//音乐数据
    private QueueManager mQueueManager;
    private PlayerManager mPlayerManager;

    private MediaSessionCompat mSession;
    private Bundle mSessionExtras;


    private PackageValidator mPackageValidator;

    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);

    /**
     * 连接到车时
     */
    private boolean mIsConnectedToCar;
    private BroadcastReceiver mCarConnectionReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicProvider = new MusicProvider();
        mMusicProvider.retrieveMediaAsync(null); //为了使应用程序更快地响应，在这就直接获取并缓存目录信息,这有助于提高方法中的响应时间
        //实现数据的更新
        mQueueManager = new QueueManager(mMusicProvider, getResources(), new QueueManager.MetadataUpdateListener() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                mSession.setMetadata(metadata);
            }

            @Override
            public void onMetadataRetrieveError() {
                mPlayerManager.updatePlaybackState(
                        getString(R.string.error_no_metadata));
            }

            @Override
            public void onCurrentQueueIndexUpdated(int queueIndex) {
                mPlayerManager.handlePlayRequest();
            }

            @Override
            public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                mSession.setQueue(newQueue);
                mSession.setQueueTitle(title);
            }
        });
        mPlayerManager = new PlayerManager(this, this, getResources(), mMusicProvider, mQueueManager);
        mPlayerManager.updatePlaybackState(null);

        mSession = new MediaSessionCompat(this, TAG);
        //把MediaSession的令牌传递给MediaBrowserService
        //这将连接MediaBrowserService到MediaSession，并将允许MediaBrowser（客户端)使用MediaSession
        this.setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mPlayerManager.getMediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);


        //......
        mSessionExtras = new Bundle();
        CarHelper.setSlotReservationFlags(mSessionExtras, true, true, true);
        WearHelper.setSlotReservationFlags(mSessionExtras, true, true);
        WearHelper.setUseBackgroundFromTheme(mSessionExtras, true);
        mSession.setExtras(mSessionExtras);

        mPackageValidator = new PackageValidator(this);
        registerCarConnectionReceiver();
    }

    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlayerManager.handlePauseRequest();
                } else if (CMD_STOP_CASTING.equals(command)) {
                    //CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(mSession, startIntent);
            }
        }

        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    /*
     * Handle case when user swipes the app away from the recents apps list by
     * stopping the service (and any ongoing playback).
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        unregisterCarConnectionReceiver();
        // Service is being killed, so make sure we release our resources
        mPlayerManager.handleStopRequest(null);
        /*mMediaNotificationManager.stopNotification();

        if (mCastSessionManager != null) {
            mCastSessionManager.removeSessionManagerListener(mCastSessionManagerListener,
                    CastSession.class);
        }*/
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mSession.release();
        //super.onDestroy();

    }

    /**
     * Called to get the root information for browsing by a particular client.
     * 被用来获取特定客户端浏览的根信息
     * <p>
     * The implementation should verify that the client package has permission
     * to access browse media information before returning the root id; it
     * should return null if the client is not allowed to access this
     * information.
     * 实现应该验证客户端包是否有权限访问浏览媒体信息，然后返回根id;
     * 如果客户端不被允许访问该信息，则应返回null
     * </p>
     *
     * @param clientPackageName The package name of the application which is
     *                          requesting access to browse media. 请求访问浏览 browse media 的应用程序的包名
     * @param clientUid         The uid of the application which is requesting access to
     *                          browse media.     请求访问浏览 browse media 的应用程序的uid
     * @param rootHints         An optional bundle of service-specific arguments to send
     *                          to the media browse service when connecting and retrieving the
     *                          root id for browsing, or null if none. The contents of this
     *                          bundle may affect the information returned when browsing.
     * @return The {@link BrowserRoot} for accessing this app's content or null.
     * @see BrowserRoot#EXTRA_RECENT
     * @see BrowserRoot#EXTRA_OFFLINE
     * @see BrowserRoot#EXTRA_SUGGESTED
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Logger.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName +
                "; clientUid=" + clientUid + " ; rootHints=" + rootHints);
        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return an empty browser root.
            // If you return null, then the media browser will not be able to connect and
            // no further calls will be made to other media browsing methods.
            Logger.i(TAG, "OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
                    + "Returning empty browser root so all apps can use MediaController."
                    + clientPackageName);
            return new MediaBrowserServiceCompat.BrowserRoot(MediaID.MEDIA_ID_EMPTY_ROOT, null);
        }
        //noinspection StatementWithEmptyBody
        if (CarHelper.isValidCarPackage(clientPackageName)) {
            // Optional: if your app needs to adapt the music library to show a different subset
            // when connected to the car, this is where you should handle it.
            // If you want to adapt other runtime behaviors, like tweak ads or change some behavior
            // that should be different on cars, you should instead use the boolean flag
            // set by the BroadcastReceiver mCarConnectionReceiver (mIsConnectedToCar).
        }
        //noinspection StatementWithEmptyBody
        if (WearHelper.isValidWearCompanionPackage(clientPackageName)) {
            // Optional: if your app needs to adapt the music library for when browsing from a
            // Wear device, you should return a different MEDIA ROOT here, and then,
            // on onLoadChildren, handle it accordingly.
        }

        return new BrowserRoot(MediaID.MEDIA_ID_ROOT, null);
    }

    /**
     * Called to get information about the children of a media item.
     * <p>
     * Implementations must call {@link Result#sendResult result.sendResult}
     * with the list of children. If loading the children will be an expensive
     * operation that should be performed on another thread,
     * {@link Result#detach result.detach} may be called before returning from
     * this function, and then {@link Result#sendResult result.sendResult}
     * called when the loading is complete.
     * </p><p>
     * In case the media item does not have any children, call {@link Result#sendResult}
     * with an empty list. When the given {@code parentId} is invalid, implementations must
     * call {@link Result#sendResult result.sendResult} with {@code null}, which will invoke
     * {@link MediaBrowserCompat.SubscriptionCallback#onError}.
     * </p>
     *
     * @param parentMediaId The id of the parent media item whose children are to be
     *                      queried.
     * @param result        The Result to send the list of children to.
     */
    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        Logger.d(TAG, "OnLoadChildren: parentMediaId=" + parentMediaId);
        if (MediaID.MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            //如果音乐库已经准备好了，立即将数据发送至客户端
            result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
        } else {
            //音乐数据检索完毕后返回结果
            result.detach();
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                //完成音乐加载后的回调
                @Override
                public void onMusicCatalogReady(boolean success) {
                    result.sendResult(mMusicProvider.getChildren(parentMediaId, getResources()));
                }
            });
        }
    }

    /**
     * 实现 {@link PlayerManager.PlayerServiceCallback} 方法开始
     */
    @Override
    public void onPlayerStart() {
        mSession.setActive(true);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        //即使绑定的客户端（通常是指MediaController）断开连接了，Service也需要继续运行，否则音乐将会停止播放。
        //调用startService(Intent)将保持Service持续运行直到明确要将服务杀掉为止
        startService(new Intent(getApplicationContext(), MusicService.class));
    }

    @Override
    public void onNotificationRequired() {

    }


    @Override
    public void onPlayerStop() {
        mSession.setActive(false);
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        //重置延迟停止的Handler，所以收到 STOP_DELAY 消息后将再次执行
        //有可能会停止Service
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onPlayerStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }
    /* 实现 {@link PlayerManager.PlayerServiceCallback} 方法结束*/


    private void registerCarConnectionReceiver() {
        IntentFilter filter = new IntentFilter(CarHelper.ACTION_MEDIA_STATUS);
        mCarConnectionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String connectionEvent = intent.getStringExtra(CarHelper.MEDIA_CONNECTION_STATUS);
                mIsConnectedToCar = CarHelper.MEDIA_CONNECTED.equals(connectionEvent);
                Logger.i(TAG, "Connection event to Android Auto: " + connectionEvent +
                        " isConnectedToCar=" + mIsConnectedToCar);
            }
        };
        registerReceiver(mCarConnectionReceiver, filter);
    }

    private void unregisterCarConnectionReceiver() {
        unregisterReceiver(mCarConnectionReceiver);
    }

    /**
     * A simple handler that stops the service if PlayAction is not active (playing)
     * 当 PlayAction 不在活跃状态时停止服务
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mPlayerManager.getPlayerAction() != null) {
                if (service.mPlayerManager.getPlayerAction().isPlaying()) {
                    Logger.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Logger.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }
}
