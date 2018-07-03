package com.sun.easysnackbar;


/**
 * Manages {@link Snackbar}s.
 */

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Manages {@link EasySnackBar}s.
 */
class SnackBarManager {

    static final int MSG_TIMEOUT = 0;

    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS = 2750;

    private static SnackBarManager sSnackbarManager;

    static SnackBarManager getInstance() {
        if (sSnackbarManager == null) {
            sSnackbarManager = new SnackBarManager();
        }
        return sSnackbarManager;
    }

    private final Object mLock;
    private final Handler mHandler;

    private SnackBarRecord mCurrentSnackbar;
    private SnackBarRecord mNextSnackbar;

    private SnackBarManager() {
        mLock = new Object();
        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_TIMEOUT:
                        handleTimeout((SnackBarRecord) message.obj);
                        return true;
                }
                return false;
            }
        });
    }

    interface Callback {
        void show();
        void dismiss(int event);
    }

    public void show(int duration, Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                // Means that the callback is already in the queue. We'll just update the duration
                mCurrentSnackbar.duration = duration;

                // If this is the Snackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
                scheduleTimeoutLocked(mCurrentSnackbar);
                return;
            } else if (isNextSnackBarLocked(callback)) {
                // We'll just update the duration
                mNextSnackbar.duration = duration;
            } else {
                // Else, we need to create a new record and queue it
                mNextSnackbar = new SnackBarRecord(duration, callback);
            }

            if (mCurrentSnackbar != null && cancelSnackbarLocked(mCurrentSnackbar,
                    EasySnackBar.Callback.DISMISS_EVENT_CONSECUTIVE)) {
                // If we currently have a Snackbar, try and cancel it and wait in line
                return;
            } else {
                // Clear out the current snackbar
                mCurrentSnackbar = null;
                // Otherwise, just show it now
                showNextSnackBarLocked();
            }
        }
    }

    public void dismiss(Callback callback, int event) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                cancelSnackbarLocked(mCurrentSnackbar, event);
            } else if (isNextSnackBarLocked(callback)) {
                cancelSnackbarLocked(mNextSnackbar, event);
            }
        }
    }

    /**
     * Should be called when a Snackbar is no longer displayed. This is after any exit
     * animation has finished.
     */
    public void onDismissed(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                // If the callback is from a Snackbar currently show, remove it and show a new one
                mCurrentSnackbar = null;
                if (mNextSnackbar != null) {
                    showNextSnackBarLocked();
                }
            }
        }
    }

    /**
     * Should be called when a Snackbar is being shown. This is after any entrance animation has
     * finished.
     */
    public void onShown(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    public void pauseTimeout(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback) && !mCurrentSnackbar.paused) {
                mCurrentSnackbar.paused = true;
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar);
            }
        }
    }

    public void restoreTimeoutIfPaused(Callback callback) {
        synchronized (mLock) {
            if (isCurrentSnackbarLocked(callback) && mCurrentSnackbar.paused) {
                mCurrentSnackbar.paused = false;
                scheduleTimeoutLocked(mCurrentSnackbar);
            }
        }
    }

    public boolean isCurrent(Callback callback) {
        synchronized (mLock) {
            return isCurrentSnackbarLocked(callback);
        }
    }

    public boolean isCurrentOrNext(Callback callback) {
        synchronized (mLock) {
            return isCurrentSnackbarLocked(callback) || isNextSnackBarLocked(callback);
        }
    }

    private static class SnackBarRecord {
        final WeakReference<Callback> callback;
        int duration;
        boolean paused;

        SnackBarRecord(int duration, Callback callback) {
            this.callback = new WeakReference<>(callback);
            this.duration = duration;
        }

        boolean isSnackBar(Callback callback) {
            return callback != null && this.callback.get() == callback;
        }
    }

    private void showNextSnackBarLocked() {
        if (mNextSnackbar != null) {
            mCurrentSnackbar = mNextSnackbar;
            mNextSnackbar = null;

            final Callback callback = mCurrentSnackbar.callback.get();
            if (callback != null) {
                callback.show();
            } else {
                // The callback doesn't exist any more, clear out the Snackbar
                mCurrentSnackbar = null;
            }
        }
    }

    private boolean cancelSnackbarLocked(SnackBarRecord record, int event) {
        final Callback callback = record.callback.get();
        if (callback != null) {
            // Make sure we remove any timeouts for the SnackBarRecord
            mHandler.removeCallbacksAndMessages(record);
            callback.dismiss(event);
            return true;
        }
        return false;
    }

    private boolean isCurrentSnackbarLocked(Callback callback) {
        return mCurrentSnackbar != null && mCurrentSnackbar.isSnackBar(callback);
    }

    private boolean isNextSnackBarLocked(Callback callback) {
        return mNextSnackbar != null && mNextSnackbar.isSnackBar(callback);
    }

    private void scheduleTimeoutLocked(SnackBarRecord r) {
        if (r.duration == EasySnackBar.LENGTH_INDEFINITE) {
            // If we're set to indefinite, we don't want to set a timeout
            return;
        }

        int durationMs = LONG_DURATION_MS;
        if (r.duration > 0) {
            durationMs = r.duration;
        } else if (r.duration == EasySnackBar.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS;
        }
        mHandler.removeCallbacksAndMessages(r);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), durationMs);
    }

    void handleTimeout(SnackBarRecord record) {
        synchronized (mLock) {
            if (mCurrentSnackbar == record || mNextSnackbar == record) {
                cancelSnackbarLocked(record, EasySnackBar.Callback.DISMISS_EVENT_TIMEOUT);
            }
        }
    }

}

