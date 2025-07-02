## HexoTools
介绍：一个用于处理hexo博客的工具包。

## 🧩 主要功能

- **🔍 图片检查**
  - 检查 Markdown 文件中引用的本地图片是否存在。
  - 检查远程图片链接是否有效。

- **🔧 图片修复**
  - 自动从指定图库中查找缺失的图片并复制到对应目录。
  - 支持下载网络图片到本地。

- **🔁 图片重命名**
  - 使用 MD5 哈希值对图片进行重命名，避免重复文件名冲突。

- **🗑️ Markdown 文件去重**
  - 检测并删除内容完全相同的 Markdown 文件，保留最优文件。

- **🕒 Hexo 时间更新**
  - 自动将 Hexo 文章的创建时间和修改时间同步到 Front Matter 中。

- **📂 友好交互**
  - 提供命令行菜单选择不同功能。
  - 支持清屏操作，保持界面整洁。

## 🛠 技术栈

- Java 17+
- CommonMark-Java
- Apache Commons IO

## 📦 安装与使用

1. 下载最新 [Release](https://github.com/yourusername/hexo-clean-markdown-pic/releases/latest)
2. 解压后运行：
   ```bash
   java -jar hexo-clean-markdown-pic.jar
   ```
3. 按照提示输入 `_posts` 目录路径，并选择所需功能。

## ✅ 适用人群

- Hexo 博客作者
- 需要维护大量 Markdown 文档的用户
- 对文档结构和图片管理有高要求的开发者

## 📄 License

本项目采用 MIT 许可证，请查看 [LICENSE](LICENSE) 文件了解更多详情。