package com.breskul.bibernate.persistence.test_model;

import com.breskul.bibernate.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.breskul.bibernate.annotation.enums.Strategy.SEQUENCE;

@Entity
@Data
@Table(name = "companies")
@EqualsAndHashCode(exclude = "noteComplex")
public class Company {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private NoteComplex noteComplex;

}