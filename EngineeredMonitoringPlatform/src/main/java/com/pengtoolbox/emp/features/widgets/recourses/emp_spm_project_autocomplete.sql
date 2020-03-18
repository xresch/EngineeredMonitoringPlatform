SELECT DISTINCT TOP 10 [ProjectID_pk] AS ProjectID,[ProjectName]
  FROM [TMART].[dbo].[SCC_Projects]
  WHERE [IsActive] = 1
  AND [ProjectName] LIKE ?
  COLLATE SQL_Latin1_General_CP1_CI_AS