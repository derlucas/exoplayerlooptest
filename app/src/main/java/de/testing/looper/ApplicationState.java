package de.testing.looper;

import android.app.Application;
import android.util.Log;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.upstream.cache.*;
import com.google.android.exoplayer2.util.Util;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ApplicationState extends Application {
    private static final String TAG = "ApplicationState";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";

    private static OkHttpClient sOkHttpClient;

    private String userAgent;
    private File downloadDirectory;
    private Cache downloadCache;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "starting application");
        userAgent = Util.getUserAgent(this, "mediaPlayerSample");

        sOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .readTimeout(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .followSslRedirects(false)
                .build();
    }


    DataSource.Factory buildDataSourceFactory(TransferListener<? super DataSource> listener) {
        DefaultDataSourceFactory upstreamFactory = new DefaultDataSourceFactory(this, listener, buildHttpDataSourceFactory(listener));
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(TransferListener<? super DataSource> listener) {
        return new OkHttpDataSourceFactory(sOkHttpClient, userAgent, listener);
    }


    private synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(DefaultDataSourceFactory upstreamFactory, Cache cache) {

        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }


}
