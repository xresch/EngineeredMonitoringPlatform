SELECT       g.LogTime AS TIMESTAMP, 
             m.Name AS MONITOR_NAME, 
             o.Name AS LOCATION_NAME, 
             l.Message AS LOG_MESSAGE,
             CASE 
                   WHEN g.Status = 1 THEN 'OK' 
                   WHEN g.Status = 2 THEN 'WARNING' 
                   WHEN g.Status = 3 THEN 'ERROR' END "STATUS"
             
FROM SV_Monitors m, 
     SV_Monitors_ExecServers x, 
     SCC_Locations o, 
     SV_MonitorTransactions t, 
     SV_ExecutionLogEntries g, 
     SV_ExecutionLogMessages l 
WHERE 
      m.ProjectID_fk = ?
      AND m.Name = ?
      AND LogTime >= ?
      AND LogTime <= ?
      AND m.Status = 1
      AND m.MonitorID_pk = t.MonitorID_fk
      AND t.TransactionID_pk_fk = g.TransID_fk
      AND g.MsgID_fk = l.MsgID_pk
      AND m.MonitorID_pk = x.MonitorID_pk_fk
      AND o.LocationID_pk = x.LocationID_pk_fk
ORDER BY g.LogTime DESC
