package ngo.nabarun.domain.user.port;

import java.util.List;
import java.util.Optional;

import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.model.User;

public interface UserRepositoryPort {
    User createUser(User user);
    User updateUser(User user);
    boolean isUserExists(String email);

    Optional<User> findById(String id);
    List<User> findPage(String emailLike, UserStatus status, String roleCode, int page, int size, String sort);
    long count(String emailLike, UserStatus status, String roleCode);

    User assignRolesToUser(User user, List<Role> rolesToAdd);
    User deleteRolesFromUser(User user, List<Role> rolesToDelete);
}
