CREATE TABLE
    test_1
(
    key1 VARCHAR(255) NOT NULL,
    key2 INT(255) NOT NULL,
    key3 DATETIME NOT NULL,
    col1 VARCHAR(255),
    col2 VARCHAR(255),
    col3 INT(255),
    PRIMARY KEY (key1, key2, key3)
)
    ENGINE=InnoDB DEFAULT CHARSET=latin1;