SELECT * FROM ( 
	SELECT DISTINCT
	AH_IDNR AS ID,
	AH_CLIENT AS CLIENT_ID,
	AH_NAME AS NAME,
	AH_OTYPE AS TYPE,
	AH_TIMESTAMP2 AS START_TIME,
	AH_TIMESTAMP4 AS END_TIME,
	AH_HOSTDST AS HOST_DESTINATION,
	AH_HOSTSRC AS HOST_SOURCE,
	AH_RUNTIME AS DURATION_SECONDS,
	(SELECT CASE WHEN AH_STATUS > 0 THEN AH_STATUS ELSE EH_STATUS END FROM DUAL) AS STATUS_CODE,
	  (SELECT CASE
	    WHEN (SELECT CASE WHEN AH_STATUS > 0 THEN AH_STATUS ELSE EH_STATUS END FROM DUAL) = 0 THEN 'ENDED OK'
	    WHEN (SELECT CASE WHEN AH_STATUS > 0 THEN AH_STATUS ELSE EH_STATUS END FROM DUAL) <= 1654 THEN 'RUNNING'
	    WHEN (SELECT CASE WHEN AH_STATUS > 0 THEN AH_STATUS ELSE EH_STATUS END FROM DUAL) BETWEEN 1655 AND 1799 THEN 'WAITING'
	    WHEN (SELECT CASE WHEN AH_STATUS > 0 THEN AH_STATUS ELSE EH_STATUS END FROM DUAL) BETWEEN 1800 AND 1899 THEN 'ABNORMAL ENDING'
	    WHEN (SELECT CASE WHEN AH_STATUS > 0 THEN AH_STATUS ELSE EH_STATUS END FROM DUAL) BETWEEN 1900 AND 1999 THEN 'ENDED OK'
	    ELSE 'UNKNOWN'
	    END FROM DUAL
	  ) as STATUS
	FROM UC4.AH
	LEFT JOIN UC4.EH
    ON AH.AH_NAME = EH.EH_NAME
	WHERE AH_CLIENT = ?
	AND AH_NAME = ?
	ORDER BY AH_IDNR DESC
) WHERE STATUS_CODE <> 0 AND ROWNUM <= ?