@echo off
set MYSQL=C:\xampp\mysql\bin\mysql.exe
set DB=cchc_clinic_db
set SQL=%~dp0jakarta-webapp\database\cchc_clinic_db.sql
set USER=root
set PASS=

echo Dropping and reimporting %DB%...

"%MYSQL%" -u %USER% --password=%PASS% -e "DROP DATABASE IF EXISTS %DB%;"
if errorlevel 1 goto error

"%MYSQL%" -u %USER% --password=%PASS% < "%SQL%"
if errorlevel 1 goto error

echo Done.
pause
exit /b 0

:error
echo Failed. Check that XAMPP MySQL is running and the path is correct.
echo MYSQL path: %MYSQL%
pause
exit /b 1
