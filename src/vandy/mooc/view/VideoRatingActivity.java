package vandy.mooc.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;
import vandy.mooc.R;
import vandy.mooc.common.GenericActivity;
import vandy.mooc.model.services.DownloadVideoService;
import vandy.mooc.presenter.VideoRatingOps;
import vandy.mooc.provider.VideoMeta;
import vandy.mooc.view.ui.UploadVideoDialogFragment;
import vandy.mooc.view.ui.UploadVideoDialogFragment.OperationType;

/**
 * This Activity can be used upload a selected video to a Video Service and also
 * displays a list of videos available at the Video Service. The user can record
 * a video or get a video from gallery and upload it. It implements
 * OnVideoSelectedListener that will handle callbacks from the UploadVideoDialog
 * Fragment. It extends GenericActivity that provides a framework for
 * automatically handling runtime configuration changes of an VideoOps object,
 * which plays the role of the "Presenter" in the MVP pattern. The VideoOps.View
 * interface is used to minimize dependencies between the View and Presenter
 * layers.
 */

// TODO It doesn't need to implemetn Uploadblahblahblah, so remove it.
public class VideoRatingActivity extends GenericActivity<VideoRatingOps.View, VideoRatingOps>
		implements UploadVideoDialogFragment.OnVideoSelectedListener, VideoRatingOps.View {

	/**
	 * The Broadcast Receiver that registers itself to receive the result from
	 * DownloadVideoService when a video download completes.
	 */
	private DownloadResultReceiver mDownloadResultReceiver;

	private class DownloadResultReceiver extends BroadcastReceiver {
		/**
		 * Hook method that's dispatched when the DownloadService has downloaded
		 * the Video.
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			// Triggers an update to the display
			// TODO Deal with the case where the download fails
			getOps().checkAndUpdateVideo();
			getOps().updateDisplay();
		}
	}

	/**
	 * The button the asks the server for a video.
	 */
	private ImageButton mDownloadVideo;

	/**
	 * Hook method called when a new instance of Activity is created. One time
	 * initialization code goes here, e.g., storing Views.
	 * 
	 * @param Bundle
	 *            object that contains saved state information.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Initialize the default layout.
		setContentView(R.layout.video_rating_activity);

		// Receiver for the notification.
		mDownloadResultReceiver = new DownloadResultReceiver();

		mDownloadVideo = (ImageButton) findViewById(R.id.download_button);

		RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		
    	ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
	
			        public void onRatingChanged(RatingBar ratingBar, float rating,  boolean fromUser) {
			        	if(fromUser){
			        		getOps().updateRating(Math.round(rating));
			        	}
	
			        }
			    }); 

		// Invoke the special onCreate() method in GenericActivity,
		// passing in the VideoOps class to instantiate/manage and
		// "this" to provide VideoOps with the VideoOps.View instance.
		super.onCreate(savedInstanceState, VideoRatingOps.class, this);

		setIdAndTitle(getIntent());
	}

	/**
	 * Set the title.
	 */
	private void setIdAndTitle(Intent intent) {

		final long id = intent.getLongExtra(VideoRatingOps.VIDEO_ID_KEY, -1);
		final String title = intent.getStringExtra(VideoRatingOps.VIDEO_TITLE_KEY);
		getOps().setIdAndTitle(id, title);

	}

	/**
	 * Register a BroadcastReceiver that receives a result from the
	 * DownloadVideoService when a video upload completes.
	 */
	private void registerReceiver() {

		// Create an Intent filter that handles Intents from the
		// UploadVideoService.
		IntentFilter intentFilter = new IntentFilter(DownloadVideoService.ACTION_DOWNLOAD_SERVICE_RESPONSE);
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

		// Register the BroadcastReceiver.
		LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadResultReceiver, intentFilter);
	}

	/**
	 * Hook method that is called when user resumes activity from paused state,
	 * onPause().
	 */
	@Override
	protected void onResume() {
		// Call up to the superclass.
		super.onResume();

		// Register BroadcastReceiver that receives result from
		// UploadVideoService when a video upload completes.
		registerReceiver();
	}

	/**
	 * Hook method that gives a final chance to release resources and stop
	 * spawned threads. onDestroy() may not always be called-when system kills
	 * hosting process.
	 */
	@Override
	protected void onPause() {
		// Call onPause() in superclass.
		super.onPause();

		// Unregister BroadcastReceiver.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadResultReceiver);
	}

	public void playVideo(View v) {
		getOps().playVideo();
	}

	public void downloadVideo(View v) {
		Log.i(TAG, "delegating downloadVideo to Ops");
		getOps().downloadVideo();
	}

	@Override
	public void play(VideoMeta meta) {
		// File file = VideoStorageUtils.getVideoStorageDir(meta.getTitle());

		Log.i(TAG, "Stored filename is" + meta.getLocalPath());
		// Log.i(TAG, "Attempting to play " + file.getPath());
		final VideoView videoView = (VideoView) findViewById(R.id.video_view);
		MediaController mediaController = new MediaController(this);

		try {
			videoView.setMediaController(mediaController);
			videoView.setVideoPath(meta.getLocalPath());
			// videoView.setVideoPath(file.getAbsolutePath());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

		videoView.requestFocus();

		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mediaPlayer) {
				videoView.start();
			}
		});

	}

	/**
	 * Finishes this Activity.
	 */
	@Override
	public void finish() {
		super.finish();
	}

	public void displayTitle(String title, String errorMessage) {
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setText(title);
	}

	public void displayVideo(VideoMeta meta, String errorMessage) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void onVideoSelected(OperationType which) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setText(String text) {
		Log.i(TAG, "Setting title: " + text);
		TextView tv = (TextView) findViewById(R.id.title);
		tv.setText(text);
	}

	@Override
	public void displayThumb(VideoMeta meta) {
		// ImageButton videoButton = (ImageButton)
		// findViewById(R.id.video_button);
		if (meta != null) {
			Log.w(TAG, "Displaying video " + meta);
			// videoButton.setText(meta.getLocalPath());
			Bitmap bMap = ThumbnailUtils.createVideoThumbnail(meta.getLocalPath(),
					MediaStore.Video.Thumbnails.MINI_KIND);

			// videoButton.setImageBitmap(bMap);
			// videoButton.setVisibility(View.VISIBLE);
		} else {
			Log.w(TAG, "Attempted to display nonexistant video");
			// videoButton.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void setStars(Float rating) {
		Log.i(TAG, "Setting stars with rating " + rating);
		RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		ratingBar.setRating(rating);
	/*	DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		float pixelWidth = displayMetrics.widthPixels;
		ImageView backView = (ImageView) findViewById(R.id.background_view);
		backView.getLayoutParams().width = (int) (pixelWidth * rating / 5.0);
		backView.requestLayout(); */
	}

	// TODO There has to be a better solution than this!
	/*
	public void starOne(View v) {
		getOps().updateRating(1);
	}

	public void starTwo(View v) {
		getOps().updateRating(2);
	}

	public void starThree(View v) {
		getOps().updateRating(3);
	}

	public void starFour(View v) {
		getOps().updateRating(4);
	}

	public void starFive(View v) {
		getOps().updateRating(5);
	} */

	@Override
	public void setButtons(boolean haveData) {
		Log.i(TAG, "setting buttons with data: " + haveData);
		ImageButton downLoad = (ImageButton) findViewById(R.id.download_button);
		ImageButton play = (ImageButton) findViewById(R.id.video_button);
		if (haveData) {
			downLoad.setVisibility(View.INVISIBLE);
			play.setVisibility(View.VISIBLE);
		} else {
			downLoad.setVisibility(View.VISIBLE);
			play.setVisibility(View.INVISIBLE);
		}
	}

}
