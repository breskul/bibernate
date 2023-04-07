package com.breskul.bibernate.persistence.test_model.cascadenone;

import com.breskul.bibernate.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

import static com.breskul.bibernate.annotation.enums.Strategy.SEQUENCE;

@Entity
@Data
@Table(name = "notes")
public class NoteComplexCascadeNone {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;
    private String body;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "person_id")
    private PersonCascadeNone person;
}
