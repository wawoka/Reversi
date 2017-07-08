package jp.techacademy.atsushi.kanamori.reversi.model;

import android.graphics.Point;

public abstract interface IPlayerCallback {

    public void onEndThinking(Point pos);
    public void onProgress();
    public void onPointStarted(Point pos);
    public void onPointEnded(Point pos);
}
