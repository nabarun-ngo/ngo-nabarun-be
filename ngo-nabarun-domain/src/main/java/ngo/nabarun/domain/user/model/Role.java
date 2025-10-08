package ngo.nabarun.domain.user.model;

//@Value
//@Builder(toBuilder = true)
public record Role (
	String id,
	String roleName,
	String roleDisplayName,
	String roleCode,
	String roleId) {

}
