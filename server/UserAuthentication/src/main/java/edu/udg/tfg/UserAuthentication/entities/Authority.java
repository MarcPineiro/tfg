package edu.udg.tfg.UserAuthentication.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
//import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Setter
@Entity
public class Authority {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    @Getter
    UUID id;

    @Enumerated( EnumType.STRING)
    @Column(nullable = false, unique = true)
    RoleName name;

    public String getAuthority() {
        return name.name();
    }

    @JsonIgnore
    public RoleName getName() {
        return name;
    }

}