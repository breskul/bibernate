package com.breskul.bibernate.persistence.test_model.cascadenone;

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
public class PersonCascadeNone {

    @Id
    @GeneratedValue(strategy = Strategy.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;


    @Column(name = "birthday")
    private LocalDate birthday;

    @OneToMany(cascade = CascadeType.NONE)
    private List<NoteComplexCascadeNone> notes = new ArrayList<>();

    public void addNote(NoteComplexCascadeNone note) {
        note.setPerson(this);
        notes.add(note);
    }
}