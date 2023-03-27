package com.breskul.bibernate.persistence.testmodel;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.JoinColumn;
import com.breskul.bibernate.annotation.ManyToOne;
import com.breskul.bibernate.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static com.breskul.bibernate.annotation.Strategy.SEQUENCE;


@Data
@Entity
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
