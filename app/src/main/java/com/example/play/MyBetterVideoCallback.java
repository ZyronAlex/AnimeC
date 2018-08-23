package com.example.play;

import android.widget.SeekBar;

import com.halilibo.bettervideoplayer.BetterVideoCallback;

public interface MyBetterVideoCallback extends BetterVideoCallback {

    void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

    void onStartTrackingTouch(SeekBar seekBar);

    void onStopTrackingTouch(SeekBar seekBar);

}
