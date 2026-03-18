#!/bin/bash

# Doris服务管理脚本(支持多版本同时运行)
# 功能:在多台服务器上管理多个版本的Doris的FE和BE服务
# 用法: ./doris_manager.sh [start|stop|restart|status] [all|fe|be] [server1 server2 ...]
# 示例: 
#   ./doris_manager.sh start all                    # 在所有服务器上启动所有服务
#   ./doris_manager.sh restart fe master01 master02 # 在指定服务器上重启FE
#   ./doris_manager.sh status be                    # 查看所有服务器上的BE状态

# 使用更安全的错误处理
set -eo pipefail

# 全局配置
DEFAULT_SERVERS=("master01" "master02" "master03")
# 当前脚本管理的Doris版本路径
DORIS_HOME="/opt/doris/apache-doris-3.1.3-bin-x64"
# 所有已知的Doris版本
KNOWN_VERSIONS=(
    "/opt/doris/apache-doris-2.1.11-bin-x64"
    "/opt/doris/apache-doris-3.1.3-bin-x64"
)

TIMEOUT=60  # 命令执行超时时间(秒)
CHECK_INTERVAL=3  # 状态检查间隔(秒)
MAX_RETRIES=3     # 最大重试次数

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} [$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} [$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} [$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} [$(date '+%Y-%m-%d %H:%M:%S')] $1" >&2
}

# 安全执行函数,允许部分命令失败
safe_exec() {
    local cmd="$1"
    local description="$2"
    
    log_info "$description"
    if eval "$cmd"; then
        log_success "$description 完成"
        return 0
    else
        local exit_code=$?
        log_warn "$description 失败 (退出码: $exit_code)"
        return $exit_code
    fi
}

# 检查远程服务器连通性
check_server_connectivity() {
    local server=$1
    if ssh -o ConnectTimeout=5 "$server" "exit" 2>/dev/null; then
        return 0
    else
        log_error "无法连接到服务器: $server"
        return 1
    fi
}

# 检查服务是否已安装
check_service_installed() {
    local server=$1
    local service_type=$2
    local doris_home="$3"
    
    case $service_type in
        fe)
            if ssh "$server" "[ -f '$doris_home/fe/bin/start_fe.sh' ]" 2>/dev/null; then
                return 0
            fi
            ;;
        be)
            if ssh "$server" "[ -f '$doris_home/be/bin/start_be.sh' ]" 2>/dev/null; then
                return 0
            fi
            ;;
        *)
            return 0
            ;;
    esac
    
    log_warn "服务器 $server 上未发现 $service_type 服务 (路径: $doris_home)"
    return 1
}

# 获取指定版本的PID文件路径
get_pid_file() {
    local doris_home="$1"
    local service_type="$2"
    
    case $service_type in
        fe)
            echo "$doris_home/fe/bin/fe.pid"
            ;;
        be)
            echo "$doris_home/be/bin/be.pid"
            ;;
        *)
            echo ""
            ;;
    esac
}

# 获取指定版本的服务状态
get_version_service_status() {
    local server=$1
    local service_type=$2
    local target_doris_home="$3"
    
    # 获取当前版本的PID文件路径
    local pid_file
    pid_file=$(get_pid_file "$target_doris_home" "$service_type")
    
    if [ -z "$pid_file" ]; then
        echo "UNKNOWN"
        return 2
    fi
    
    # 通过ssh检查PID文件是否存在并获取PID
    local pid
    pid=$(ssh "$server" "
        if [ -f '$pid_file' ]; then
            cat '$pid_file' 2>/dev/null
        fi
    " 2>/dev/null || echo "")
    
    if [ -n "$pid" ]; then
        # 检查PID对应的进程是否存在
        if ssh "$server" "ps -p $pid >/dev/null 2>&1"; then
            # 进一步检查进程的工作目录是否匹配目标DORIS_HOME
            local process_cwd
            process_cwd=$(ssh "$server" "ls -l /proc/$pid/cwd 2>/dev/null | awk '{print \$NF}'" 2>/dev/null || echo "")
            
            # 如果进程工作目录包含目标DORIS_HOME,则确认是目标版本
            if echo "$process_cwd" | grep -q "$target_doris_home"; then
                echo "RUNNING"
                return 0
            else
                # PID文件存在但进程工作目录不匹配,可能是其他版本的进程
                echo "STOPPED"
                return 1
            fi
        else
            # PID文件存在但进程不存在,可能是异常退出
            echo "STOPPED"
            return 1
        fi
    else
        # 没有PID文件,检查是否有匹配当前DORIS_HOME的进程
        local process_pattern
        case $service_type in
            fe)
                process_pattern="DorisFE"
                ;;
            be)
                process_pattern="DorisBe"
                ;;
            *)
                process_pattern=""
                ;;
        esac
        
        if [ -n "$process_pattern" ]; then
            # 查找匹配目标DORIS_HOME的进程
            local matching_pid
            matching_pid=$(ssh "$server" "
                for pid in \$(pgrep -f '$process_pattern' 2>/dev/null); do
                    if [ -d /proc/\$pid ]; then
                        cwd=\$(readlink /proc/\$pid/cwd 2>/dev/null)
                        if echo \"\$cwd\" | grep -q '$target_doris_home'; then
                            echo \$pid
                            break
                        fi
                    fi
                done
            " 2>/dev/null || echo "")
            
            if [ -n "$matching_pid" ]; then
                # 有匹配当前DORIS_HOME的进程但无PID文件,状态异常
                echo "STALE"
                return 3
            else
                echo "STOPPED"
                return 1
            fi
        else
            echo "STOPPED"
            return 1
        fi
    fi
}

# 获取当前版本的服务状态(兼容旧函数)
get_service_status() {
    get_version_service_status "$1" "$2" "$DORIS_HOME"
}

# 清理stale状态的服务(只清理指定版本的)
cleanup_stale_service() {
    local server=$1
    local service_type=$2
    local doris_home="$3"
    
    log_warn "清理 $server 上的 $service_type 残留进程 (版本: $doris_home)..."
    
    # 获取当前版本的PID文件
    local pid_file
    pid_file=$(get_pid_file "$doris_home" "$service_type")
    
    # 先清理PID文件
    ssh "$server" "rm -f '$pid_file' 2>/dev/null || true"
    
    # 查找并终止匹配指定DORIS_HOME的进程
    local process_pattern
    case $service_type in
        fe)
            process_pattern="DorisFE"
            ;;
        be)
            process_pattern="DorisBe"
            ;;
    esac
    
    if [ -n "$process_pattern" ]; then
        # 只终止工作目录匹配指定DORIS_HOME的进程
        ssh "$server" "
            for pid in \$(pgrep -f '$process_pattern' 2>/dev/null); do
                if [ -d /proc/\$pid ]; then
                    cwd=\$(readlink /proc/\$pid/cwd 2>/dev/null)
                    if echo \"\$cwd\" | grep -q '$doris_home'; then
                        kill -9 \$pid 2>/dev/null || true
                        echo '已终止进程: ' \$pid
                    fi
                fi
            done
        " 2>/dev/null || true
    fi
    
    sleep 1
    log_success "已清理 $server 上的 $service_type 残留进程"
}

# 获取所有正在运行的Doris版本信息
get_all_running_versions() {
    local server=$1
    local service_type=$2
    
    local process_pattern
    case $service_type in
        fe)
            process_pattern="DorisFE"
            ;;
        be)
            process_pattern="DorisBe"
            ;;
        *)
            echo ""
            return
            ;;
    esac
    
    ssh "$server" "
        for pid in \$(pgrep -f '$process_pattern' 2>/dev/null); do
            if [ -d /proc/\$pid ]; then
                cwd=\$(readlink /proc/\$pid/cwd 2>/dev/null)
                if [ -n \"\$cwd\" ]; then
                    echo \"PID: \$pid, 路径: \$cwd\"
                fi
            fi
        done
    " 2>/dev/null || echo "无进程运行"
}

# 显示所有Doris版本的状态
show_all_versions_status() {
    local server=$1
    
    echo "========================================"
    echo "      $server 上的所有Doris版本状态"
    echo "========================================"
    echo "检查时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "----------------------------------------"
    
    # 检查FE服务
    echo "FE 服务状态:"
    local found_fe=0
    for version in "${KNOWN_VERSIONS[@]}"; do
        local fe_path="$version/fe/bin/start_fe.sh"
        if ssh "$server" "[ -f '$fe_path' ]" 2>/dev/null; then
            found_fe=1
            local status
            status=$(get_version_service_status "$server" "fe" "$version")
            if [ "$version" = "$DORIS_HOME" ]; then
                echo -e "  ${CYAN}[当前管理]${NC} $version: $status"
            else
                echo "  $version: $status"
            fi
        fi
    done
    if [ $found_fe -eq 0 ]; then
        echo "  未发现FE服务安装"
    fi
    
    echo ""
    echo "BE 服务状态:"
    local found_be=0
    for version in "${KNOWN_VERSIONS[@]}"; do
        local be_path="$version/be/bin/start_be.sh"
        if ssh "$server" "[ -f '$be_path' ]" 2>/dev/null; then
            found_be=1
            local status
            status=$(get_version_service_status "$server" "be" "$version")
            if [ "$version" = "$DORIS_HOME" ]; then
                echo -e "  ${CYAN}[当前管理]${NC} $version: $status"
            else
                echo "  $version: $status"
            fi
        fi
    done
    if [ $found_be -eq 0 ]; then
        echo "  未发现BE服务安装"
    fi
    
    echo "----------------------------------------"
    echo "正在运行的Doris进程:"
    echo "FE进程:"
    get_all_running_versions "$server" "fe"
    echo ""
    echo "BE进程:"
    get_all_running_versions "$server" "be"
    echo "========================================"
}

# 启动指定版本的服务
start_version_service() {
    local server=$1
    local service_type=$2
    local doris_home="$3"
    
    log_info "在 $server 上启动 $service_type 服务 (版本: $doris_home)..."
    
    if ! check_service_installed "$server" "$service_type" "$doris_home"; then
        return 1
    fi
    
    # 获取当前状态
    local current_status
    current_status=$(get_version_service_status "$server" "$service_type" "$doris_home")
    
    case $current_status in
        RUNNING)
            log_warn "$server 上的 $service_type 服务已经在运行中 (版本: $doris_home)"
            return 0
            ;;
        STALE)
            log_warn "$server 上的 $service_type 服务处于异常状态,正在清理..."
            cleanup_stale_service "$server" "$service_type" "$doris_home"
            ;;
        STOPPED)
            # 正常停止状态,可以启动
            ;;
        *)
            log_error "$server 上的 $service_type 服务状态未知: $current_status (版本: $doris_home)"
            return 1
            ;;
    esac
    
    # 启动服务
    case $service_type in
        fe)
            safe_exec "ssh -o ConnectTimeout=$TIMEOUT '$server' \"cd '$doris_home/fe' && nohup ./bin/start_fe.sh --daemon > /dev/null 2>&1 & sleep 2\"" \
                "启动 $server 上的 FE 服务 (版本: $doris_home)"
            ;;
        be)
            safe_exec "ssh -o ConnectTimeout=$TIMEOUT '$server' \"cd '$doris_home/be' && nohup ./bin/start_be.sh --daemon > /dev/null 2>&1 & sleep 2\"" \
                "启动 $server 上的 BE 服务 (版本: $doris_home)"
            ;;
    esac
    
    if [ $? -eq 0 ]; then
        # 等待服务完全启动
        local retry_count=0
        while [ $retry_count -lt $MAX_RETRIES ]; do
            sleep $CHECK_INTERVAL
            if get_version_service_status "$server" "$service_type" "$doris_home" > /dev/null; then
                log_success "$server 上的 $service_type 服务启动成功 (版本: $doris_home)"
                return 0
            fi
            retry_count=$((retry_count + 1))
        done
        
        log_warn "$server 上的 $service_type 服务启动可能存在问题,请检查日志"
        return 1
    fi
    
    return 1
}

# 启动当前版本的服务(兼容旧函数)
start_service() {
    start_version_service "$1" "$2" "$DORIS_HOME"
}

# 停止指定版本的服务
stop_version_service() {
    local server=$1
    local service_type=$2
    local doris_home="$3"
    
    log_info "在 $server 上停止 $service_type 服务 (版本: $doris_home)..."
    
    if ! check_service_installed "$server" "$service_type" "$doris_home"; then
        return 1
    fi
    
    # 获取当前状态
    local current_status
    current_status=$(get_version_service_status "$server" "$service_type" "$doris_home")
    
    case $current_status in
        STOPPED)
            log_warn "$server 上的 $service_type 服务已经停止 (版本: $doris_home)"
            return 0
            ;;
        STALE)
            log_warn "$server 上的 $service_type 服务处于异常状态,使用强制清理..."
            cleanup_stale_service "$server" "$service_type" "$doris_home"
            return 0
            ;;
        RUNNING)
            # 正常停止操作
            ;;
        *)
            log_error "$server 上的 $service_type 服务状态未知: $current_status (版本: $doris_home)"
            return 1
            ;;
    esac
    
    # 停止服务
    case $service_type in
        fe)
            safe_exec "ssh -o ConnectTimeout=$TIMEOUT '$server' \"cd '$doris_home/fe' && ./bin/stop_fe.sh\"" \
                "停止 $server 上的 FE 服务 (版本: $doris_home)"
            ;;
        be)
            safe_exec "ssh -o ConnectTimeout=$TIMEOUT '$server' \"cd '$doris_home/be' && ./bin/stop_be.sh\"" \
                "停止 $server 上的 BE 服务 (版本: $doris_home)"
            ;;
    esac
    
    if [ $? -eq 0 ]; then
        # 等待服务完全停止
        local retry_count=0
        while [ $retry_count -lt $MAX_RETRIES ]; do
            sleep $CHECK_INTERVAL
            if ! get_version_service_status "$server" "$service_type" "$doris_home" > /dev/null; then
                log_success "$server 上的 $service_type 服务停止成功 (版本: $doris_home)"
                return 0
            fi
            retry_count=$((retry_count + 1))
        done
        
        log_warn "$server 上的 $service_type 服务停止可能存在问题,尝试强制停止..."
        cleanup_stale_service "$server" "$service_type" "$doris_home"
        return 0
    fi
    
    return 1
}

# 停止当前版本的服务(兼容旧函数)
stop_service() {
    stop_version_service "$1" "$2" "$DORIS_HOME"
}

# 重启指定版本的服务
restart_version_service() {
    local server=$1
    local service_type=$2
    local doris_home="$3"
    
    log_info "在 $server 上重启 $service_type 服务 (版本: $doris_home)..."
    
    # 停止服务
    if stop_version_service "$server" "$service_type" "$doris_home"; then
        # 等待一下再启动
        sleep 2
        # 启动服务
        start_version_service "$server" "$service_type" "$doris_home"
    else
        log_error "$server 上的 $service_type 服务停止失败,跳过重启"
        return 1
    fi
}

# 重启当前版本的服务(兼容旧函数)
restart_service() {
    restart_version_service "$1" "$2" "$DORIS_HOME"
}

# 显示指定版本的服务状态
show_version_status() {
    local server=$1
    local service_type=$2
    local doris_home="$3"
    
    if check_service_installed "$server" "$service_type" "$doris_home"; then
        local status
        status=$(get_version_service_status "$server" "$service_type" "$doris_home")
        
        case $status in
            RUNNING)
                echo -e "${GREEN}✓${NC} $server 上的 $service_type 服务: ${GREEN}运行中${NC} (版本: $doris_home)"
                ;;
            STOPPED)
                echo -e "${RED}✗${NC} $server 上的 $service_type 服务: ${RED}已停止${NC} (版本: $doris_home)"
                ;;
            STALE)
                echo -e "${YELLOW}⚠${NC} $server 上的 $service_type 服务: ${YELLOW}异常状态(需清理)${NC} (版本: $doris_home)"
                ;;
            *)
                echo -e "${YELLOW}?${NC} $server 上的 $service_type 服务: ${YELLOW}状态未知${NC} (版本: $doris_home)"
                ;;
        esac
    else
        echo -e "${YELLOW}?${NC} $server 上的 $service_type 服务: ${YELLOW}未安装${NC} (版本: $doris_home)"
    fi
}

# 显示当前版本的服务状态(兼容旧函数)
show_status() {
    show_version_status "$1" "$2" "$DORIS_HOME"
}

# 显示所有服务器状态
show_all_status() {
    local servers
    # 如果有参数传入,使用传入的服务器列表,否则使用默认列表
    if [ $# -eq 0 ]; then
        servers=("${DEFAULT_SERVERS[@]}")
    else
        servers=("$@")
    fi
    
    echo "========================================"
    echo "          Doris 集群状态报告"
    echo "========================================"
    echo "检查时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "当前脚本管理的版本: $DORIS_HOME"
    echo "服务器列表: ${servers[*]}"
    echo "----------------------------------------"
    
    for server in "${servers[@]}"; do
        echo "服务器: $server"
        echo "  FE 服务状态: $(get_service_status "$server" "fe")"
        echo "  BE 服务状态: $(get_service_status "$server" "be")"
        echo "----------------------------------------"
    done
}

# 显示所有版本的状态
show_versions() {
    local servers
    if [ $# -eq 0 ]; then
        servers=("${DEFAULT_SERVERS[@]}")
    else
        servers=("$@")
    fi
    
    for server in "${servers[@]}"; do
        show_all_versions_status "$server"
        echo ""
    done
}

# 跨版本管理函数
manage_all_versions() {
    local operation=$1
    local service_type=$2
    shift 2
    local servers=("$@")
    
    # 如果没有指定服务器,使用默认列表
    if [ ${#servers[@]} -eq 0 ]; then
        servers=("${DEFAULT_SERVERS[@]}")
    fi
    
    log_info "开始在所有版本上执行 $operation $service_type 操作..."
    log_info "服务器列表: ${servers[*]}"
    
    for version in "${KNOWN_VERSIONS[@]}"; do
        echo "========================================"
        echo "处理版本: $version"
        echo "========================================"
        
        for server in "${servers[@]}"; do
            # 检查该版本在服务器上是否已安装
            case $service_type in
                fe)
                    if ! ssh "$server" "[ -f '$version/fe/bin/start_fe.sh' ]" 2>/dev/null; then
                        log_warn "服务器 $server 上未安装 $version 的 FE 服务,跳过"
                        continue
                    fi
                    ;;
                be)
                    if ! ssh "$server" "[ -f '$version/be/bin/start_be.sh' ]" 2>/dev/null; then
                        log_warn "服务器 $server 上未安装 $version 的 BE 服务,跳过"
                        continue
                    fi
                    ;;
                all)
                    # 检查FE和BE
                    local has_fe=0
                    local has_be=0
                    if ssh "$server" "[ -f '$version/fe/bin/start_fe.sh' ]" 2>/dev/null; then
                        has_fe=1
                    fi
                    if ssh "$server" "[ -f '$version/be/bin/start_be.sh' ]" 2>/dev/null; then
                        has_be=1
                    fi
                    if [ $has_fe -eq 0 ] && [ $has_be -eq 0 ]; then
                        log_warn "服务器 $server 上未安装 $version 的服务,跳过"
                        continue
                    fi
                    ;;
            esac
            
            case $operation in
                start)
                    case $service_type in
                        all)
                            safe_exec "start_version_service '$server' 'fe' '$version'" "启动 $server 上的 FE 服务 (版本: $version)"
                            sleep 2
                            safe_exec "start_version_service '$server' 'be' '$version'" "启动 $server 上的 BE 服务 (版本: $version)"
                            ;;
                        fe|be)
                            safe_exec "start_version_service '$server' '$service_type' '$version'" "启动 $server 上的 $service_type 服务 (版本: $version)"
                            ;;
                    esac
                    ;;
                stop)
                    case $service_type in
                        all)
                            safe_exec "stop_version_service '$server' 'be' '$version'" "停止 $server 上的 BE 服务 (版本: $version)"
                            sleep 2
                            safe_exec "stop_version_service '$server' 'fe' '$version'" "停止 $server 上的 FE 服务 (版本: $version)"
                            ;;
                        fe|be)
                            safe_exec "stop_version_service '$server' '$service_type' '$version'" "停止 $server 上的 $service_type 服务 (版本: $version)"
                            ;;
                    esac
                    ;;
                restart)
                    case $service_type in
                        all)
                            safe_exec "restart_version_service '$server' 'fe' '$version'" "重启 $server 上的 FE 服务 (版本: $version)"
                            sleep 2
                            safe_exec "restart_version_service '$server' 'be' '$version'" "重启 $server 上的 BE 服务 (版本: $version)"
                            ;;
                        fe|be)
                            safe_exec "restart_version_service '$server' '$service_type' '$version'" "重启 $server 上的 $service_type 服务 (版本: $version)"
                            ;;
                    esac
                    ;;
                status)
                    case $service_type in
                        all)
                            show_version_status "$server" "fe" "$version"
                            show_version_status "$server" "be" "$version"
                            ;;
                        fe|be)
                            show_version_status "$server" "$service_type" "$version"
                            ;;
                    esac
                    ;;
            esac
        done
        echo ""
    done
    
    log_success "所有版本上的 $operation $service_type 操作完成"
}

# 显示帮助信息
show_help() {
    cat << EOF
Doris 集群管理脚本(支持多版本同时运行)

用法: $0 [命令] [服务类型] [服务器列表...]
      $0 all-versions [命令] [服务类型] [服务器列表...]

命令:
  start          启动当前版本的服务
  stop           停止当前版本的服务
  restart        重启当前版本的服务
  status         查看当前版本的服务状态
  versions       查看所有版本在所有服务器上的状态
  all-versions   在所有版本上执行命令(启动/停止/重启/状态查看)
  help           显示此帮助信息

服务类型:
  all            所有服务 (FE 和 BE)
  fe             Frontend 服务
  be             Backend 服务

服务器列表 (可选):
  如果不指定,使用默认服务器列表: ${DEFAULT_SERVERS[*]}

状态说明:
  ✓ 运行中    - 服务正常运行
  ✗ 已停止   - 服务已停止
  ⚠ 异常状态 - 服务处于异常状态,需要清理
  ? 状态未知 - 无法确定服务状态

重要提示:
  当前脚本管理的版本: $DORIS_HOME
  支持同时运行多个版本,只需确保端口和数据目录隔离

示例:
  # 管理当前版本
  $0 start all                           # 在所有服务器上启动当前版本的所有服务
  $0 stop fe master01 master02           # 在指定服务器上停止当前版本的FE服务
  $0 restart be                          # 在所有服务器上重启当前版本的BE服务
  $0 status all                          # 查看当前版本在所有服务器上的状态
  
  # 管理所有版本
  $0 all-versions start all              # 在所有服务器上启动所有版本的所有服务
  $0 all-versions stop fe                # 在所有服务器上停止所有版本的FE服务
  $0 all-versions status be              # 查看所有版本的BE服务状态
  
  $0 versions                            # 查看所有版本在所有服务器上的状态
  $0 status                              # 查看当前版本在所有服务器上的状态(默认)

环境变量配置:
  DEFAULT_SERVERS: 默认服务器列表
  DORIS_HOME:      当前脚本管理的Doris版本路径
  KNOWN_VERSIONS:  所有已知的Doris版本路径

EOF
}

# 执行批量操作
execute_batch_operation() {
    local operation=$1
    local service_type=$2
    shift 2
    local servers=("$@")
    
    # 如果没有指定服务器,使用默认列表
    if [ ${#servers[@]} -eq 0 ]; then
        servers=("${DEFAULT_SERVERS[@]}")
    fi
    
    log_info "开始执行 $operation $service_type 操作..."
    log_info "版本: $DORIS_HOME"
    log_info "服务器列表: ${servers[*]}"
    
    # 先检查所有服务器的连通性
    local failed_servers=()
    for server in "${servers[@]}"; do
        if ! check_server_connectivity "$server"; then
            failed_servers+=("$server")
        fi
    done
    
    if [ ${#failed_servers[@]} -gt 0 ]; then
        log_error "以下服务器无法连接: ${failed_servers[*]}"
        return 1
    fi
    
    # 执行操作
    case "$service_type" in
        all)
            # 注意:先启动FE,再启动BE;先停止BE,再停止FE
            if [ "$operation" = "start" ]; then
                # 先启动所有FE
                for server in "${servers[@]}"; do
                    safe_exec "start_service '$server' 'fe'" "启动 $server 上的 FE 服务"
                done
                # 等待FE启动完成
                sleep 5
                # 再启动所有BE
                for server in "${servers[@]}"; do
                    safe_exec "start_service '$server' 'be'" "启动 $server 上的 BE 服务"
                done
            elif [ "$operation" = "stop" ]; then
                # 先停止所有BE
                for server in "${servers[@]}"; do
                    safe_exec "stop_service '$server' 'be'" "停止 $server 上的 BE 服务"
                done
                # 等待BE停止完成
                sleep 3
                # 再停止所有FE
                for server in "${servers[@]}"; do
                    safe_exec "stop_service '$server' 'fe'" "停止 $server 上的 FE 服务"
                done
            elif [ "$operation" = "restart" ]; then
                # 重启所有服务
                for server in "${servers[@]}"; do
                    safe_exec "restart_service '$server' 'fe'" "重启 $server 上的 FE 服务"
                done
                sleep 5
                for server in "${servers[@]}"; do
                    safe_exec "restart_service '$server' 'be'" "重启 $server 上的 BE 服务"
                done
            elif [ "$operation" = "status" ]; then
                show_all_status "${servers[@]}"
            fi
            ;;
        fe|be)
            for server in "${servers[@]}"; do
                case "$operation" in
                    start) safe_exec "start_service '$server' '$service_type'" "启动 $server 上的 $service_type 服务" ;;
                    stop) safe_exec "stop_service '$server' '$service_type'" "停止 $server 上的 $service_type 服务" ;;
                    restart) safe_exec "restart_service '$server' '$service_type'" "重启 $server 上的 $service_type 服务" ;;
                    status) show_status "$server" "$service_type" ;;
                esac
                echo
            done
            ;;
    esac
    
    log_success "$operation $service_type 操作完成 (版本: $DORIS_HOME)"
}

# 主函数
main() {
    if [ $# -lt 1 ]; then
        show_all_status
        exit 0
    fi
    
    local command=$1
    local service_type="all"
    local servers=()
    
    case "$command" in
        help|--help|-h)
            show_help
            exit 0
            ;;
        versions)
            shift
            servers=("$@")
            show_versions "${servers[@]}"
            exit 0
            ;;
        all-versions)
            if [ $# -lt 2 ]; then
                log_error "all-versions 命令需要指定操作"
                show_help
                exit 1
            fi
            local operation="$2"
            if [ $# -ge 3 ]; then
                service_type="$3"
                if [ $# -ge 4 ]; then
                    shift 3
                    servers=("$@")
                fi
            fi
            
            # 验证操作类型
            if [[ ! "$operation" =~ ^(start|stop|restart|status)$ ]]; then
                log_error "无效的操作: $operation"
                show_help
                exit 1
            fi
            
            # 验证服务类型
            if [[ ! "$service_type" =~ ^(all|fe|be)$ ]]; then
                log_error "无效的服务类型: $service_type"
                show_help
                exit 1
            fi
            
            manage_all_versions "$operation" "$service_type" "${servers[@]}"
            exit 0
            ;;
        start|stop|restart|status)
            if [ $# -ge 2 ]; then
                service_type=$2
                # 获取服务器列表(剩余的参数)
                if [ $# -ge 3 ]; then
                    shift 2
                    servers=("$@")
                fi
            fi
            ;;
        *)
            log_error "无效的命令: $command"
            show_help
            exit 1
            ;;
    esac
    
    # 验证服务类型
    if [[ ! "$service_type" =~ ^(all|fe|be)$ ]]; then
        log_error "无效的服务类型: $service_type"
        show_help
        exit 1
    fi
    
    # 执行操作
    case "$command" in
        start|stop|restart|status)
            execute_batch_operation "$command" "$service_type" "${servers[@]}"
            ;;
    esac
}

# 异常处理
trap 'log_error "脚本执行异常,退出码: $?"; exit 1' ERR
trap 'log_error "脚本被用户中断"; exit 1' INT TERM

# 运行主函数
main "$@"
