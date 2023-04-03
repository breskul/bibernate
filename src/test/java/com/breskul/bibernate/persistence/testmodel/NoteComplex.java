package com.breskul.bibernate.persistence.testmodel;

import com.breskul.bibernate.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.breskul.bibernate.annotation.Strategy.SEQUENCE;

@Entity
@Data
@Table(name = "notes")
@EqualsAndHashCode(exclude = {"companies"})
@ToString(exclude = {"companies"})
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

    @OneToMany
    private List<Company> companies = new ArrayList<>();

    public void addCompany(Company company) {
        company.setNoteComplex(this);
        companies.add(company);


    }

}
