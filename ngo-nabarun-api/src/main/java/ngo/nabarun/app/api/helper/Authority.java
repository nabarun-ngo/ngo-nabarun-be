package ngo.nabarun.app.api.helper;

public class Authority {
	public static final String READ_USERS="hasAuthority('SCOPE_read:users')";
	public static final String READ_USER = "hasAuthority('SCOPE_read:user')";
	public static final String UPDATE_USER = "hasAuthority('SCOPE_update:user')";
	
	public static final String READ_WORK = "hasAuthority('SCOPE_read:work')";
	public static final String UPDATE_WORK = "hasAuthority('SCOPE_update:work')";
	public static final String READ_REQUEST = "hasAuthority('SCOPE_read:request')";
	public static final String CREATE_REQUEST = "hasAuthority('SCOPE_create:request')";
	public static final String UPDATE_REQUEST = "hasAuthority('SCOPE_update:request')";
	
	public static final String READ_NOTICES = "hasAuthority('SCOPE_read:notices')";
	public static final String READ_NOTICE = "hasAuthority('SCOPE_read:notice')";
	public static final String CREATE_NOTICE = "hasAuthority('SCOPE_create:notice')";
	public static final String UPDATE_NOTICE = "hasAuthority('SCOPE_update:notice')";
	public static final String DELETE_NOTICE = "hasAuthority('SCOPE_delete:notice')";
	
	public static final String READ_DONATIONS = "hasAuthority('SCOPE_read:donations')";
	public static final String READ_USER_DONATIONS = "hasAuthority('SCOPE_read:user_donations')";
	public static final String UPDATE_DONATION = "hasAuthority('SCOPE_update:donation')";
	public static final String CREATE_DONATION = "hasAuthority('SCOPE_create:donation')";
	
	public static final String READ_ACCOUNTS = "hasAuthority('SCOPE_read:accounts')";
	public static final String CREATE_ACCOUNT = "hasAuthority('SCOPE_create:account')";
	public static final String UPDATE_ACCOUNT = "hasAuthority('SCOPE_update:account')";
	public static final String READ_TRANSACTIONS = "hasAuthority('SCOPE_read:transactions')";
}
