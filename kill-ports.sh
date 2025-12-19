#!/bin/bash

# Скрипт для завершения процессов, занимающих порты из docker-compose.yml
# Работает в Git Bash и WSL на Windows

# Порты из docker-compose.yml (внешние порты хоста)
PORTS=(5672 15672 9411 9090 8080 8082 8083 8084 8085 8086 8088 9091 50000)

echo "Поиск и завершение процессов на портах из docker-compose.yml..."
echo ""

if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    for PORT in "${PORTS[@]}"; do
        echo "Проверка порта $PORT..."
        
        powershell.exe -Command "
            \$connections = Get-NetTCPConnection -LocalPort $PORT -ErrorAction SilentlyContinue
            if (\$connections) {
                \$pids = \$connections | Select-Object -ExpandProperty OwningProcess -Unique
                foreach (\$pid in \$pids) {
                    Write-Host \"  Найден процесс с PID: \$pid на порту $PORT\"
                    try {
                        Stop-Process -Id \$pid -Force -ErrorAction Stop
                        Write-Host \"    Процесс \$pid завершен\"
                    } catch {
                        Write-Host \"    Не удалось завершить процесс \$pid\"
                    }
                }
            } else {
                Write-Host \"  Порт $PORT свободен\"
            }
        "
    done
elif [[ -n "$WSL_DISTRO_NAME" ]]; then
    for PORT in "${PORTS[@]}"; do
        echo "Проверка порта $PORT..."
        PIDS=$(netstat -tuln 2>/dev/null | grep ":$PORT " | awk '{print $7}' | cut -d'/' -f1 | sort -u)
        if [ -z "$PIDS" ]; then
            PIDS=$(lsof -ti:$PORT 2>/dev/null)
        fi
        
        if [ -n "$PIDS" ]; then
            for PID in $PIDS; do
                PID=$(echo $PID | tr -d '\r\n' | xargs)
                if [ -n "$PID" ] && [ "$PID" != "0" ]; then
                    echo "  Найден процесс с PID: $PID на порту $PORT"
                    kill -9 $PID 2>/dev/null && echo "    Процесс $PID завершен" || echo "    Не удалось завершить процесс $PID"
                fi
            done
        else
            echo "  Порт $PORT свободен"
        fi
    done
else
    for PORT in "${PORTS[@]}"; do
        echo "Проверка порта $PORT..."
        PIDS=$(lsof -ti:$PORT 2>/dev/null)
        
        if [ -n "$PIDS" ]; then
            for PID in $PIDS; do
                echo "  Найден процесс с PID: $PID на порту $PORT"
                kill -9 $PID 2>/dev/null && echo "    Процесс $PID завершен" || echo "    Не удалось завершить процесс $PID"
            done
        else
            echo "  Порт $PORT свободен"
        fi
    done
fi

echo ""
echo "Готово!"

