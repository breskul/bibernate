package com.breskul.bibernate.persistence.testmodel;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.OneToMany;
import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.annotation.Table;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@ToString(exclude = "notes")
@Table(name = "users")
public class Person {

	@Id
	@GeneratedValue(strategy = Strategy.IDENTITY)
	private Long id;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;


	private LocalDate birthday;

	@OneToMany
	private List<NoteComplex> notes = new ArrayList<>();

	public void addNote(NoteComplex note) {
		note.setPerson(this);
		notes.add(note);
	}
}