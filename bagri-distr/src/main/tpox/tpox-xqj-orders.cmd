@echo off
@
setlocal

call set-tpox-env.cmd

rem The current version of the TPoX expects the number of orders to be 10x the 
rem number of custacc docs.

rem insert orders to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/insOrder-xqj.xml -tr 25000 -u 10

rem get insert statistics
"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement executeXQuery InsertOrder ./stats.txt false

rem perform queries loopig by user count
for /l %%x in (50, 10, 100) do (
	echo %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/orders-xqj.xml -u %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" com.bagri.client.tpox.StatisticsCollector %admin_addr% %schema% QueryManagement executeXQuery Users=%%x ./stats.txt false
)
                                      

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-orders.cmd
goto exit

:exit
endlocal
@echo on