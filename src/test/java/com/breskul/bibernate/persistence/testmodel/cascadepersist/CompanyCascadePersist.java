package com.breskul.bibernate.persistence.testmodel.cascadepersist;

import com.breskul.bibernate.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.breskul.bibernate.annotation.Strategy.SEQUENCE;

@Entity
@Data
@Table(name = "companies")
@EqualsAndHashCode(exclude = "noteComplex")
public class CompanyCascadePersist {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;
    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private NoteComplexCascadePersist noteComplex;


}