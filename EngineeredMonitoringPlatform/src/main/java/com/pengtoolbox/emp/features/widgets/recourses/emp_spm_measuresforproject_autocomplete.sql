SELECT DISTINCT TOP 10 [ProjectID]
      ,[ProjectName]
	  ,[MeasureName] 
FROM [TMART].[dbo].[SV_V_Monitors_TimeSeriesData]
WHERE [ProjectIsActive] = 1
  AND [MonitorIsActive] = 1
  AND [MeasureName] LIKE ? 
  AND [ProjectID] = ?