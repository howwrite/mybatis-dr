package com.github.howwrite.model;

public class IgnoreCaseField {
    private final String upperCase;
    private final String lowerCase;

    public IgnoreCaseField(String columnName) {
        this.upperCase = columnName.toUpperCase();
        this.lowerCase = columnName.toLowerCase();
    }

    @Override
    public int hashCode() {
        return upperCase.hashCode() + lowerCase.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IgnoreCaseField other)) {
            return false;
        }
        return other.upperCase.equals(upperCase) && other.lowerCase.equals(lowerCase);
    }
}
