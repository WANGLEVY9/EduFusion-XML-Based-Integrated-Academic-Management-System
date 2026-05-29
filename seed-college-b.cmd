@echo off
REM ============================================================
REM EduFusion - College B (Oracle) 差异化数据批量导入脚本
REM 用法：双击运行，或在命令行直接执行
REM 前提：Docker 容器 "edufusion-oracle" 必须正在运行
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ============================================
echo  College B - Oracle 数据批量导入
echo  目标：55名学生, 52门课程, ~165条选课记录
echo ============================================
echo.

REM 检查 Docker 是否可用
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 docker 命令，请确保 Docker Desktop 已安装并运行
    exit /b 1
)

REM 检查容器是否运行
docker ps --format "{{.Names}}" | findstr /C:"edufusion-oracle" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 容器 "edufusion-oracle" 未在运行状态
    echo 请先执行 docker-compose up -d 启动数据库容器
    exit /b 1
)

echo [1/3] 正在插入学生数据 (B001-B055) ...
echo [2/3] 正在插入课程数据 (B101-B152) ...
echo [3/3] 正在插入选课数据 (~165条) ...

REM Oracle 通过管道传 SQL 给 sqlplus
type "%~dp0docker\oracle\insert-data-b-differentiated.sql" | docker exec -i edufusion-oracle sqlplus -s EDUFUSION_B/EduFusion123@//localhost:1521/EDUFUSION_B

if %ERRORLEVEL% NEQ 0 (
    echo [警告] sqlplus 返回非零状态，但数据可能已部分导入
    echo 请检查 Oracle 容器日志确认
)

echo.
echo ============================================
echo  College B 数据导入完成！
echo   - 学生: 55 人
echo   - 课程: 52 门
echo   - 选课: ~165 条
echo ============================================
echo.
pause
