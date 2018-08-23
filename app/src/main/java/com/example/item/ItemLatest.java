package com.example.item;

import java.io.Serializable;

public class ItemLatest implements Serializable {
	
	private String id;
	private String CategoryId;
	private String CategoryName;
	private String VideoUrl;
	private String VideoId;
	private String VideoName;
	private String Duration;
	private String Description;
	private String ImageUrl;
	private String VideoType;
	private String VideoImgBig;
	private String VideoRate;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getCategoryId() {
		return CategoryId;
	}

	public void setCategoryId(String categoryid) {
		this.CategoryId = categoryid;
	}
	
	public String getCategoryName() {
		return CategoryName;
	}

	public void setCategoryName(String categoryname) {
		this.CategoryName = categoryname;
	}
	
	public String getVideoUrl() {
		return VideoUrl;
	}

	public void setVideoUrl(String videourl) {
		this.VideoUrl = videourl;
	}
	
	
	public String getVideoId() {
		return VideoId;
	}

	public void setVideoId(String videoid) {
		this.VideoId = videoid;
	}
	
	public String getVideoName() {
		return VideoName;
	}

	public void setVideoName(String videoname) {
		this.VideoName = videoname;
	}
	
	public String getDuration() {
		return Duration;
	}

	public void setDuration(String duration) {
		this.Duration = duration;
	}
	
	public String getDescription() {
		return Description;
	}

	public void setDescription(String desc) {
		this.Description = desc;
	}
	
	public String getImageUrl() {
		return ImageUrl;
	}

	public void setImageUrl(String imageurl) {
		this.ImageUrl = imageurl;
	}
	
	
	public String getVideoType() {
		return VideoType;
	}

	public void setVideoType(String videotype) {
		this.VideoType = videotype;
	}

	public String getVideoImgBig() {
		return VideoImgBig;
	}

	public void setVideoImgBig(String VideoImgBig) {
		this.VideoImgBig = VideoImgBig;
	}

	public String getVideoRate() {
		return VideoRate;
	}

	public void setVideoRate(String VideoRate) {
		this.VideoRate = VideoRate;
	}

}
