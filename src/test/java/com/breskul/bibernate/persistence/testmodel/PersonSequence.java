package com.breskul.bibernate.persistence.testmodel;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.annotation.Table;
import lombok.Data;

import java.time.LocalDate;


@Entity
@Data
@Table(name = "users")
public class PersonSequence {

	@Id
	@GeneratedValue(strategy = Strategy.SEQUENCE)
	private Long id;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;


	private LocalDate birthday;

}