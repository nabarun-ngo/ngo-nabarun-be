package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Page<T> {
	@JsonProperty("pageIndex")
	private Integer pageIndex;
	
	@JsonProperty("pageSize")
	private Integer pageSize;
	 
	@JsonProperty("totalSize")
	private long totalSize;
	
	@JsonProperty("currentSize")
	private long currentSize;
	
	@JsonProperty("totalPages")
	private int totalPages;
	
	@JsonProperty("content")
	private List<T> content;
	
	@JsonProperty("nextPageIndex")
	private int nextPageIndex;
	
	@JsonProperty("prevPageIndex")
	private int prevPageIndex;
	
	public Page(Integer pageIndex,Integer pageSize,long totalSize,List<T> content){
		this.pageIndex=pageIndex == null ? 0 : pageIndex;
		this.pageSize= pageSize;
		this.currentSize=content == null ? 0 : content.size();
		this.content=content;
		this.totalSize=totalSize;
		this.nextPageIndex= pageIndex == null || pageIndex == 0 ? 0 : pageIndex+1;
		this.prevPageIndex= pageIndex == null || pageIndex == 0 ? 0 : pageIndex-1;
		this.totalPages= pageSize == null || pageSize == 0 ? 1 : (totalSize%pageSize == 0 ? (int)(totalSize/pageSize) : (int)(totalSize/pageSize)+1);
	}
	
	
	

}
