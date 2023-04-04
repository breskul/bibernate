package com.breskul.bibernate.persistence.test_model;

import com.breskul.bibernate.annotation.*;
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