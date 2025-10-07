
package ngo.nabarun.domain.member;

import java.time.Instant;


public class Member {
    private final String id;
    private final String name;
    private final String email;
    private final Instant joinedAt;

    public Member(String id, String name, String email, Instant joinedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.joinedAt = joinedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Instant getJoinedAt() { return joinedAt; }

    
}
