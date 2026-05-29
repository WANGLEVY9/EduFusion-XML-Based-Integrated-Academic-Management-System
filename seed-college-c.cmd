@echo off
REM ============================================================
REM EduFusion - College C (MySQL) 差异化数据批量导入脚本
REM 用法：双击运行，或在命令行直接执行
REM 前提：Docker 容器 "edufusion-mysql" 必须正在运行
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ============================================
echo  College C - MySQL 数据批量导入
echo  目标：65名学生, 52门课程, ~390条选课记录
echo ============================================
echo.

REM 检查 Docker 是否可用
where docker >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 docker 命令，请确保 Docker Desktop 已安装并运行
    exit /b 1
)

REM 检查容器是否运行
docker ps --format "{{.Names}}" | findstr /C:"edufusion-mysql" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 容器 "edufusion-mysql" 未在运行状态
    echo 请先执行 docker-compose up -d 启动数据库容器
    exit /b 1
)

echo [1/3] 正在插入学生数据 (C001-C065) ...
echo [2/3] 正在插入课程数据 (C101-C152) ...
echo [3/3] 正在插入选课数据 (~390条) ...

type "%~dp0docker\mysql\insert-data-c-differentiated.sql" | docker exec -i edufusion-mysql mysql -u root -p123456 edufusion_iams_c

if %ERRORLEVEL% NEQ 0 (
    echo [错误] 数据导入失败，请检查 Docker 容器日志
    exit /b 1
)

echo.
echo ============================================
echo  College C 数据导入成功！
echo   - 学生: 65 人
echo   - 课程: 52 门
echo   - 选课: ~390 条
echo ============================================
echo.
pause
