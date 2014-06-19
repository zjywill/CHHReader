package com.comic.chhreader.evernoteshare;

import android.R;
import android.content.Context;

import com.comic.chhreader.Loge;
import com.comic.chhreader.utils.CHHNetUtils;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.thrift.transport.TTransportException;

public class ShareToEvernote {
	private static final String CONSUMER_KEY = "zjywill";
	private static final String CONSUMER_SECRET = "6ced9b74e128bcfb";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.PRODUCTION;
	private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;

	public static ShareToEvernote sShareToEvernote = null;

	public static EvernoteSession sEvernoteSession = null;
	public static Context sContext = null;

	synchronized public static ShareToEvernote getInstance(Context context) {
		if (sShareToEvernote == null) {
			sShareToEvernote = new ShareToEvernote(context);
		}
		return sShareToEvernote;
	}

	private ShareToEvernote(Context context) {
		sContext = context;
		genEvernoteSession(context);
	}

	private void genEvernoteSession(Context context) {
		sEvernoteSession = EvernoteSession.getInstance(context, CONSUMER_KEY, CONSUMER_SECRET,
				EVERNOTE_SERVICE, SUPPORT_APP_LINKED_NOTEBOOKS);
	}

	public EvernoteSession getEvernoteSession() {
		return sEvernoteSession;
	}

	public boolean isAppLinkedNotebook() {
		if (sEvernoteSession != null) {
			return sEvernoteSession.getAuthenticationResult().isAppLinkedNotebook();
		}
		Loge.d("isAppLinkedNotebook sEvernoteSession is null");
		return false;
	}

	public boolean isLoggedIn() {
		if (sEvernoteSession != null) {
			return sEvernoteSession.isLoggedIn();
		}
		Loge.d("isLoggedIn sEvernoteSession is null");
		return false;
	}

	public void authenticate() {
		if (sEvernoteSession != null && sContext != null) {
			sEvernoteSession.authenticate(sContext);
		}
	}

	public void shareNote(Context context, String title, String url, String content,
			OnClientCallback<Note> mNoteCreateCallback) {
		genEvernoteSession(context);

		Note note = new Note();
		note.setTitle(title);

		NoteAttributes attrs = new NoteAttributes();
		attrs.setSourceURL(url);
		note.setAttributes(attrs);

		String originUrl = context.getString(com.comic.chhreader.R.string.origin_url) + "	";
		originUrl = originUrl + "<a href=\"" + url + "\"" + ">" + url + "</a>";
		String fomateBegin = "<div>" + originUrl + "</div>" + "<div><br /></div> 	";
		note.setContent(EvernoteUtil.NOTE_PREFIX + fomateBegin + content + EvernoteUtil.NOTE_SUFFIX);

		if (sEvernoteSession == null) {
			return;
		}
		try {
			sEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note, mNoteCreateCallback);
		} catch (TTransportException exception) {
			Loge.e("Error creating notestore", exception);
		}
	}
}
