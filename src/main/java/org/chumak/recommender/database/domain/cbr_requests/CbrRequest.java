package org.chumak.recommender.database.domain.cbr_requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chumak.recommender.database.common.model.BaseEntity;
import org.chumak.recommender.database.domain.user.User;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Table(name = "cbr_requests")
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class CbrRequest extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "completed_at")
    private Date completedAt;
}
