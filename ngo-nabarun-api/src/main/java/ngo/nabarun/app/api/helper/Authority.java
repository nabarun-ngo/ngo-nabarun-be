package ngo.nabarun.app.api.helper;

public class Authority {
	//public static final String ROLE_API_USER="ROLE_API_USER";

	public static final String READ_USERS="hasAnyAuthority('SCOPE_read:users','ROLE_API_USER')";
	public static final String READ_USER = "hasAuthority('SCOPE_read:user')";
	public static final String UPDATE_USER = "hasAuthority('SCOPE_update:user')";
	public static final String UPDATE_USER_ROLE =  "hasAuthority('SCOPE_update:user_role')";

	public static final String READ_WORK = "hasAuthority('SCOPE_read:work')";
	public static final String UPDATE_WORK = "hasAuthority('SCOPE_update:work')";
	
	public static final String READ_REQUEST = "hasAuthority('SCOPE_read:request')";
	public static final String CREATE_REQUEST = "hasAuthority('SCOPE_create:request')";
	public static final String UPDATE_REQUEST = "hasAuthority('SCOPE_update:request')";
	
	public static final String READ_NOTICES = "hasAuthority('SCOPE_read:notices')";
	public static final String READ_NOTICE = "hasAuthority('SCOPE_read:notice')";
	public static final String CREATE_NOTICE = "hasAuthority('SCOPE_create:notice')";
	public static final String UPDATE_NOTICE = "hasAuthority('SCOPE_update:notice')";
    //public static final String DELETE_NOTICE = "hasAuthority('SCOPE_delete:notice')";
	
	public static final String READ_DONATIONS = "hasAuthority('SCOPE_read:donations')";
	public static final String READ_DONATIONS_GUEST= "hasAnyAuthority('SCOPE_read:donation_guest','ROLE_API_USER')";
	public static final String READ_USER_DONATIONS= "hasAuthority('SCOPE_read:user_donations')";
	public static final String READ_DONATION_DOCUMENTS = "hasAuthority('SCOPE_read:donation_documents')";
	public static final String READ_DONATION_HISTORY = "hasAuthority('SCOPE_read:donation_history')";
	public static final String UPDATE_DONATION = "hasAuthority('SCOPE_update:donation')";
	public static final String CREATE_DONATION = "hasAuthority('SCOPE_create:donation')";
	
	public static final String READ_ACCOUNTS = "hasAuthority('SCOPE_read:accounts')";
	public static final String CREATE_ACCOUNT = "hasAuthority('SCOPE_create:account')";
	public static final String UPDATE_ACCOUNT = "hasAuthority('SCOPE_update:account')";
	
	public static final String READ_TRANSACTIONS = "hasAuthority('SCOPE_read:transactions')";
	public static final String CREATE_TRANSACTION = "hasAuthority('SCOPE_create:transaction')";
	
	public static final String READ_EXPENSE = "hasAuthority('SCOPE_read:expenses')";
	public static final String CREATE_EXPENSE = "hasAuthority('SCOPE_create:expense')";
	public static final String UPDATE_EXPENSE = "hasAuthority('SCOPE_update:expense')";
	public static final String CREATE_EXPENSE_FINAL = "hasAuthority('SCOPE_create:expense_final')";

	public static final String READ_APIKEY = "hasAuthority('SCOPE_read:apikey')";
	public static final String CREATE_APIKEY = "hasAuthority('SCOPE_create:apikey')";
	public static final String UPDATE_APIKEY = "hasAuthority('SCOPE_update:apikey')";
	
	public static final String CREATE_SERVICERUN = "hasAuthority('SCOPE_create:servicerun')";
	public static final String READ_ADMIN_SERVICE = "hasAuthority('SCOPE_read:admin_service')";

	public static final String READ_DOCUMENT_LIST = "hasAuthority('SCOPE_read:document_list')";
	public static final String DELETE_DOCUMENT = "hasAuthority('SCOPE_delete:document')";
	
	public static final String READ_JOB = "hasAuthority('SCOPE_read:job')";
	public static final String CREATE_JOB = "hasAuthority('SCOPE_create:job')";


}
