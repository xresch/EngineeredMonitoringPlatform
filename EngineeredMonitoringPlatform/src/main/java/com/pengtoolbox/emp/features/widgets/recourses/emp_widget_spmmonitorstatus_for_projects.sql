WITH rankedResults AS(
SELECT [ProjectID]
      ,[ProjectName]
      ,[ProjectIsActive]
      ,[MonitorID]
      ,[MonitorName]
      ,[MonitorIsActive]
      ,[LocationID]
      ,[LocationName]
      ,[MeasureName]
      ,[SeriesTime]
      --,[ExactTime]
      ,[Aggregation]
      --,[ValCount]
      --,[ValSum]
      --,[ValMin]
      --,[ValMax]
      ,[Value]
      --,[RawCount]
                  , ROW_NUMBER() OVER(PARTITION BY [MonitorID]
  					ORDER BY [SeriesTime] DESC) AS rk
  FROM [TMART].[dbo].[SV_V_Monitors_TimeSeriesData]
  WHERE [ProjectIsActive] = 1
  AND [Aggregation] <= 15
  AND [MonitorIsActive] = 1
  AND [SeriesTime] >= DateADD(minute, -15, GETUTCDATE())
  AND [MeasureName] = ?
  AND [ProjectID] = ?
  )
select r.*
FROM rankedResults r
WHERE r.rk = 1
ORDER BY ProjectName, MonitorName