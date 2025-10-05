
package ngo.nabarun.infra.mongo.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "members")
public class MemberEntity {
    @Id
    private String id;
    private String name;
    private String email;
    private Instant joinedAt;

    public MemberEntity() {}

    public MemberEntity(String id, String name, String email, Instant joinedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.joinedAt = joinedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}
