package com.breskul.bibernate.persistence.test_model.cascadepersist;

import com.breskul.bibernate.annotation.*;
import com.breskul.bibernate.annotation.enums.Strategy;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
@Table(name = "profiles")
public class PersonProfileCascadePersist {
    @Id
    @GeneratedValue(strategy = Strategy.SEQUENCE)
    private Long id;
    @Column(name = "profile")
    private String profile;

    @OneToOne
    @JoinColumn(name = "person_id")
    private PersonCascadePersist personCascadePersist;

}
