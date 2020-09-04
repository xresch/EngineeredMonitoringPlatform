SELECT DISTINCT TOP 10 [ProjectID]
      ,[ProjectName]
      ,[MonitorID]
      ,[MonitorName]
	  ,[MeasureName] 
FROM [TMART].[dbo].[SV_V_Monitors_TimeSeriesData]
WHERE [ProjectIsActive] = 1
  AND [MonitorIsActive] = 1
  AND [MeasureName] LIKE ? 
  AND [MonitorID] = ?