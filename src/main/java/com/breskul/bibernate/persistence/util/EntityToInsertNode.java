package com.breskul.bibernate.persistence.util;

import java.util.List;

public record EntityToInsertNode(Object entity, List<EntityToInsertNode> childes) {
}
