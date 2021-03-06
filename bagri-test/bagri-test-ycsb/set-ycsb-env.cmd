
:config

set app_home=.

rem specify the JVM heap size
rem set memory=1024m
set memory=2048m

set schema_addr=localhost:10150
rem ,localhost:10151
rem set schema_addr=192.168.1.139:10150

:start
if "%java_home%"=="" (set java_exec=java) else (set java_exec=%java_home%\bin\java)

:launch

set java_opts=-Xms%memory% -Xmx%memory% 

set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=ycsb-logging.xml
rem set java_opts=%java_opts% -Dhazelcast.logging.type=slf4j -Dlogback.configurationFile=hz-client-logging.xml
set java_opts=%java_opts% -Dlog.name=ycsb-client -Dhz.log.level=warn -Dbdb.log.level=info

set java_opts=%java_opts% -Dhazelcast.client.event.thread.count=1

set java_opts=%java_opts% -Dbdb.schema.address=%schema_addr%
set java_opts=%java_opts% -Dbdb.schema.name=YCSB
set java_opts=%java_opts% -Dbdb.schema.user=guest
set java_opts=%java_opts% -Dbdb.schema.password=password

rem possible values are: all, any, query-key-owner, param-hash-owner, param-value-owner, partition key value
set java_opts=%java_opts% -Dbdb.client.submitTo=param-hash-owner
set java_opts=%java_opts% -Dbdb.client.ownerParam=uri
set java_opts=%java_opts% -Dbdb.client.bufferSize=32
set java_opts=%java_opts% -Dbdb.client.fetchSize=10
set java_opts=%java_opts% -Dbdb.client.connectAttempts=3
set java_opts=%java_opts% -Dbdb.client.loginTimeout=30
set java_opts=%java_opts% -Dbdb.client.smart=true
set java_opts=%java_opts% -Dbdb.client.poolSize=32
set java_opts=%java_opts% -Dbdb.client.healthCheck=skip
set java_opts=%java_opts% -Dbdb.client.queryCache=true
set java_opts=%java_opts% -Dbdb.client.customAuth=true
set java_opts=%java_opts% -Dbdb.client.fetchAsynch=false
rem set java_opts=%java_opts% -Dbdb.client.sharedConnection=true
set java_opts=%java_opts% -Dbdb.client.contentSerializers=BMAP
rem set java_opts=%java_opts% -Dbdb.client.contentSerializer.MAP= 

rem set java_opts=%java_opts% -Dbdb.client.storeMode=merge
rem set java_opts=%java_opts% -Dbdb.client.txTimeout=100
rem set java_opts=%java_opts% -Dbdb.client.txLevel=skip
set java_opts=%java_opts% -Dbdb.document.cache.elements=true
set java_opts=%java_opts% -Dbdb.document.compress=false
set java_opts=%java_opts% -Dbdb.document.data.format=BMAP
set java_opts=%java_opts% -Dbdb.document.map.merge=true

rem set java_opts=%java_opts% -Duser.country=US -Duser.language=en

exit /b


