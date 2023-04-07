package com.breskul.bibernate.persistence.test_model.cascadepersist;

import com.breskul.bibernate.annotation.*;
import com.breskul.bibernate.annotation.enums.CascadeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.breskul.bibernate.annotation.enums.Strategy.SEQUENCE;

@Entity
@Data
@Table(name = "notes")
@EqualsAndHashCode(exclude = {"companies"})
@ToString(exclude = {"companies"})
public class NoteComplexCascadePersist {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;
    private String body;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "person_id")
    private PersonCascadePersist person;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<CompanyCascadePersist> companies = new ArrayList<>();

    public void addCompany(CompanyCascadePersist company) {
        company.setNoteComplex(this);
        companies.add(company);


    }
}
