package ngo.nabarun.app.infra.service;

import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public interface ISequenceInfraService {
	int getLastSequence(String seqName); 
	Date getLastResetDate(String seqName); 
	int incrementSequence(String seqName); 
	int decrementSequence(String seqName); 
	int resetSequence(String seqName); 

}
