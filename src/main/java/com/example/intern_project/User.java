package com.example.intern_project;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String name;

    public User() {}

    public User(String name) {
        this.name = name;
    }
}