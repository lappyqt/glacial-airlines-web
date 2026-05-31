package com.lappyqt.glacialairlines.enums;

public enum DocumentType {
    PASSPORT_RUSSIAN("Паспорт РФ"),
    INTERNATIONAL("Загранпаспорт"),
    BIRTH_CERTIFICATE("Свидетельство о рождении");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
