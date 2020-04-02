SELECT p.projectName AS ProjectName,
	   p.ProjectID_pk AS ProjectID,
	   m.Name AS MeasureName,
	   --r.Time_pk,
	   sum(r.ValSum)/ sum(r.ValCount) Value, 
	   sum(r.ValCount) ValCount, 
	   sum(r.ValSum) ValSum, 
	   sum(r.ValSumSquare) ValSumSquare, 
	   min(r.ValMin) ValMin, 
	   max(r.ValMax) ValMax, 
	   sum(r.BoundCount1) BoundCount1, 
	   sum(r.BoundCount2) BoundCount2
FROM SV_ExecutionSets s   
	 INNER JOIN SV_Measures m 
	 	  ON (s.SetID_pk = m.SetID_fk) 
	 INNER JOIN SV_MeasureResults mr 
	 	  ON (m.MeasureID_pk = mr.MeasureID_pk_fk) 
	 INNER JOIN SV_TimeSeries ts 
	 	  ON (mr.TimeSeriesID_pk_fk=ts.TimeSeriesID_pk) 
	 INNER JOIN SV_Aggregations a 
	 	  ON (AggregationID_pk_fk=AggregationID_pk) 
	 	  AND Aggregation=15 
	 INNER JOIN SV_TimeSeriesData r 
	 	  ON (r.TimeSeriesID_pk_fk=ts.TimeSeriesID_pk)
	 INNER JOIN SCC_Projects p
	 	 on p.ProjectID_pk = s.ProjectID_fk
WHERE p.isActive = 1
     AND s.ProjectID_fk = ?
	 AND m.Name = ?
	 AND r.Time_pk >= DateADD(minute, -20, GETUTCDATE())
GROUP BY p.ProjectName, p.ProjectID_pk, m.Name
ORDER BY p.ProjectName DESC