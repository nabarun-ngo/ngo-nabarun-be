package ngo.nabarun.app.businesslogic.businessobjects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.ExpenseRefType;

@Data
public class ExpenseDetail implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("name")
	private String name;
		
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("createdBy")
	private UserDetail createdBy;
	
	@JsonProperty("createdOn")
	private Date createdOn;
	
	@JsonProperty("approvedBy")
	private UserDetail approvedBy;
	
	@JsonProperty("approved")
	private boolean approved;
	
	@JsonProperty("expenseAccount")
	private AccountDetail expenseAccount;
	
	@JsonProperty("expenseItems")
	private List<ExpenseItemDetail> expenseItems;
	
	@JsonProperty("finalAmount")
	private Double finalAmount;
	
	@JsonProperty("expenseRefType")
	private ExpenseRefType expenseRefType;
	
	@JsonProperty("expenseRefId")
	private String expenseRefId;
	
	@Data
	public static class ExpenseDetailFilter {
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		@JsonProperty("startDate")
		private Date startDate;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		@JsonProperty("endDate")
		private Date endDate;
	}
	@Data
	public static class ExpenseItemDetail {
		@JsonProperty("id")
		private String id;
		
		@JsonProperty("itemName")
		private String itemName;
			
		@JsonProperty("description")
		private String description;
		
		@JsonProperty("amount")
		private Double amount;
	}


	
}
