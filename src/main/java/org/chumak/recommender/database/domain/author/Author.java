package org.chumak.recommender.database.domain.author;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "authors")
@Entity
@Data
public class Author {
    @Id
    private String id;

    @Column(nullable = false)
    private String fullName;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Date updatedAt;
}
