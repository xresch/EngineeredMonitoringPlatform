SELECT DISTINCT TOP 30 [MonitorID],[MonitorName],[ProjectID],[ProjectName]
	FROM (
	SELECT TOP 50000 [MonitorID],[MonitorName],[ProjectID],[ProjectName],[ProjectIsActive]
		FROM [TMART].[dbo].[SV_V_Monitors_TimeSeriesData]
		ORDER BY [SeriesTime] DESC
	) AS T
  WHERE [ProjectIsActive] = 1
  -- AND [MonitorIsActive] = 1
  AND [MonitorName] LIKE ?
  OR [ProjectName] LIKE ?
  COLLATE SQL_Latin1_General_CP1_CI_AS