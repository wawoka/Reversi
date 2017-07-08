package jp.techacademy.atsushi.kanamori.reversi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class GameActivity extends Activity {

    ReversiView reversiview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_game);
        Utils.d("GameActivity.onCreate");

        reversiview = new ReversiView(this);
        setContentView(reversiview);
    }

    @Override
    protected void onPause() {
        Utils.d("GameActivity.onPause");
        Pref.setState(this.getApplicationContext(), reversiview.getState());

        reversiview.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Utils.d("GameActivity.onResume");
        reversiview.resume(Pref.getState(this.getApplicationContext()));
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuExit:
                finish();
                break;
            case R.id.mnuPref:
                openPref();
                break;
            case R.id.mnuStat:
                reversiview.showCountsToast();
                break;
            case R.id.mnuInit:
                reversiview.init(true);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPref() {
        Intent intent = new Intent(this, Pref.class);
        startActivity(intent);
    }
}
