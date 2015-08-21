package vandy.mooc.provider;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import vandy.mooc.common.LifecycleLoggingActivity;
import vandy.mooc.model.mediator.webdata.Video;
import vandy.mooc.provider.VideoContract.VideoEntry;

/**
 * Timeout cache that uses Content Providers to cache data and uses AlarmManager
 * and Broadcast Receiver to remove expired cache entries periodically.
 */
public class ContentProviderHelper {
	final static String TAG = ContentProviderHelper.class.getSimpleName();

	/**
	 * Store the context to allow access to application-specific resources and
	 * classes.
	 */
	private Context mContext;

	/**
	 * This constructor sets the default timeout to the designated @a timeout
	 * parameter and initialises local variables.
	 * 
	 * @param context
	 */
	public ContentProviderHelper(Context context) {
		// Store the context.
		mContext = context;
	}

	/**
	 * Gets the @a List of Acronym Expansions from the cache having given @a
	 * acronym. Remove it if expired.
	 * 
	 * @param acronym
	 * @return List of Acronym Data
	 */
	public VideoMeta get(final Long id) {
		if (id < 0){
			return null;
		}
		Log.i(TAG, "Fetching metadata associated with id: " + id);
		// Selection clause to find rows with given title.
		final String selectionTitle = VideoEntry.COLUMN_ID + " = ?";

		// Initializes an array to contain selection arguments.
		String[] selectionArgs = { id.toString() };

		Log.i(TAG, "selectionArgs" + selectionArgs);
	
		// Cursor that is returned as a result of database query which
		// points to one or more rows.
		try (Cursor cursor = mContext.getContentResolver().query(VideoEntry.CONTENT_URI, null, selectionTitle,
				selectionArgs, null)) {
			// If there are not matches in the database return false.
			if (cursor == null || !cursor.moveToFirst()) {
				return null;
			} else {
				return getVideoMeta(cursor);
			}
		}
	}

	/**
	 * Get the acronym expansions data from the database.
	 * 
	 * @param cursor
	 * @return AcronymExpansion
	 */
	private VideoMeta getVideoMeta(Cursor cursor) {
		long id = cursor.getLong(cursor.getColumnIndex(VideoEntry.COLUMN_ID));
		String title = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_TITLE));
		String dataUrl = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_DATA_URL));
		String contentType = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_CONTENT_TYPE));
		long duration = cursor.getLong(cursor.getColumnIndex(VideoEntry.COLUMN_DURATION));
		String localPath = cursor.getString(cursor.getColumnIndex(VideoEntry.COLUMN_LOCAL_PATH));
		double rating = cursor.getDouble(cursor.getColumnIndex(VideoEntry.COLUMN_STAR_RATING));

		return new VideoMeta(id, title, dataUrl, contentType, duration, localPath, rating);
	}

	/**
	 * Put the @a longForms into the cache at the designated @a acronym with the
	 * default timeout.
	 * 
	 * @param title
	 * @param meta
	 */
	public void put(String title, VideoMeta meta) {
		putValues(title, meta);
	}

	
	public int update(String title, VideoMeta meta){
		// Check if the metadata is not null
		if (meta == null)
			return -1;
		
		ContentValues cv = getContentValues(meta, title);
		final String SELECTION_VIDEO = VideoEntry.COLUMN_TITLE + " = ?";

		// Initializes an array to contain selection arguments
		String[] selectionArgs = { title };

		return mContext.getContentResolver().update(VideoEntry.CONTENT_URI, cv, SELECTION_VIDEO, selectionArgs);
	}

	private ContentValues getContentValues(VideoMeta meta, String title){
		ContentValues cv = new ContentValues();

		cv.put(VideoEntry.COLUMN_ID, meta.getId());
		cv.put(VideoEntry.COLUMN_TITLE, title);
		cv.put(VideoEntry.COLUMN_DATA_URL, meta.getDataUrl());
		cv.put(VideoEntry.COLUMN_CONTENT_TYPE, meta.getContentType());
		cv.put(VideoEntry.COLUMN_DURATION, meta.getDuration());
		cv.put(VideoEntry.COLUMN_LOCAL_PATH, meta.getLocalPath());
		cv.put(VideoEntry.COLUMN_STAR_RATING, meta.getRating());
		
		return cv;
	}
	
	/**
	 * Helper method that puts a @a List of Acronym Expansions into the cache at
	 * the designated @a acronym with a certain timeout, after which the cached
	 * data expires.
	 * 
	 * @param title
	 * @param meta
	 * @param timeout
	 * @return number of rows inserted
	 */
	private Uri putValues(String title, VideoMeta meta) {
		// Check if the metadata is not null
		if (meta == null)
			return null;
		
		ContentValues cv = getContentValues(meta, title);
		

		return mContext.getContentResolver().insert(VideoEntry.CONTENT_URI, cv);
	}

	/**
	 * Removes a video
	 * 
	 * @param title
	 */
	public void remove(String title) {
		// Selection clause to find rows with given acronym.
		final String SELECTION_VIDEO = VideoEntry.COLUMN_TITLE + " = ?";

		// Initializes an array to contain selection arguments
		String[] selectionArgs = { title };

		// TODO - delete the row(s) associated with an acronym.
		mContext.getContentResolver().delete(VideoEntry.CONTENT_URI, SELECTION_VIDEO, selectionArgs);
	}

	/**
	 * Return the current number of rows in Database.
	 * 
	 * @return size
	 */
	public int size() {
		// Use ContentResolver to get a Cursor that points to all rows
		// of Acronym table.
		try (Cursor cursor = mContext.getContentResolver().query(VideoEntry.CONTENT_URI, null, null, null, null)) {
			return cursor.getCount();
		}
	}

}
