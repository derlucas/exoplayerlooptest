/*
 * Copyright (C) 2016 The Android Open Source Project
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
package de.testing.looper;

import android.os.SystemClock;
import android.util.Log;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.text.NumberFormat;
import java.util.Locale;

final class EventLogger implements Player.EventListener {

    private static final String TAG = "EventLogger";
    private static final NumberFormat TIME_FORMAT;

    static {
        TIME_FORMAT = NumberFormat.getInstance(Locale.US);
        TIME_FORMAT.setMinimumFractionDigits(2);
        TIME_FORMAT.setMaximumFractionDigits(2);
        TIME_FORMAT.setGroupingUsed(false);
    }

    private final long startTimeMs;

    private int changes = 0;

    EventLogger() {
        startTimeMs = SystemClock.elapsedRealtime();
    }


    @Override
    public void onLoadingChanged(boolean isLoading) {
//        Log.d(TAG, "loading [" + isLoading + "]");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        Log.d(TAG, "state [ sesstime: " + getSessionTimeString() + ", playwhenready: " + playWhenReady + ", state: " + getStateString(state) + "]");
    }

    @Override
    public void onRepeatModeChanged(@Player.RepeatMode int repeatMode) {
        Log.d(TAG, "repeatMode [" + getRepeatModeString(repeatMode) + "]");
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        Log.d(TAG, "shuffleModeEnabled [" + shuffleModeEnabled + "]");
    }

    @Override
    public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
        Log.d(TAG, "positionDiscontinuity [" + getDiscontinuityReasonString(reason) + "]");
        if(reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
            changes++;
            Log.d(TAG, "changes = " + changes);
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.d(TAG, "playbackParameters " + String.format(
                "[speed=%.2f, pitch=%.2f]", playbackParameters.speed, playbackParameters.pitch));
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException e) {
        Log.e(TAG, "onPlayerError [" + getSessionTimeString() + "]", e);
    }


    @Override
    public void onTracksChanged(TrackGroupArray ignored, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onSeekProcessed() {
//        Log.d(TAG, "seekProcessed");
    }


    private String getSessionTimeString() {
        return getTimeString(SystemClock.elapsedRealtime() - startTimeMs);
    }

    private static String getTimeString(long timeMs) {
        return timeMs == C.TIME_UNSET ? "?" : TIME_FORMAT.format((timeMs) / 1000f);
    }

    private static String getStateString(int state) {
        switch (state) {
            case Player.STATE_BUFFERING:
                return "BUFFERING";
            case Player.STATE_ENDED:
                return "ENDED";
            case Player.STATE_IDLE:
                return "IDLE";
            case Player.STATE_READY:
                return "READY";
            default:
                return "?";
        }
    }



    private static String getRepeatModeString(@Player.RepeatMode int repeatMode) {
        switch (repeatMode) {
            case Player.REPEAT_MODE_OFF:
                return "OFF";
            case Player.REPEAT_MODE_ONE:
                return "ONE";
            case Player.REPEAT_MODE_ALL:
                return "ALL";
            default:
                return "?";
        }
    }

    private static String getDiscontinuityReasonString(@Player.DiscontinuityReason int reason) {
        switch (reason) {
            case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
                return "PERIOD_TRANSITION";
            case Player.DISCONTINUITY_REASON_SEEK:
                return "SEEK";
            case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                return "SEEK_ADJUSTMENT";
            case Player.DISCONTINUITY_REASON_INTERNAL:
                return "INTERNAL";
            default:
                return "?";
        }
    }
}
