package ngo.nabarun.app.ext.service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.RazorPayBankList;

public interface IMiscExtService {
	
	RazorPayBankList getRazorPayBanks(int offset,int limit,String state,String city,String bankcode) throws ThirdPartyException;

}
