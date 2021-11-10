SELECT
    utc.COLUMN_NAME                          AS COLUMN_NAME,
    DECODE(uc.constraint_type, 'P', 1, NULL) AS KEYSEQ,
    DATA_TYPE                                AS TYPENAME,
    DATA_LENGTH                              AS LENGTH
FROM
    user_tab_columns utc
LEFT JOIN
    user_cons_columns ucc
ON
    utc.TABLE_NAME = ucc.TABLE_NAME
AND utc.COLUMN_NAME = ucc.COLUMN_NAME
LEFT JOIN
    user_constraints uc
ON
    ucc.constraint_name = uc.constraint_name
WHERE
    utc.TABLE_NAME = 'test_1'
AND uc.index_name IS NOT NULL
OR  (
        utc.TABLE_NAME = 'test_1'
    AND uc.index_name IS NULL
    AND ucc.constraint_name IS NULL)
ORDER BY
    COLUMN_ID
--��ѯ���б�   DEV Ϊ�û���
SELECT
    OWNER||'.'||TABLE_NAME  TABLEID,
    TABLE_NAME                               TABLENAME
FROM
    user_constraints
WHERE INDEX_OWNER = 'DEV'

--��ѯ������    DEV Ϊ�û���
SELECT
    'DEV'||'.'||TABLE_NAME||'.'||COLUMN_NAME COLUMNID,
    COLUMN_NAME                                           COLUMNNAME,
    DATA_TYPE
FROM
    user_tab_columns
    
 --��ѯ���û����б���Ϣ
SELECT
    'DEV'                                                     AS SCHEMANAME,
    utc.TABLE_NAME                                                        AS TABLENAME,
    utc.COLUMN_NAME                          AS COLNAME,
    DECODE(uc.constraint_type, 'P', 1, NULL) AS KEYSEQ,
    DATA_TYPE                                AS TYPENAME,
    DATA_LENGTH                              AS LENGTH
FROM
    user_tab_columns utc
LEFT JOIN
    user_cons_columns ucc
ON
    utc.TABLE_NAME = ucc.TABLE_NAME
AND utc.COLUMN_NAME = ucc.COLUMN_NAME
LEFT JOIN
    user_constraints uc
ON
    ucc.constraint_name = uc.constraint_name
WHERE
    uc.index_name IS NOT NULL
OR  (
    uc.index_name IS NULL
    AND ucc.constraint_name IS NULL)
