@echo off
@
setlocal

call set-tpox-env.cmd

rem The current version of the TPoX expects the number of orders to be 10x the 
rem number of custacc docs.

rem insert orders to the cache
"%java_exec%" -server -showversion %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/insOrder.xml -tr 5000 -u 50

rem perform queries loopig by user count
for /l %%x in (5, 15, 200) do (
	echo %%x
	"%java_exec%" -server %java_opts% -cp "%app_home%\config\*;%app_home%\lib\*" net.sf.tpox.workload.core.WorkloadDriver -w queries/XQJ/orders.xml -u %%x -r 10
)
                                      

goto exit

:instructions

echo Usage:
echo %app_home%\tpox-xqj-orders.cmd
goto exit

:exit
endlocal
@echo on
