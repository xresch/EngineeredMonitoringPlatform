DECLARE @earliest DATETIME
SET @earliest = ?

DECLARE @latest DATETIME
SET @latest = ?

SELECT ProjectName AS ProjectName, 
Name AS 'Rule',
COUNT(Name) AS ViolationCount,
'DurationAvg' = CONVERT(VARCHAR(5), AVG(DATEDIFF(s, BeginsAt, EndsAt))/60/60)
   + ':' + RIGHT('0' + CONVERT(VARCHAR(2), AVG(DATEDIFF(s, BeginsAt, EndsAt))/60%60), 2)
   + ':' + RIGHT('0' + CONVERT(VARCHAR(2), AVG(DATEDIFF(s, BeginsAt, EndsAt)) % 60), 2),
DurationAvgSeconds = AVG(DATEDIFF(s, BeginsAt, EndsAt)),
CAST(ROUND((1 - CAST(SUM(DATEDIFF(s, BeginsAt, EndsAt)) AS FLOAT) / CAST(DATEDIFF(s, @earliest, @latest) AS FLOAT)) * 100, 2) AS VARCHAR) + '%' AS Availability
FROM SV_V_FilteredIncidents
WITH (NOLOCK)
JOIN SCC_Projects
WITH (NOLOCK)
ON ProjectID_pk = ?
WHERE ProjectID_fk  = ProjectID_pk
AND (
	(BeginsAt BETWEEN @earliest AND @latest) 
	OR (EndsAt BETWEEN @earliest AND @latest) 
	OR ( 
		BeginsAt < @latest 
		AND (
			EndsAt>= @earliest 
			AND EndsAt <= @latest
		)
	)
) 
AND (Severity = 'Service Target Violation' OR InvalidateInReports = 1) 
GROUP BY ProjectName, Name
ORDER BY ProjectName ASC 
OPTION (HASH UNION)