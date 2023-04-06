package com.breskul.bibernate.persistence.test_model;

import com.breskul.bibernate.annotation.*;

import com.breskul.bibernate.annotation.enums.FetchType;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@ToString(exclude = "notes")
@Table(name = "users")
public class PersonWithoutGeneratedValueWithEagerFetch {

    @Id
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    private LocalDate birthday;

    @OneToMany(fetch = FetchType.EAGER)
    private List<NoteWithoutGeneratedValueWithEagerFetchFromPerson> notes = new ArrayList<>();

    public void addNote(NoteWithoutGeneratedValueWithEagerFetchFromPerson note) {
        note.setPerson(this);
        notes.add(note);
    }
}
