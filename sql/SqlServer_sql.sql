SELECT
    col.COLUMN_NAME                                           AS COLNAME,
    IIF ( keycol.COLUMN_NAME IS NULL, NULL, 1)                   AS KEYSEQ,
    col.DATA_TYPE                                                   TYPENAME,
    COALESCE(col.CHARACTER_MAXIMUM_LENGTH,col.NUMERIC_PRECISION)    LENGTH
FROM
    INFORMATION_SCHEMA.COLUMNS col
LEFT JOIN
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE keycol
ON
    col.TABLE_NAME = keycol.TABLE_NAME
AND col.COLUMN_NAME = keycol.COLUMN_NAME
WHERE
    col.TABLE_NAME = 'test_1'
AND col.TABLE_SCHEMA = 'dbo'
ORDER BY
    col.TABLE_NAME
--查询所有表
SELECT
    col.TABLE_SCHEMA+'.'+col.TABLE_NAME AS TABLEID,
    col.TABLE_NAME                      AS TABLENAME
FROM
    INFORMATION_SCHEMA.COLUMNS col
--查询所有列
SELECT
    col.TABLE_SCHEMA+'.'+col.TABLE_NAME+'.'+col.COLUMN_NAME AS COLUMNID,
    col.COLUMN_NAME                                         AS COLUMNNAME,
    col.DATA_TYPE,
    COALESCE(col.CHARACTER_MAXIMUM_LENGTH,col.NUMERIC_PRECISION) LENGTH
FROM
    INFORMATION_SCHEMA.COLUMNS col
--查询该用户所有表信息
SELECT
    col.TABLE_SCHEMA                                        AS SCHEMANAME,
    col.TABLE_NAME                                           AS TABLENAME,
    col.COLUMN_NAME                                            AS COLNAME,
    IIF ( keycol.COLUMN_NAME IS NULL, NULL, 1)                   AS KEYSEQ,
    col.DATA_TYPE                                                   TYPENAME,
    COALESCE(col.CHARACTER_MAXIMUM_LENGTH,col.NUMERIC_PRECISION)    LENGTH
FROM
    INFORMATION_SCHEMA.COLUMNS col
LEFT JOIN
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE keycol
ON
    col.TABLE_NAME = keycol.TABLE_NAME
AND col.COLUMN_NAME = keycol.COLUMN_NAME
ORDER BY
    col.TABLE_NAME