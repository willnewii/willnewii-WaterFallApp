package com.willnewii.waterfallapp;

public class SimpleObject {

	private String filePath = "";
	
	private String content = "";
	
	public SimpleObject(String _filePath , String _content) {
		this.filePath = _filePath ;
		this.content = _content ;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
}
