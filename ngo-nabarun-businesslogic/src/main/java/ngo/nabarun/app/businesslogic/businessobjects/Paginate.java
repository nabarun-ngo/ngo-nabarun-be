package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Paginate<T> {
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
	
	public Paginate(Integer pageIndex,Integer pageSize,long totalSize,List<T> content){
		this.pageIndex=pageIndex == null ? 0 : pageIndex;
		this.pageSize= pageSize;
		this.currentSize=content == null ? 0 : content.size();
		this.content=content;
		this.totalSize=totalSize;
		this.nextPageIndex= pageIndex == null || pageIndex == 0 ? 0 : pageIndex+1;
		this.prevPageIndex= pageIndex == null || pageIndex == 0 ? 0 : pageIndex-1;
		this.totalPages= pageSize == null || pageSize == 0 ? 1 : (totalSize%pageSize == 0 ? (int)(totalSize/pageSize) : (int)(totalSize/pageSize)+1);
	}
	
	public Paginate(Page<T> page){
		this.pageIndex=page.getNumber();
		this.pageSize= page.getSize();
		this.currentSize=page.getNumberOfElements();
		this.content=page.getContent();
		this.totalSize=page.getTotalElements();
		this.nextPageIndex= page.getNumber()+1;
		this.prevPageIndex= page.getNumber() == 0 ? 0 : page.getNumber()-1;
		this.totalPages= page.getTotalPages();
	}
	
	
	public <U> Paginate<U> map(Function<? super T, ? extends U> converter) {
		List<U> newList=content.stream().map(converter::apply).collect(Collectors.toList());
		return new Paginate<>(pageIndex,pageSize,totalSize,newList);
	}


}
