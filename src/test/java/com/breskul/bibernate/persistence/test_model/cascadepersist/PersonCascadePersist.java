package com.breskul.bibernate.persistence.test_model.cascadepersist;

import com.breskul.bibernate.annotation.*;
import com.breskul.bibernate.annotation.enums.CascadeType;
import com.breskul.bibernate.annotation.enums.Strategy;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@ToString(exclude = "notes")
@Table(name = "users")
public class PersonCascadePersist {

    @Id
    @GeneratedValue(strategy = Strategy.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;


    @Column(name = "birthday")
    private LocalDate birthday;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<NoteComplexCascadePersist> notes = new ArrayList<>();

    public void addNote(NoteComplexCascadePersist note) {
        note.setPerson(this);
        notes.add(note);
    }
}