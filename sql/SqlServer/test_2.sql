CREATE TABLE
    test_2
(
    key1 INT NOT NULL,
    key2 VARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    key3 DATE NOT NULL,
    col1 INT,
    col2 VARCHAR(255) COLLATE SQL_Latin1_General_CP1_CI_AS,
    col4 DATE,
    PRIMARY KEY (key1, key2, key3)
);