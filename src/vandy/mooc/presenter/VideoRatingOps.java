package vandy.mooc.presenter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import vandy.mooc.view.VideoRatingActivity;
import vandy.mooc.view.ui.VideoAdapter;
import vandy.mooc.common.ConfigurableOps;
import vandy.mooc.common.ContextView;
import vandy.mooc.common.GenericAsyncTask;
import vandy.mooc.common.GenericAsyncTaskOps;
import vandy.mooc.model.mediator.VideoDataMediator;
import vandy.mooc.model.services.DownloadVideoService;
import vandy.mooc.provider.ContentProviderHelper;
import vandy.mooc.provider.VideoContract;
import vandy.mooc.provider.VideoMeta;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * This class implements all the acronym-related operations defined in
 * the AcronymOps interface.
 */
public class VideoRatingOps
       implements ConfigurableOps<VideoRatingOps.View>,
                  GenericAsyncTaskOps<Long, Void, VideoMeta> {
	/**
     * Debugging tag used by the Android logger.
     */
    private final String TAG = getClass().getSimpleName();
   
    /**
     * Key used when passing id data in an intent
     */
    
    public static final String VIDEO_ID_KEY = "video_id_key";
    
    
    /**
     * Key used when passing title data in an intent
     */
    
    public static final String VIDEO_TITLE_KEY = "video_title_key";
    
    /**
     * Used to enable garbage collection.
     */
    private WeakReference<VideoRatingOps.View> mVideoView;

    /*
     * Helper object used to access the ContentProvider
     */
    
    private ContentProviderHelper mCPH;
    
    /**
     * List of results to display (if any).
     */
    private VideoMeta mResult;
    
    /**
     * id of the video to be displayed
     */
    
    private long mId;
    
    private String mTitle;
    
    private boolean mData;

    public interface View extends ContextView {
        /**
         * Finishes the Activity the VideoOps is
         * associated with.
         */
        void finish();

        /**
         * Sets text as a test
         */
        void setText(String text);
        
        void displayThumb(VideoMeta meta);
        void play(VideoMeta meta);
        void setStars(Float rating);
        void setButtons(boolean haveData);
    }
    



    /**
     * 
     */
    private GenericAsyncTask<Long, Void, VideoMeta, VideoRatingOps> mAsyncTask;

    private GenericAsyncTask<Integer, Void, String, AsyncUpdater> mAsyncTaskRating;
    
    
    /**
     * Default constructor that's needed by the GenericActivity
     * framework.
     */
    public VideoRatingOps() {
    }


    /**
     * Display results if any (due to runtime configuration change).
     */
    public void updateDisplay() {
    	Log.i(TAG, "updating display");
    	Log.i(TAG, "mTitle: " + mId);
    	View view = mVideoView.get();
		if(view != null){
			view.setText(mTitle);
        	view.displayThumb(mResult);
		}
        	setButtons();
        	updateStars(mResult);
    }

 
    public void updateRating(int rating){
    	int stars = rating;
		if(stars > 5){
			stars = 5;
		} else if(stars < 1){
			stars = 1;
		}
        mAsyncTaskRating = new GenericAsyncTask<>(new AsyncUpdater());
        mAsyncTaskRating.execute(stars);
    }
    
    private void setButtons(){
    	View view = mVideoView.get();
		if(view != null){
			view.setButtons(mData);
		}
    }
    
    
    private void updateStars(VideoMeta meta){
  // 	VideoMeta meta = mCPH.get(mId);
    	View view = mVideoView.get();
		if(view != null && meta != null){
			view.setStars((float)meta.getRating());
		}
    }

    public void checkAndUpdateVideo(){
        mAsyncTask = new GenericAsyncTask<>(this);
        mAsyncTask.execute(mId);
    }

	@Override
	public VideoMeta doInBackground(Long... id) {
		if (id.length > 0){
			Log.i(TAG, "calling the CP with id " + id[0]);
			VideoMeta meta = mCPH.get(id[0]); 
			if (meta != null){
				Log.i(TAG, "metadata with file: " + meta.getLocalPath());
				File file = new File(meta.getLocalPath());
				Log.i(TAG, "file exists? " + file.exists());
				mData = file.exists();
			} else {
				mData = false;
			}
	    	return meta;
		} else {
			mData = false;
			return null;
		}
	}
	
	   /**
  * Display the results in the UI Thread.
  */
	@Override
	public void onPostExecute(VideoMeta result) {
 	// TODO display the video and all that.
		Log.i(TAG, "displaying " + result);
		mResult = result;
		updateDisplay();
	//	mVideoView.get().displayThumb(result);
		
	}
	
	private class AsyncUpdater implements GenericAsyncTaskOps<Integer, Void, String>{

		@Override
		public String doInBackground(Integer... rating) {
			String status = null;
			VideoDataMediator mediator = new VideoDataMediator();
			View view = mVideoView.get();
			if(view != null){
				status = mediator.addRating(view.getActivityContext(), mId, rating[0]);
				Log.i(TAG, "attempted to update ratings: " +status);
			}
			
			mResult = mCPH.get(mId);
			// TODO This is a bit hacky.  I just want to update the ContentProvider, without
			// blocking, but I have to return *something*.
			return status;
		}

		@Override
		public void onPostExecute(String result) {
			// TODO Auto-generated method stub
			updateStars(mResult);
		}
		
	}




	public void setIdAndTitle(long id, String title){
		mId = id;
		mTitle = title;
		checkAndUpdateVideo();
		updateDisplay();
	}
	
	


	public void playVideo(){
		if (mResult != null){
			View view = mVideoView.get(); 
			if(view != null){
				view.play(mResult);
			} 
		}
	}
	
	public void downloadVideo(){
		View view = mVideoView.get();
		if(view != null){
			Log.i(TAG, "starting DownloadVideoService");
			view.getApplicationContext().startService( DownloadVideoService.makeIntent(view.getActivityContext(), mId));
		}		
	}
	

    /**
     *
     * @param view     The currently active VideoRatingOps.View.
     * @param firstTimeIn  Set to "true" if this is the first time the
     *                     Ops class is initialized, else set to
     *                     "false" if called after a runtime
     *                     configuration change.
     */
 
    @Override
    public void onConfiguration(VideoRatingOps.View view,
                                boolean firstTimeIn) {      
        final String time =
                firstTimeIn 
                ? "first time" 
                : "second+ time";
            
            Log.d(TAG,
                  "onConfiguration() called the "
                  + time
                  + " with view = "
                  + view);

            // (Re)set the mVideoView WeakReference.
            mVideoView =
                new WeakReference<>(view);
    	
        if (firstTimeIn) {
            // Create a new helper to access the content provider
            mCPH = new ContentProviderHelper(view.getActivityContext());
        } 
        
        
        // TODO Have this only update if there's something to be shown.
      
        updateDisplay();
    }
    



}
