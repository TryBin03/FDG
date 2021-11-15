CREATE TABLE
    test_2
(
    key1 VARCHAR(255) NOT NULL,
    key2 INT(255) NOT NULL AUTO_INCREMENT,
    key3 DATETIME NOT NULL,
    col1 VARCHAR(255),
    col2 VARCHAR(255),
    date1 DATE,
    date2 DATETIME,
    date3 YEAR,
    date4 TIME,
    date5 TIMESTAMP NULL,
    PRIMARY KEY (key2, key1, key3)
)
    ENGINE=InnoDB DEFAULT CHARSET=latin1;