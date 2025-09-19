# Node 使用与环境管理（进阶）

本文档聚焦 Node 的环境变量管理、跨平台设置、`.env` 与 `dotenv` 的使用、npm 缓存与安装差异、安全与调试实用技巧，适合希望把开发/构建/部署流程做稳健化的工程师。

---

## 1. NODE_ENV：语义与实践

NODE_ENV 不是 Node.js 强制定义的“行为开关”，而是社区约定的环境标识，用来区分 development / production / test 等环境。库与框架（例如 Express、React、Webpack）通常会根据该变量选择不同的优化或行为。

语义建议：
- development：本地开发，开启详细日志、热重载、未压缩构建等。
- production：生产环境，开启性能优化、关闭调试日志、启用缓存等。
- test：测试环境，控制外部依赖、使用测试配置。

设置示例（按平台）：

- Windows cmd.exe（临时在当前命令行窗口）

```bash
#查看是否存在环境变量，如果有则会输出，反之为空
set NODE_ENV
#删除环境变量
set NODE_ENV=
#临时设置NODE_ENV为production
set NODE_ENV=production

# 运行程序
node server.js
```

- PowerShell（临时）

```powershell
$env:NODE_ENV = "production"; node server.js
```

- macOS / Linux（临时）

```bash
NODE_ENV=production node server.js
```

在 package.json 脚本中跨平台设置环境变量时建议使用 cross-env（见下）。直接在 Windows 上使用 `NODE_ENV=...` 会失败。

检测 NODE_ENV 的正确写法：在代码中通过 `process.env.NODE_ENV` 读取。注意它是字符串或 undefined。

示例：

```javascript
const env = process.env.NODE_ENV || 'development';
console.log('当前环境：', env);
```

边界与注意事项：
- 不要把敏感凭据放在环境变量外的源代码或版本库中；使用专门的机密管理工具或 CI/CD 的保密变量功能。
- 某些构建工具在构建时替换 process.env.NODE_ENV（如 webpack 的 DefinePlugin），确保构建时传入正确值。

---

## 2. 跨平台设置环境变量：cross-env 与 dotenv

1) cross-env：用于在 npm script 中统一设置环境变量，支持 Windows/macOS/Linux。

安装：

```bash
npm install --save-dev cross-env
```

示例 package.json 脚本：

```javascripton
"scripts": {
	"start": "cross-env NODE_ENV=production node server.js",
	"dev": "cross-env NODE_ENV=development nodemon server.js"
}
```

2) dotenv：用于从 `.env` 文件加载环境变量到 `process.env`。适合本地开发或非机密配置加载（机密请使用更安全的方案）。

安装：

```bash
npm install dotenv
```

使用：在程序入口提前加载：

```javascript
require('dotenv').config();
// 现在可以访问 process.env.MY_VAR
```

.env 文件示例（不要提交到 git）

```
PORT=3000
DB_HOST=localhost
API_KEY=xxxxx   # 机密不建议写入仓库
```

注意：dotenv 读取 `.env` 文件并将变量注入到运行时的 process.env，但不会在其它终端会话持久化这些变量。

推荐组合：在本地使用 `.env` + dotenv；在 CI/CD 或生产环境使用平台/容器提供的环境变量或机密管理服务。

---

## 3. npm 缓存：机制、命令与常见场景

npm 的缓存用于保存下载的包文件（tarballs）和一些元数据，目的是加速安装并减少重复下载。理解缓存行为有助于诊断安装失败或不一致问题。

常用命令：

- 清除缓存（新版 npm 建议用 verify，强制清除仅在必要时使用）

```bash
# 验证缓存一致性（推荐）
npm cache verify

# 强制清除缓存（会删除缓存目录，谨慎使用）
npm cache clean --force
```

- 列出缓存内容（较少用）

```bash
npm cache ls
```

缓存位置：
- 运行 `npm config get cache` 查看当前缓存路径。容器/CI 常常会挂载缓存目录以加速构建。

常见问题与实践：
- 安装失败时的第一步：先尝试 `npm cache verify`，再重试安装。只有在缓存损坏且 verify 无效时才使用 `npm cache clean --force`。
- 当你需要确保构建可复现，优先使用 `npm ci`（见下一节）；`npm install` 在 package-lock.json 与 node_modules 不一致时会修改 node_modules，但不会修改 lockfile（npm v7+ 行为更复杂）。
- 私有 registry 与缓存：若使用私有 npm registry（如 Verdaccio 或企业镜像），缓存与镜像配置会影响包解析与下载，遇到 404/401 等问题时先确认 registry 设置。

---

## 4. npm install vs npm ci vs pnpm/yarn 的注意点

- npm install：用于日常安装和开发，若 package-lock.json 与 package.json 有差异，npm 可能会更新 lockfile（取决于 npm 版本）。对开发友好但在 CI 中可能带来不可复现行为。
- npm ci：针对 CI 的安装命令，要求存在 package-lock.json，并在安装前删除 node_modules，然后严格按照 lockfile 安装。更快、更可预测，推荐在 CI/CD 中使用。

示例：

```bash
# 在 CI 中
npm ci --only=production
```

- pnpm / yarn：这些工具有各自的缓存和工作机制（pnpm 使用硬链接节省磁盘），在 monorepo 或大型项目中可以显著提高性能，切换工具时注意 lockfile 格式和团队协作的统一。

---

## 5. 安全与最佳实践

- 不要把凭据（密码、私钥、API Key）写进 `.env` 并推送到仓库。使用：
	- CI 的 secrets 功能（GitHub Actions Secrets、GitLab CI/CD Variables）
	- 云平台的 Secrets Manager（AWS Secrets Manager、Azure Key Vault 等）
	- HashiCorp Vault 等。

- 最小权限原则：短期 token、按需授权、定期轮换。

- 依赖安全：
	- 定期运行 `npm audit` 或使用 Snyk、Dependabot 提交依赖安全 PR。
	- 锁定依赖版本并使用 lockfile（package-lock.json 或 pnpm-lock.yaml）。

- 构建产物不要包含源代码中的敏感信息。对构建输出进行审计。

---

## 6. 常见问题与排查步骤

问题：`NODE_ENV=production npm run build` 在 Windows 上失败
- 原因：Windows cmd 不支持 `NODE_ENV=...` 这种写法。
- 解决：使用 cross-env：`npx cross-env NODE_ENV=production npm run build`。

问题：安装报错 `EACCES` 或权限错误
- 场景：全局安装或系统级目录权限问题
- 解决：避免使用全局安装作为修复，推荐使用 nvm/nvs 切换到用户级 Node，或修复 npm 全局目录权限（官方文档有说明）。

问题：npm 安装非常慢
- 检查网络与 registry：`npm config get registry`，考虑使用镜像源（比如内部镜像或淘宝源），或开启缓存代理。

问题：包版本不一致 / 构建在 CI 上失败但本地成功
- 排查：确保 CI 使用 `npm ci`，并且提交了 package-lock.json；检查 Node 与 npm 版本是否一致，推荐在 CI 中指定 Node 版本（例如使用 actions/setup-node）。

---

## 7. 进阶技巧与性能

- 使用 `--prefer-offline` 或缓存策略在 CI 中加速重试安装。
- 对于 monorepo，考虑使用 pnpm + workspace 或 yarn workspaces 来减少重复依赖。
- 在 Docker 镜像中，先拷贝 package.json 与 lockfile，执行 `npm ci` 再复制源代码，以充分利用缓存层。

示例 Dockerfile 片段：

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
CMD ["node", "dist/server.js"]
```

---

## 8. 常用命令速查表

- 查看缓存目录：

```bash
npm config get cache
```

- 验证缓存：

```bash
npm cache verify
```

- 强制清理缓存：

```bash
npm cache clean --force
```

- 安装（开发）：

```bash
npm install
```

- 在 CI 中安装（从 lockfile）：

```bash
npm ci
```

---

## 9. 参考资料

- Node/ENV 讨论与实践
- cross-env: https://www.npmjs.com/package/cross-env
- dotenv: https://www.npmjs.com/package/dotenv
- npm 文档（cache、ci、install）: https://docs.npmjs.com
- npm 权限修复: https://docs.npmjs.com/resolving-eacces-permissions-errors-when-installing-packages-globally


