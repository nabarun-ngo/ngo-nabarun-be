package ngo.nabarun.app.infra.service;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.infra.dto.TicketDTO;

@Service
public interface ITicketInfraService {
	TicketDTO getTicketInfoByToken(String token);
	TicketDTO createTicket(TicketDTO ticket);
	//TicketDTO deleteTicket(String id);
	TicketDTO updateTicket(String id,TicketDTO updatedTicket);

}
