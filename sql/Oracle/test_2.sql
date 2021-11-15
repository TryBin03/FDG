CREATE TABLE
    test_2
(
    key1 NUMBER NOT NULL,
    key2 VARCHAR2(255) NOT NULL,
    key3 DATE NOT NULL,
    col1 NUMBER,
    col2 VARCHAR2(255),
    col3 TIMESTAMP(6),
    col4 DATE,
    PRIMARY KEY (key1, key2, key3)
);