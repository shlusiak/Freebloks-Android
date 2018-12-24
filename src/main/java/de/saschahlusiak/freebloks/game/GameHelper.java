/*
 * Copyright (C) 2013 Google Inc.
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

package de.saschahlusiak.freebloks.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import de.saschahlusiak.freebloks.R;

public class GameHelper implements OnCompleteListener<Player>, OnSuccessListener<GoogleSignInAccount> {

    private static final String tag = "GameHelper";

    /** Listener for sign-in success or failure events. */
    public interface GameHelperListener {
        /**
         * Called when the user is signed out
         */
        void onSignInFailed();

        /**
         * Called when the user is signed in
         */
        void onSignInSucceeded();
    }

    // configuration done?
    private boolean mSetupDone = false;

    /**
     * The Activity we are bound to. We need to keep a reference to the Activity
     * because some games methods require an Activity (a Context won't do). We
     * are careful not to leak these references: we release them on onStop().
     */
    private Activity mActivity;

    // The request code when requesting sign in
    private final static int RC_START_SIGN_IN = 9001;

    private GamesClient gamesClient;
    private GoogleSignInClient googleSignInClient;
    private LeaderboardsClient leaderboardsClient;
    private AchievementsClient achievementsClient;
    private PlayersClient playersClient;
    private GoogleSignInAccount googleAccount;
    private Player currentPlayer;

    // Listener
    private GameHelperListener mListener = null;

    public GameHelper(Activity activity) {
        mActivity = activity;
    }

    private void assertConfigured(String operation) {
        if (!mSetupDone) {
            String error = "GameHelper error: Operation attempted without setup: "
                    + operation
                    + ". The setup() method must be called before attempting any other operation.";
            Log.e(tag, error);
            throw new IllegalStateException(error);
        }
    }

    /**
     * Performs setup on this GameHelper object. Call this from the onCreate()
     * method of your Activity. This will create the clients and do a few other
     * initialization tasks. Next, call @link{#onStart} from the onStart()
     * method of your Activity.
     *
     * @param listener
     *            The listener to be notified of sign-in events.
     */
    public void setup(GameHelperListener listener) {
        if (mSetupDone) {
            String error = "GameHelper: you cannot call GameHelper.setup() more than once!";
            Log.e(tag, error);
            throw new IllegalStateException(error);
        }
        mListener = listener;
        Log.d(tag, "Setup");

        mSetupDone = true;
    }

    public void doSilentSignIn() {
        if (googleSignInClient != null) {
            googleSignInClient.silentSignIn().addOnSuccessListener(this);
        }
    }

    @Override
    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
        Log.i(tag, "Sign in with Google success");
        setGoogleAccount(googleSignInAccount);
    }

    public void beginUserInitiatedSignIn() {
        Log.w(tag, "Starting sign in to Google Play Games");
        Intent intent = googleSignInClient.getSignInIntent();
        mActivity.startActivityForResult(intent, RC_START_SIGN_IN);
    }

    private void setGoogleAccount(GoogleSignInAccount account) {
        if (account == this.googleAccount)
            return;
        if (mActivity == null)
        	return;

        this.googleAccount = account;

        if (this.googleAccount != null) {
            leaderboardsClient = Games.getLeaderboardsClient(mActivity, googleAccount);
            achievementsClient = Games.getAchievementsClient(mActivity, googleAccount);
            gamesClient = Games.getGamesClient(mActivity, googleAccount);

            Window wnd = mActivity.getWindow();
            if (wnd != null && wnd.getDecorView() != null) {
				gamesClient.setViewForPopups(wnd.getDecorView());
			}

            playersClient = Games.getPlayersClient(mActivity, googleAccount);

            playersClient.getCurrentPlayer().addOnCompleteListener(this);
        } else {
            leaderboardsClient = null;
            achievementsClient = null;
            gamesClient = null;
            currentPlayer = null;

            if (mListener != null) {
                mListener.onSignInFailed();
            }
        }
    }

    @Override
    public void onComplete(@NonNull Task<Player> task) {
        if (task.isSuccessful()) {
            this.currentPlayer = task.getResult();
            if (mListener != null) {
                mListener.onSignInSucceeded();
            }
        } else {
            if (mListener != null) {
            	// silent login failed, no message necessary
//                String message = null;
//                if (task.getException() != null)
//                    message = task.getException().getMessage();

                mListener.onSignInFailed();
            }
            startSignOut();
        }
    }

    public void startSignOut() {
        final OnCompleteListener<Void> onSignOutCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setGoogleAccount(null);
            }
        };
        googleSignInClient.signOut().addOnCompleteListener(onSignOutCompleteListener);
    }

    public GoogleSignInAccount getGoogleAccount() {
        return googleAccount;
    }

    public boolean isSignedIn() {
        return mSetupDone && googleAccount != null && currentPlayer != null;
    }

    /** Call this method from your Activity's onStart(). */
    public void onStart(Activity act) {
        mActivity = act;

        Log.d(tag, "onStart");
        assertConfigured("onStart");

        if (!isSignedIn()) {
            googleSignInClient = GoogleSignIn.getClient(mActivity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

            GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(mActivity);

            if (lastAccount == null) {
                doSilentSignIn();
            } else {
                setGoogleAccount(lastAccount);
            }
        } else {
			if (mListener != null) {
				mListener.onSignInFailed();
			}
		}
    }

    /** Call this method from your Activity's onStop(). */
    public void onStop() {
        Log.d(tag, "onStop");
        assertConfigured("onStop");

        // let go of the Activity reference
        mActivity = null;
        googleSignInClient = null;
        currentPlayer = null;
     	googleAccount = null;
    }

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public void unlock(String achievement) {
    	if (!isSignedIn())
    		return;
    	if (achievementsClient == null)
    		return;

    	achievementsClient.unlock(achievement);
	}

	public void increment(String achievement, int increment) {
		if (!isSignedIn())
			return;
		if (achievementsClient == null)
			return;

		achievementsClient.increment(achievement, increment);
	}

	public void submitScore(String leaderboard, long score) {
		if (!isSignedIn())
			return;
		if (achievementsClient == null)
			return;

		leaderboardsClient.submitScore(leaderboard, score);
	}

	public void startAchievementsIntent(final int requestCode) {
    	assertConfigured("achievements");
		if (achievementsClient == null)
			return;
		achievementsClient.getAchievementsIntent().addOnSuccessListener(
			new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess(Intent intent) {
					mActivity.startActivityForResult(intent, requestCode);
				}
			}
		);
	}

	public void startLeaderboardIntent(String leaderboard, final int requestCode) {
		assertConfigured("achievements");
		if (leaderboardsClient == null)
			return;
		leaderboardsClient.getLeaderboardIntent(leaderboard).addOnSuccessListener(
			new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess(Intent intent) {
					mActivity.startActivityForResult(intent, requestCode);
				}
			}
		);
	}

    /**
     * Handle activity result. Call this method from your Activity's
     * onActivityResult callback. If the activity result pertains to the sign-in
     * process, processes it appropriately.
     */
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {
        Log.d(tag, "onActivityResult: req="
                + requestCode
                + ", resp="
                + responseCode);

        if (requestCode != RC_START_SIGN_IN) {
            Log.d(tag, "onActivityResult: request code not meant for us. Ignoring.");
            return;
        }

//        if (responseCode == Activity.RESULT_CANCELED) {
//             user cancel
//            return;
//        }

        final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            setGoogleAccount(account);
        } else {
            Log.e(tag, "Sign in result: " + result.getStatus());
            String message = result.getStatus().getStatusMessage();
            if (message == null)
            	message = mActivity.getString(R.string.playgames_sign_in_failed);

            if (mListener != null) {
                mListener.onSignInFailed();

                if (message != null) {
                	makeSimpleDialog(message).show();
				}
            }
        }
    }

    private static Dialog makeSimpleDialog(Activity activity, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setNeutralButton(android.R.string.ok, null).create();
    }

    private static Dialog makeSimpleDialog(Activity activity, String title, String text) {
        return (new AlertDialog.Builder(activity)).setMessage(text)
                .setTitle(title).setNeutralButton(android.R.string.ok, null)
                .create();
    }

    private Dialog makeSimpleDialog(String text) {
        if (mActivity == null) {
            Log.e(tag, "*** makeSimpleDialog failed: no current Activity!");
            return null;
        }
        return makeSimpleDialog(mActivity, text);
    }

    private Dialog makeSimpleDialog(String title, String text) {
        if (mActivity == null) {
            Log.e(tag, "*** makeSimpleDialog failed: no current Activity!");
            return null;
        }
        return makeSimpleDialog(mActivity, title, text);
    }
}
