package com.breskul.bibernate.persistence.testmodel;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;


@Data
@Entity
@EqualsAndHashCode
@Table(name = "users")
public class PersonWithoutIdAndStrategy {

	@Id
	private Long id;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;
	private LocalDate birthday;

}
