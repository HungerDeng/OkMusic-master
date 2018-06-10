package com.gdut.dkmfromcg.playmusiclib.control;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.gdut.dkmfromcg.commonlib.util.log.Logger;
import com.gdut.dkmfromcg.playmusiclib.QueueManager;
import com.gdut.dkmfromcg.playmusiclib.R;
import com.gdut.dkmfromcg.playmusiclib.model.MusicProvider;
import com.gdut.dkmfromcg.playmusiclib.util.MediaIDHelper;
import com.gdut.dkmfromcg.playmusiclib.util.WearHelper;

/**
 * Created by dkmFromCG on 2018/5/7.
 * function: 连接 {@link com.gdut.dkmfromcg.playmusiclib.MusicService#mSession} 来更新状态
 * 连接{@link ExoPlayerActionImpl} 来控制播放
 */

public class PlayerManager implements IPlayerAction.Callback {

    private static final String TAG = "PlayerManager";
    // Action to thumbs up a media item
    //对某项媒体内容进行点赞
    private static final String CUSTOM_ACTION_THUMBS_UP = "com.example.android.uamp.THUMBS_UP";

    private MusicProvider mMusicProvider;
    private QueueManager mQueueManager;
    private Resources mResources;

    private IPlayerAction mPlayerAction;
    private PlayerServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;

    public PlayerManager(Context tx, PlayerServiceCallback serviceCallback, Resources resources,
                         MusicProvider musicProvider, QueueManager queueManager) {
        this.mServiceCallback = serviceCallback;
        this.mResources = resources;
        this.mMusicProvider = musicProvider;
        this.mQueueManager = queueManager;

        mPlayerAction = new ExoPlayerActionImpl(tx, musicProvider);
        mPlayerAction.setCallback(this);
        mMediaSessionCallback = new MediaSessionCallback();
    }

    public IPlayerAction getPlayerAction() {
        return mPlayerAction;
    }

    public MediaSessionCallback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    /*Implementation of the IPlayerAction.Callback interface start*/
    @Override
    public void onCompletion() {
        // The media player finished playing the current song, so we go ahead
        // and start the next.
        //当音乐播放器播完了当前歌曲，则继续播放下一首
        if (mQueueManager.skipQueuePosition(1)) {
            handlePlayRequest();
            mQueueManager.updateMetadata();
        } else {
            // If skipping was not possible, we stop and release the resources:
            //若不可能跳到下一首音乐进行播放，则停止并释放资源
            handleStopRequest(null);
        }
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    @Override
    public void setCurrentMediaId(String mediaId) {
        Logger.d(TAG, "setCurrentMediaId" + mediaId);
        mQueueManager.setQueueFromMusic(mediaId);
    }
    /*Implementation of the IPlayerAction.Callback interface end*/


    /**
     * 处理播放音乐的请求
     */
    public void handlePlayRequest() {
        Logger.d(TAG, "handlePlayRequest: mState=" + mPlayerAction.getState());
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            mServiceCallback.onPlayerStart();
            mPlayerAction.play(currentMusic);
        }
    }

    /**
     * Handle a request to pause music
     * 处理暂停音乐的请求
     */
    public void handlePauseRequest() {
        Logger.d(TAG, "handlePauseRequest: mState=" + mPlayerAction.getState());
        if (mPlayerAction.isPlaying()) {
            mPlayerAction.pause();
            mServiceCallback.onPlayerStop();
        }
    }

    /**
     * Handle a request to stop music
     * 处理停止音乐的请求
     *
     * @param withError Error message in case the stop has an unexpected cause. The error
     *                  message will be set in the PlaybackState and will be visible to
     *                  MediaController clients.
     */
    public void handleStopRequest(String withError) {
        Logger.d(TAG, "handleStopRequest: mState=" + mPlayerAction.getState() + " error=" + withError);
        mPlayerAction.stop(true);
        mServiceCallback.onPlayerStop();
        updatePlaybackState(withError);
    }

    /**
     * Update the current media player state, optionally showing an error message.
     * 更新当前媒体播放器的状态，可选择是否显示错误信息
     *
     * @param error 如果不为null，错误信息将呈现给用户.
     */
    public void updatePlaybackState(String error) {
        Logger.d(TAG, "updatePlaybackState, playback state=" + mPlayerAction.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayerAction != null && mPlayerAction.isConnected()) {
            position = mPlayerAction.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        setCustomAction(stateBuilder);
        int state = mPlayerAction.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }
        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        //如果当前索引是有效的
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getQueueId());
        }

        mServiceCallback.onPlayerStateUpdated(stateBuilder.build());

        if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED) {
            mServiceCallback.onNotificationRequired();
        }
    }

    /**
     * Switch to a different IPlayerAction instance, maintaining all playerActionImpl state, if possible.
     *
     * @param playerActionImpl switch to this IPlayerAction
     */
    public void switchToPlayback(IPlayerAction playerActionImpl, boolean resumePlaying) {
        if (playerActionImpl == null) {
            throw new IllegalArgumentException("Playback cannot be null");
        }
        // Suspends current state.
        int oldState = mPlayerAction.getState();
        long pos = mPlayerAction.getCurrentStreamPosition();
        String currentMediaId = mPlayerAction.getCurrentMediaId();
        mPlayerAction.stop(false);
        playerActionImpl.setCallback(this);
        playerActionImpl.setCurrentMediaId(currentMediaId);
        playerActionImpl.seekTo(pos < 0 ? 0 : pos);
        playerActionImpl.start();
        // Swaps instance.
        mPlayerAction = playerActionImpl;
        switch (oldState) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayerAction.pause();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
                if (resumePlaying && currentMusic != null) {
                    mPlayerAction.play(currentMusic);
                } else if (!resumePlaying) {
                    mPlayerAction.pause();
                } else {
                    mPlayerAction.stop(true);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            default:
                Logger.d(TAG, "Default called. Old state is " + oldState);
        }
    }

    //获取所有可用的动作命令
    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayerAction.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    /**
     * 设置自定义的操作
     */
    private void setCustomAction(PlaybackStateCompat.Builder stateBuilder) {
        MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
        if (currentMusic == null) {
            return;
        }
        // Set appropriate "Favorite" icon on Custom action:
        //在自定义操作中设置适当的"喜爱"图标
        String mediaId = currentMusic.getDescription().getMediaId();
        if (mediaId == null) {
            return;
        }
        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
        int favoriteIcon = mMusicProvider.isFavorite(musicId) ?
                R.drawable.ic_star_on : R.drawable.ic_star_off;
        Logger.d(TAG, "updatePlaybackState, setting Favorite custom action of music " +
                musicId + " current favorite=" + mMusicProvider.isFavorite(musicId));
        Bundle customActionExtras = new Bundle();
        WearHelper.setShowCustomActionOnWear(customActionExtras, true);
        stateBuilder.addCustomAction(new PlaybackStateCompat.CustomAction.Builder(
                CUSTOM_ACTION_THUMBS_UP, mResources.getString(R.string.favorite), favoriteIcon)
                .setExtras(customActionExtras)
                .build());
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        //点击播放按钮时触发
        //通过MediaControllerCompat.getTransportControls().play()触发
        @Override
        public void onPlay() {
            Logger.d(TAG, "play");
            if (mQueueManager.getCurrentMusic() == null) {
                mQueueManager.setRandomQueue();
            }
            handlePlayRequest();
        }

        //播放指定队列媒体时触发
        //通过MediaControllerCompat.getTransportControls().onSkipToQueueItem(queueId)触发
        @Override
        public void onSkipToQueueItem(long queueId) {
            Logger.d(TAG, "OnSkipToQueueItem:" + queueId);
            mQueueManager.setCurrentQueueItem(queueId);
            mQueueManager.updateMetadata();
        }

        //设置到指定进度时触发
        //通过MediaControllerCompat.getTransportControls().seekTo(position)触发
        @Override
        public void onSeekTo(long position) {
            Logger.d(TAG, "onSeekTo:" + position);
            mPlayerAction.seekTo((int) position);
        }

        //播放指定媒体数据时触发
        //通过MediaControllerCompat.getTransportControls().playFromMediaId(mediaItem.getMediaId(), null)触发
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Logger.d(TAG, "playFromMediaId mediaId:" + mediaId + "  extras=" + extras);
            mQueueManager.setQueueFromMusic(mediaId);
            handlePlayRequest();
        }

        //暂停时触发
        //通过MediaControllerCompat.getTransportControls().pause()触发
        @Override
        public void onPause() {
            Logger.d(TAG, "pause. current state=" + mPlayerAction.getState());
            handlePauseRequest();
        }

        //停止播放时触发
        //通过MediaControllerCompat.getTransportControls().stop()触发
        @Override
        public void onStop() {
            Logger.d(TAG, "stop. current state=" + mPlayerAction.getState());
            handleStopRequest(null);
        }

        //跳到下一首时触发
        //通过MediaControllerCompat.getTransportControls().skipToNext()触发
        @Override
        public void onSkipToNext() {
            Logger.d(TAG, "skipToNext");
            if (mQueueManager.skipQueuePosition(1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        //跳到上一首时触发
        //通过MediaControllerCompat.getTransportControls().skipToPrevious()触发
        @Override
        public void onSkipToPrevious() {
            if (mQueueManager.skipQueuePosition(-1)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip");
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            if (CUSTOM_ACTION_THUMBS_UP.equals(action)) {
                Logger.i(TAG, "onCustomAction: favorite for current track");
                MediaSessionCompat.QueueItem currentMusic = mQueueManager.getCurrentMusic();
                if (currentMusic != null) {
                    String mediaId = currentMusic.getDescription().getMediaId();
                    if (mediaId != null) {
                        String musicId = MediaIDHelper.extractMusicIDFromMediaID(mediaId);
                        mMusicProvider.setFavorite(musicId, !mMusicProvider.isFavorite(musicId));
                    }
                }
                // playback state needs to be updated because the "Favorite" icon on the
                // custom action will change to reflect the new favorite state.
                updatePlaybackState(null);
            } else {
                Logger.e(TAG, "Unsupported action: " + action);
            }
        }

        /**
         * Handle free and contextual searches.
         * <p/>
         * All voice searches on Android Auto are sent to this method through a connected
         * {@link android.support.v4.media.session.MediaControllerCompat}.
         * <p/>
         * Threads and async handling:
         * Search, as a potentially slow operation, should run in another thread.
         * <p/>
         * Since this method runs on the main thread, most apps with non-trivial metadata
         * should defer the actual search to another thread (for example, by using
         * an {@link AsyncTask} as we do here).
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            Logger.d(TAG, "playFromSearch  query=" + query + " extras=" + extras);

            mPlayerAction.setState(PlaybackStateCompat.STATE_CONNECTING);
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    if (!success) {
                        updatePlaybackState("Could not load catalog");
                    }

                    boolean successSearch = mQueueManager.setQueueFromSearch(query, extras);
                    if (successSearch) {
                        handlePlayRequest();
                        mQueueManager.updateMetadata();
                    } else {
                        updatePlaybackState("Could not find music");
                    }
                }
            });
        }
    }

    public interface PlayerServiceCallback {
        void onPlayerStart();

        void onNotificationRequired();

        void onPlayerStop();

        void onPlayerStateUpdated(PlaybackStateCompat newState);
    }

}
