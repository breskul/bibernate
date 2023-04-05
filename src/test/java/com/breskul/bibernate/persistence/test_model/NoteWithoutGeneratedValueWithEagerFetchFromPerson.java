package com.breskul.bibernate.persistence.test_model;

import com.breskul.bibernate.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notes")
@EqualsAndHashCode(exclude = "person")
public class NoteWithoutGeneratedValueWithEagerFetchFromPerson {

    @Id
    private Long id;
    private String body;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "person_id")
    private PersonWithoutGeneratedValueWithEagerFetch person;
}
