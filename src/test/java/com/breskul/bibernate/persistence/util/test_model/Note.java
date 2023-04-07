package com.breskul.bibernate.persistence.util.test_model;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
public class Note {

    private Long id;
    @Column(name = "body")
    private String body;
}
