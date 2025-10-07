package ngo.nabarun.domain.user.model;

import java.util.UUID;

import lombok.Getter;

@Getter
public class Role {
	
	private UUID id;
	private String name;
	private String code;
	private String group;



}
