package ngo.nabarun.domain.user.port;

import java.util.List;
import ngo.nabarun.domain.user.model.Role;

public interface RoleMetadataPort {

    /**
     * Returns all roles available in the system with metadata.
     */
    List<Role> getAllRoles();

    /**
     * Returns roles marked as default (e.g., MEMBER, BASIC_USER, etc.)
     */
    List<Role> getDefaultRoles();

    /**
     * Fetch a single role by code (used when validating or resolving role metadata)
     */
    Role getRoleByCode(String code);
}
