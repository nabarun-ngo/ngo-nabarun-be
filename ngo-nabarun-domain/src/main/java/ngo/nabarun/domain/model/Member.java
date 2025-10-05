
package ngo.nabarun.domain.model;

import java.time.Instant;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
