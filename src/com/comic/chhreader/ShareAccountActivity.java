package com.comic.chhreader;

import com.comic.chhreader.evernoteshare.ShareToEvernote;
import com.evernote.client.android.EvernoteSession;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

public class ShareAccountActivity extends PreferenceActivity implements OnPreferenceClickListener {

	private static final String EVERNOTE = "evernote";

	private Preference mEvernotePreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setPreferenceScreen(createPreferenceHierarchy());
		intActionBar();
	}

	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

		mEvernotePreference = new Preference(this);
		mEvernotePreference.setOnPreferenceClickListener(this);
		mEvernotePreference.setKey(EVERNOTE);
		refreshEvernote();

		root.addPreference(mEvernotePreference);
		return root;
	}

	void intActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action_bar_bg));
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(R.string.action_share_account);
			actionBar.setIcon(R.drawable.chh_icon);
		}
	}

	@Override
	public boolean onPreferenceClick(Preference pre) {
		if (pre.getKey().equals(EVERNOTE)) {
			if (ShareToEvernote.getInstance(this).isLoggedIn()) {
				ShareToEvernote.getInstance(this).logOut();
				refreshEvernote();
			} else {
				ShareToEvernote.getInstance(this).authenticate();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case EvernoteSession.REQUEST_CODE_OAUTH:
				if (resultCode == Activity.RESULT_OK) {
					if (mEvernotePreference != null) {
						refreshEvernote();
					}
				}
				break;
		}
	}

	private void refreshEvernote() {
		if (mEvernotePreference != null) {
			String evernoteTitle = getString(R.string.login);
			if (ShareToEvernote.getInstance(this).isLoggedIn()) {
				evernoteTitle = getString(R.string.logout);
			}
			evernoteTitle += (" " + getString(R.string.evernote));
			mEvernotePreference.setTitle(evernoteTitle);
		}
	}

}
