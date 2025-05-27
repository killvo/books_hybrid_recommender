package org.chumak.recommender.database.domain.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chumak.recommender.database.common.model.BaseEntity;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Table(name = "users")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;
}