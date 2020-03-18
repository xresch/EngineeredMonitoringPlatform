SELECT DISTINCT TOP 10 [MonitorID],[MonitorName],[ProjectID],[ProjectName]
  FROM [TMART].[dbo].[SV_V_Monitors_TimeSeriesData]
  WHERE [ProjectIsActive] = 1
  -- AND [MonitorIsActive] = 1
  AND [MonitorName] LIKE ?
  OR [ProjectName] LIKE ?
  COLLATE SQL_Latin1_General_CP1_CI_AS