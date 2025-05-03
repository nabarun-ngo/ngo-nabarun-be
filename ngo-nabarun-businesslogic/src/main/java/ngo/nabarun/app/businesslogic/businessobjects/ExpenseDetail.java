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
	
	@JsonProperty("expenseDate")
	private Date expenseDate;
	
	@JsonProperty("createdBy")
	private UserDetail createdBy;

	@JsonProperty("createdOn")
	private Date createdOn;
	
	@JsonProperty("isAdmin")
	private boolean admin;
	
	@JsonProperty("isDeligated")
	private boolean deligated;
	
	@JsonProperty("paidBy")
	private UserDetail paidBy;
	
	//Finalize
	@JsonProperty("finalizedBy")
	private UserDetail finalizedBy;
	
	@JsonProperty("status")
	private ExpenseStatus status;
	
	@JsonProperty("finalizedOn")
	private Date finalizedOn;
	
	//Settlement
	@JsonProperty("settledBy")
	private UserDetail settledBy;
	
	@JsonProperty("settledOn")
	private Date settledOn;

	@JsonProperty("expenseItems")
	private List<ExpenseItemDetail> expenseItems;
	
	@JsonProperty("finalAmount")
	private Double finalAmount;
	
	@JsonProperty("expenseRefType")
	private ExpenseRefType expenseRefType;
	
	@JsonProperty("expenseRefId")
	private String expenseRefId;
	
	@JsonProperty("txnNumber")
	private String txnNumber;
	
	@JsonProperty("settlementAccount")
	private AccountDetail settlementAccount;
	
	@JsonProperty("rejectedBy")
	private UserDetail rejectedBy;
	
	@JsonProperty("rejectedOn")
	private Date rejectedOn;
	
	@JsonProperty("remarks")
	private String remarks;
	
	@Data
	public static class ExpenseDetailFilter {
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		@JsonProperty("startDate")
		private Date startDate;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		@JsonProperty("endDate")
		private Date endDate;
		
		@JsonProperty("expenseRefId")
		private String expenseRefId;
		
		@JsonProperty("expenseId")
		private String expenseId;
		
		@JsonProperty("expenseStatus")
		private List<ExpenseStatus> expenseStatus;
		
		@JsonProperty("payerId")
		private String payerId;
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
