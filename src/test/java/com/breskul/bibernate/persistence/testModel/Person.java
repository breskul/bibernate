package com.breskul.bibernate.persistence.testModel;

import com.breskul.bibernate.annotations.Column;
import com.breskul.bibernate.annotations.Id;
import com.breskul.bibernate.annotations.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class Person {

    @Id
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
}
