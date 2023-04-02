package com.breskul.bibernate.persistence.test_model;

import com.breskul.bibernate.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static com.breskul.bibernate.annotation.Strategy.SEQUENCE;

@Entity
@Data
@Table(name = "notes")
@EqualsAndHashCode(exclude = "person")
public class NoteComplex {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;
    private String body;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

}
