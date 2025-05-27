package org.chumak.recommender.database.domain.work;

import lombok.Data;
import org.chumak.recommender.database.domain.author.Author;
import org.chumak.recommender.database.domain.review.Review;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "works")
@Entity
@Data
public class Work {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    private String description;

    private String subjects;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    @ManyToMany
    @JoinTable(
            name = "work_authors",
            joinColumns = @JoinColumn(name = "work_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "author_id", nullable = false)
    )
    private List<Author> authors;

    @OneToMany(mappedBy = "work")
    private List<Review> reviews;
}
