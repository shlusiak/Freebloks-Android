package de.saschahlusiak.freebloks.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class BaseGameActivity extends Activity implements GameHelper.GameHelperListener {

    // The game helper object. This class is mainly a wrapper around this object.
    protected GameHelper mHelper;

    private final static String TAG = "BaseGameActivity";

    /** Constructs a BaseGameActivity with default client (GamesClient). */
    protected BaseGameActivity() {
        super();
    }

    public GameHelper getGameHelper() {
        if (mHelper == null) {
            mHelper = new GameHelper(this);
        }
        return mHelper;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (mHelper == null) {
            getGameHelper();
        }
        mHelper.setup(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHelper.onStop();
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mHelper.onActivityResult(request, response, data);
    }

    protected boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    protected void beginUserInitiatedSignIn() {
        mHelper.beginUserInitiatedSignIn();
    }

    protected void signOut() {
        mHelper.startSignOut();
    }

    public void startAchievementsIntent(final int requestCode) {
        getGameHelper().startAchievementsIntent(requestCode);
    }

    public void startLeaderboardIntent(String leaderboard, final int requestCode) {
        getGameHelper().startLeaderboardIntent(leaderboard, requestCode);
    }

    public void unlock(String achievement) {
        mHelper.unlock(achievement);
    }

    public void increment(String achievement, int increment) {
        mHelper.increment(achievement, increment);
    }

    public void submitScore(String leaderboard, long score) {
        mHelper.submitScore(leaderboard, score);
    }
}
