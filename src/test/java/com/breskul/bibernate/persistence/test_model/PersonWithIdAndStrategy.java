package com.breskul.bibernate.persistence.test_model;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Strategy;
import com.breskul.bibernate.annotation.Table;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "users")
public class PersonWithIdAndStrategy {

	@Id
	@GeneratedValue(strategy = Strategy.IDENTITY)
	private Long id;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;


	private LocalDate birthday;

}