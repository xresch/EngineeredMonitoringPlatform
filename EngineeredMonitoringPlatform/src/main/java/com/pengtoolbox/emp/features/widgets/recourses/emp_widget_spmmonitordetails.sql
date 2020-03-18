SELECT TOP(1) [ProjectID],[ProjectName],[ProjectIsActive],[MonitorID],[MonitorName],[MonitorIsActive]
  FROM [TMART].[dbo].[SV_V_Monitors_TimeSeriesData]
  WHERE [MonitorID] = ?