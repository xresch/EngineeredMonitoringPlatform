# Introduction

The Engineered Monitoring Platform(EMP) is a project which arised from the need to have a easy to customize Monitoring & Dashboarding solution. Company specific needs can easily be implemented through extensions and therefore can be tailored to a lot of situations. With a flexible Role & permission management it allows to grant access in verious ways, from public dashboards that do not need any login down to sharing dashboards with specific users and groups.
EMP is built upon the CoreFramework(CFW) project.

The following were the goals in mind when EMP was created:
- Dashboarding of status information (green, yellow red) for a lot of data.
- Dashboarding of time based information (charts)
- Dashboards can have customized parameters
- eMail Alerting based on widget settings (if supported by widget)
- Flexible Permission Management (Users/Roles/Permissions)
- Extendable with custom widgets to tailor it to specific needs.

#Quick Start
You can find a lot of tutorials on the EMP Youtube Channel :
https://www.youtube.com/@EMPMonitoring

Best Videos to Start:
* [Installation and Setup](https://www.youtube.com/watch?v=0Ug1daCedfs)
* [Dashboarding Intro](https://www.youtube.com/watch?v=dZvoUlYOqbI)
* [Query Intro](https://www.youtube.com/watch?v=U8JH9mVJtxM)

Youtube Playlists:
* [User Tutorials](https://www.youtube.com/watch?v=raeSbIi18Ks&list=PLRvyXNOgocHQCgGWolDdZS864tQ4grycU&pp=iAQB)
* [Query Language Tutorials](https://www.youtube.com/watch?v=U8JH9mVJtxM&list=PLRvyXNOgocHSM2MD_fsxBHcM-2V40qYSZ&pp=iAQB)
* [Query Sources](https://www.youtube.com/watch?v=LHR4OXoysOc&list=PLRvyXNOgocHRgoK4xFLRoB81PilmEnNHd&pp=iAQB)
* [Admin Tutorials](https://www.youtube.com/watch?v=0Ug1daCedfs&list=PLRvyXNOgocHTbIBuor6_wyMeuY9NLWOBC&pp=iAQB)

# Features
Following is a list of features of EMP:
* Dashboards
* Query Language to fetch, manipulate format data for displaying it 
* Credential Management
* Task Scheduler 
* Fully functional user management with roles, groups and permissions
* Connectors to various Databases and other data sources
* Users have the possibility to create and manage their own groups for sharing
* API Endpoints


# Included Query Sources
The query language can grab data from various sources.
Here a list of most of them:
- web
- jdbc
- mssql
- mysql
- oracle
- postgres
- mongodb
- prometheus
- appdb (DB of EMP)
- applog (Log of EMP)
- auditlog (Audit Log of EMP)
- csv
- eavstats
- empty
- influxql
- json
- random
- stepapi
- stepdata
- text
- threaddump


# Included Dashboard Widgets
EMP is delivered with the following widgets:
- Standard Widgets
- Text
  - Label
  - List
  - Checklist
  - CSV Table
  - Tags
  - Image
  - HTML Editor
  - Website
  - Youtube Video
  - Refresh Time
- Advanced
  - Default Refresh
  - Force Refresh
  - Parameters
  - Replica
  - Javascript
  - Custom Threshold Legend
  - Display Query Results
- Database
  - Chart
    - Postgres Query Chart
    - MySQL Query Chart
    - MSSQL Query Chart
    - Oracle Query Chart
    - Generic JDBC Query Chart
    - InfluxQL Query Chart
    - MongoDB Query Chart
  - Status
    - Postgres Query Status
    - MySQL Query Status
    - MSSQL Query Status
    - Oracle Query Status
    - Generic JDBC Query Status
    - InfluxQL Query Status
    - MongoDB Query Status
- Web
  - Evaluate Response
- Automic Workload Automation (AWA)
  - AWA Job Status - Current
  - AWA Job Status - Current Grouped
  - AWA Job Status - History
  - AWA Jobs with Status
  - AWA Job Status Legend
- Dynatrace
  - Host Details
  - Host Events
  - Host Logs
  - Host Metrics Chart
  - Host Metrics Status
  - Host Processes
  - Host Unit Consumption By Tags
  - Process Events
  - Process Logs
  - Process Metrics Chart
- Exense STEP
  - Scheduler Status
  - Scheduler Status By Project
  - Scheduler Status By User
  - Scheduler Status History
  - Scheduler Executions Last N
  - Scheduler Executions Time Range
  - Scheduler Duration Chart
  - Scheduler Metrics Chart
  - Status Legend
- Prometheus
  - Instant Threshold
  - Range Chart
- Silk Performance Manager(SPM) - End of Life
  - SPM Project Status
  - SPM Monitor Status
  - SPM Monitor Status for Projects
  - SPM Monitor Status All
  - SPM Counters for Monitor
  - SPM Counters for Project
  - SPM Timers for Monitor
  - SPM Timers for Project
  - SPM Status Legend
  - SPM Measure Legend

 

