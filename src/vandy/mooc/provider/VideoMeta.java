package vandy.mooc.provider;

import vandy.mooc.model.mediator.webdata.Video;

public class VideoMeta {

	private long id;
	private String title;
	private String dataUrl;
	private String contentType;
	private long duration;
	private String localPath;
	private double rating;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public VideoMeta(long id, String title, String dataUrl, String contentType, long duration, String localPath, double rating) {
		super();
		this.id = id;
		this.title = title;
		this.dataUrl = dataUrl;
		this.contentType = contentType;
		this.duration = duration;
		this.localPath = localPath;
		this.rating = rating;
	}

	
	public VideoMeta(Video video, String localPath, double rating){
		// TODO Fix this when you change the server, since you don't need the rating any more
		this(video.getId(), video.getTitle(), video.getDataUrl(), video.getContentType(), video.getDuration(), localPath, rating);
		
	}



	
}
