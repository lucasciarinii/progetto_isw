package org.example.server.model.interfaces;

public interface Visitable {
    void accept(Visitor visitor);
}
