package com.breskul.bibernate.persistence.model;

import com.breskul.bibernate.annotation.OneToMany;

import java.util.List;

/**
 * {@link EntityNode} provides an abstraction for entity. In case parent entity has collection field {@link OneToMany}, it is easier to think of it as a tree with parent node and childes nodes
 * @param entity {@link Object} entity to be inserted
 * @param childes {@link List} list of child nodes to be inserted in database
 */
public record EntityNode(Object entity, List<EntityNode> childes) {
}
