package ngo.nabarun.app.businesslogic.businessobjects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.ExpenseRefType;
import ngo.nabarun.app.common.enums.ExpenseStatus;

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
	
	@JsonProperty("expenseDate")
	private Date expenseDate;
	
	@JsonProperty("createdOn")
	private Date createdOn;
	
	@JsonProperty("finalizedBy")
	private UserDetail finalizedBy;
	
	@JsonProperty("finalized")
	private boolean finalized;
	
	@JsonProperty("expenseItems")
	private List<ExpenseItemDetail> expenseItems;
	
	@JsonProperty("finalAmount")
	private Double finalAmount;
	
	@JsonProperty("expenseRefType")
	private ExpenseRefType expenseRefType;
	
	@JsonProperty("expenseRefId")
	private String expenseRefId;
	
	@JsonProperty("status")
	private ExpenseStatus status;
	
	@JsonProperty("txnNumber")
	private String txnNumber;
	
	@JsonProperty("account")
	private AccountDetail account;
	
//	@JsonProperty("expenseItem")
//	private ExpenseItemDetail expenseItem;
	
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
		
		@JsonProperty("expenseDate")
		private Date expenseDate;
		
		@JsonProperty("amount")
		private Double amount;
		
		@JsonProperty("expenseAccount")
		private AccountDetail expenseAccount;
		
		@JsonProperty("remove")
		private boolean remove;
		
		@JsonProperty("status")
		private ExpenseStatus status;

	}


	
}
