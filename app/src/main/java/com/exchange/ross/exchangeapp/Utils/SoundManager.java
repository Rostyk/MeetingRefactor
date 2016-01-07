package com.exchange.ross.exchangeapp.Utils;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by ross on 5/1/15.
 */
@SuppressWarnings("deprecation")
public class SoundManager {

    private static SoundManager instance;
    public static synchronized SoundManager sharedManager() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void mute(Boolean on) {
        Context context = ApplicationContextProvider.getContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (context != null) {
            if (on) {
                if(Settings.getVibration()) {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
                else {
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
            }
            else {
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
            }
        }
    }
}
