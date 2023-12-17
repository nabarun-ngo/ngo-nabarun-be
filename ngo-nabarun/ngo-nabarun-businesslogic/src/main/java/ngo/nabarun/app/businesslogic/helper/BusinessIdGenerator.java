package ngo.nabarun.app.businesslogic.helper;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.infra.service.ISequenceInfraService;

@Service
public class BusinessIdGenerator {

	@Autowired
	private ISequenceInfraService sequenceInfraService;
	
	public String generateNoticeId() {
		String seqName="NOTICE_SEQUENCE";
		String pattern="NNOT%s%sN%s";
		String year = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yy");
		String month = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "MMM").toUpperCase();
		String ran = CommonUtils.generateRandomNumber(3);

		Date lastResetDate=sequenceInfraService.getLastResetDate(seqName);
		if(lastResetDate == null || CommonUtils.isCurrentMonth(lastResetDate)) {
			int seq=sequenceInfraService.incrementSequence(seqName);
			return String.format(pattern, year+month,ran,seq);
		}else {
			int seq=sequenceInfraService.resetSequence(seqName);
			return String.format(pattern, year+month,ran,seq);
		}
	}
	
	public String generateAccountId() {
		String seqName="ACCOUNT_SEQUENCE";
		String pattern="NACC%s%sA%s";
		String ran = CommonUtils.generateRandomNumber(6);
		String yearmonth = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yyyyMM").toUpperCase();
		int seq=sequenceInfraService.incrementSequence(seqName);
		return String.format(pattern,yearmonth,ran,seq);
	}
	
	public String generateDonationId() {
		String seqName="DONATION_SEQUENCE";
		String pattern="NDON%s%sD%s";
		String ran = CommonUtils.generateRandomNumber(4);
		String yearmonth = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yyyyMMM").toUpperCase();
		int seq=sequenceInfraService.incrementSequence(seqName);
		return String.format(pattern, yearmonth, ran,seq);
	}

	public String generateTransactionId() {
		String seqName="TRANSACTION_SEQUENCE";
		String pattern="NTXN%s%sT%s";
		String date = CommonUtils.getFormattedDate(CommonUtils.getSystemDate(), "yyyyMMddHHmmss");
		String ran = CommonUtils.generateRandomNumber(4);
		int seq=sequenceInfraService.incrementSequence(seqName);
		return String.format(pattern, date,ran,seq);
	}
}
