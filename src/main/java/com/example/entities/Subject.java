/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package com.example.entities;

import lombok.Data;

@Data
public class Subject {
    private String identifier;
    private String type;

    public Subject() {
    }

    public Subject(final String identifier) {
        this.identifier = identifier;
    }

    public Subject(final String identifier, final String type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "identifier='" + identifier + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
