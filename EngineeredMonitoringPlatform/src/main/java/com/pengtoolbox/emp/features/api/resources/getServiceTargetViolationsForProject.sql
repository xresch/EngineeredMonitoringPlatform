DECLARE @earliest DATETIME
SET @earliest = ?

DECLARE @latest DATETIME
SET @latest = ?

SELECT 
ProjectName AS PROJECT,
Name AS 'RULE',
BeginsAt AS 'FROM', 
EndsAt AS 'TO', 
DURATION = CONVERT(VARCHAR(5), DATEDIFF(s, BeginsAt, EndsAt)/60/60)
  + ':' + RIGHT('0' + CONVERT(VARCHAR(2), DATEDIFF(s, BeginsAt, EndsAt)/60%60), 2)
  + ':' + RIGHT('0' + CONVERT(VARCHAR(2), DATEDIFF(s, BeginsAt, EndsAt) % 60), 2),
DURATION_SECONDS = DATEDIFF(s, BeginsAt, EndsAt)
FROM SV_V_FilteredIncidents
WITH (NOLOCK)
JOIN SCC_Projects
WITH (NOLOCK)
ON ProjectID_pk = ProjectID_fk 
WHERE ProjectID_fk = ?
AND ((BeginsAt BETWEEN @earliest AND @latest) OR (EndsAt BETWEEN @earliest AND @latest) OR ((BeginsAt < @latest AND (EndsAt>= @earliest AND EndsAt <= @latest)))) 
AND (Severity = 'Service Target Violation' OR InvalidateInReports = 1) 
ORDER BY ProjectName, BeginsAt ASC
OPTION (HASH UNION)