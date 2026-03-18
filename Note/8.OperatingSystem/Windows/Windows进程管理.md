# Windows 进程管理

## 查找进程

### 根据进程名查找
```bash
tasklist | findstr <进程名>
```

示例:
```bash
tasklist | findstr PPOCRLabel
```

### 查看所有进程
```bash
tasklist
```

## 终止进程

### 根据进程名终止
```bash
taskkill /f /im <进程名>
```

示例:
```bash
taskkill /f /im PPOCRLabel.exe
```

### 根据PID终止
```bash
taskkill /f /pid <PID>
```

示例:
```bash
# 先查找进程获取PID
tasklist | findstr PPOCRLabel
# 根据显示的PID终止进程
taskkill /f /pid 12345
```

## 参数说明

- `/f`: 强制终止进程
- `/im`: 指定进程名称(Image Name)
- `/pid`: 指定进程ID

## 常见应用场景

### 终止无响应的程序
当程序无响应时,可以使用任务管理器或命令行强制终止:
```bash
taskkill /f /im notepad.exe
```

### 批量终止同名进程
如果有多个同名进程,使用 `/im` 参数可以一次性全部终止:
```bash
taskkill /f /im chrome.exe
```

### 终止特定用户的进程
```bash
taskkill /f /im <进程名> /u <用户名>
```
