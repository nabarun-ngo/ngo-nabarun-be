package ngo.nabarun.infra.adapter.ext.firebase;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.port.RoleMetadataPort;

@Service
public class FirebaseRCAdapter implements RoleMetadataPort {

	public FirebaseRCAdapter() {

	}

	@Override
	public List<Role> getAllRoles() {
		// TODO Auto-generated method stub
		return List.of(new Role(null, "MEMBER", "Member", "Member", null),
				new Role(null, "CASHIER", "Cashier", "CASHIER", null),
				new Role(null, "ASST_CASHIER", "Assistant Cashier", "CASHIER", null),
				new Role(null, "PRESIDENT", "President", "PRESIDENT", null),
				new Role(null, "TREASURER", "Treasurer", "TREASURER", null));
	}

	@Override
	public List<Role> getDefaultRoles() {
		// TODO Auto-generated method stub
		// Role.builder().roleCode("MEMBER").roleDisplayName("Member").roleName("MEMBER").build()
		return List.of(new Role(null, "MEMBER", "Member", "Member", null));
	}

	@Override
	public Role getRoleByCode(String code) {
		// TODO Auto-generated method stub
		return getAllRoles().stream().filter(d -> d.roleCode().equalsIgnoreCase(code)).findFirst().get();
	}

}
