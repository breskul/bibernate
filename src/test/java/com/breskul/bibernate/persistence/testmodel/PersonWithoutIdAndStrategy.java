package com.breskul.bibernate.persistence.testmodel;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import lombok.Data;

import java.time.LocalDate;


@Data
@Entity
public class PersonWithoutIdAndStrategy {

	@Id
	private Long id;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;


	private LocalDate birthday;

}