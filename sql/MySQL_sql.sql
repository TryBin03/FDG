SELECT
    COLUMN_NAME                                                        AS COLNAME,
    IF(COLUMN_KEY = 'PRI',1,NULL)                                         AS KEYSEQ,
    DATA_TYPE                                                             AS TYPENAME,
    CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) AS UNSIGNED )    LENGTH
FROM
    information_schema.columns
WHERE
    table_schema = 'fdgtest'
AND table_name = 'test_1'
ORDER BY
    ORDINAL_POSITION
--��ѯ���б�
SELECT DISTINCT
    CONCAT_WS('.', TABLE_SCHEMA, TABLE_NAME) TABLEID,
    TABLE_NAME                               TABLENAME
FROM
    information_schema.columns
WHERE
    table_schema NOT IN ('information_schema',
                         'mysql',
                         'performance_schema')
--��ѯ������
SELECT
    CONCAT_WS('.', TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME) COLUMNID,
    COLUMN_NAME                                           COLUMNNAME,
    DATA_TYPE,
    CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) AS UNSIGNED )    LENGTH
FROM
    information_schema.columns
WHERE
    table_schema NOT IN ('information_schema',
                         'mysql',
                         'performance_schema')
--��ѯ���û����б���Ϣ
SELECT
    TABLE_SCHEMA                                                     AS SCHEMANAME,
    TABLE_NAME                                                        AS TABLENAME,
    COLUMN_NAME                                                         AS COLNAME,
    IF(COLUMN_KEY = 'PRI',1,NULL)                                         AS KEYSEQ,
    DATA_TYPE                                                             AS TYPENAME,
    CAST(IFNULL(CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION) AS UNSIGNED )    LENGTH
FROM
    information_schema.columns
WHERE
    table_schema NOT IN ('information_schema',
                         'mysql',
                         'performance_schema')
ORDER BY
    ORDINAL_POSITION