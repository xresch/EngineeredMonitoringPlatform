SELECT DISTINCT TOP 1 [ProjectID_pk] AS ProjectID,[ProjectName],[Description],[IsActive]
  FROM [TMART].[dbo].[SCC_Projects]
  WHERE [ProjectID_pk] = ?