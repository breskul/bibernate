package com.breskul.bibernate.persistence.util;

import java.util.List;

public record Node(Object entity, List<Node> childes) {
}
