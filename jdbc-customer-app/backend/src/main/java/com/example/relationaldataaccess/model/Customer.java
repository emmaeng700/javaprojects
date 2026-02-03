package com.example.relationaldataaccess.model;

public record Customer(Long id, String firstName, String lastName) {

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%d, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }
}