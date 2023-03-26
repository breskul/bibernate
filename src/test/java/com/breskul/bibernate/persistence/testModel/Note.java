package com.breskul.bibernate.persistence.testModel;

import com.breskul.bibernate.annotations.Column;
import com.breskul.bibernate.annotations.Id;
import com.breskul.bibernate.annotations.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
public class Note {

    @Id
    private Long id;
    @Column(name = "body")
    private String body;
}
