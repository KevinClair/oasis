# 公告 HTML 内容支持说明

## ✅ 功能概述

首页公告内容现在支持 HTML 富文本渲染，可以在公告内容中使用各种 HTML 标签来美化显示效果。

## 🎨 支持的 HTML 标签

### 文本格式标签

| 标签 | 说明 | 示例 |
|------|------|------|
| `<b>` | 粗体 | `<b>重要内容</b>` |
| `<strong>` | 强调（粗体） | `<strong>强调文本</strong>` |
| `<i>` | 斜体 | `<i>斜体文本</i>` |
| `<em>` | 强调（斜体） | `<em>强调文本</em>` |
| `<u>` | 下划线 | `<u>下划线文本</u>` |
| `<s>` | 删除线 | `<s>删除的文本</s>` |
| `<mark>` | 高亮 | `<mark>高亮文本</mark>` |

### 布局标签

| 标签 | 说明 | 示例 |
|------|------|------|
| `<br>` | 换行 | `第一行<br>第二行` |
| `<p>` | 段落 | `<p>这是一个段落</p>` |
| `<div>` | 块级容器 | `<div>块级内容</div>` |
| `<span>` | 行内容器 | `<span>行内内容</span>` |
| `<hr>` | 分隔线 | `<hr>` |

### 列表标签

| 标签 | 说明 | 示例 |
|------|------|------|
| `<ul>` + `<li>` | 无序列表 | `<ul><li>项目1</li><li>项目2</li></ul>` |
| `<ol>` + `<li>` | 有序列表 | `<ol><li>步骤1</li><li>步骤2</li></ol>` |

### 链接标签

| 标签 | 说明 | 示例 |
|------|------|------|
| `<a>` | 超链接 | `<a href="https://example.com">点击查看</a>` |

### 样式标签

可以使用 `style` 属性自定义样式：

```html
<span style="color: red;">红色文字</span>
<span style="font-size: 18px;">大字号</span>
<span style="background-color: yellow;">黄色背景</span>
```

## 📝 示例公告内容

### 示例 1：系统维护通知（警告类型）

```html
系统将于<b>今晚 22:00</b> 进行维护<br/>
预计耗时：<span style="color: red; font-weight: bold;">2小时</span><br/>
<br/>
<b>影响范围：</b><br/>
<ul>
  <li>所有在线用户将被强制下线</li>
  <li>数据查询功能暂时不可用</li>
  <li>文件上传功能暂停</li>
</ul>
<br/>
请提前保存您的工作内容！<br/>
<a href="https://example.com/maintenance" target="_blank">查看维护详情 →</a>
```

### 示例 2：新功能发布（普通类型）

```html
<p><b>🎉 新功能上线啦！</b></p>
<p>我们很高兴地宣布以下新功能已经正式上线：</p>
<ol>
  <li><b>公告管理</b>：支持发布系统公告</li>
  <li><b>富文本编辑</b>：公告内容支持HTML格式</li>
  <li><b>主题色适配</b>：根据公告类型自动调整颜色</li>
</ol>
<p>欢迎大家体验使用！<br/>
<a href="https://example.com/docs" target="_blank">查看使用文档</a></p>
```

### 示例 3：安全警告（重要通知类型）

```html
<p style="font-size: 16px; font-weight: bold;">⚠️ 安全警告</p>
<p>我们检测到系统存在以下安全风险：</p>
<ul>
  <li style="color: red;"><b>弱密码账户</b>：请立即修改密码</li>
  <li style="color: orange;">未绑定邮箱：建议尽快绑定</li>
</ul>
<hr>
<p><b>请立即采取以下措施：</b></p>
<ol>
  <li>修改密码（必须包含大小写字母、数字、特殊字符）</li>
  <li>绑定安全邮箱</li>
  <li>启用双因素认证</li>
</ol>
<p><a href="https://example.com/security" style="color: red; font-weight: bold;">立即前往安全中心 →</a></p>
```

### 示例 4：简单文本（带格式）

```html
欢迎使用本系统！<br/>
<br/>
系统当前版本：<b>v2.0.0</b><br/>
更新时间：<i>2026-02-13</i><br/>
<br/>
如有问题，请联系：<a href="mailto:support@example.com">support@example.com</a>
```

## ⚠️ 安全注意事项

### 1. XSS 防护

虽然使用 `v-html` 可以渲染 HTML 内容，但需要注意以下安全问题：

- **不要直接渲染用户输入的内容**（本系统只有管理员可以创建公告，相对安全）
- **避免使用 `<script>` 标签**
- **避免使用 `<iframe>` 标签**
- **避免使用事件处理器**（如 `onclick`、`onerror` 等）

### 2. 建议的安全实践

如果需要更高的安全性，可以考虑：

1. **使用 Markdown 替代 HTML**
   - 更安全
   - 更易于编辑
   - 可以使用 markdown 解析库

2. **使用 HTML 净化库**
   ```bash
   npm install dompurify
   ```
   ```typescript
   import DOMPurify from 'dompurify';
   
   const sanitizedContent = DOMPurify.sanitize(latestAnnouncement.value.content);
   ```

3. **限制允许的 HTML 标签**
   - 只允许安全的标签（如 `<b>`, `<i>`, `<br>`, `<p>`, `<a>` 等）
   - 禁止 `<script>`, `<iframe>`, `<object>` 等危险标签

## 🎯 最佳实践

### 1. 内容编辑建议

- 使用在线 HTML 编辑器预览效果（如 CodePen、JSFiddle）
- 保持 HTML 结构简单清晰
- 适当使用换行和空格，提高可读性
- 避免过度使用样式，保持统一性

### 2. 样式规范

- 使用内联样式（`style` 属性）
- 颜色建议：
  - 普通文本：黑色或深灰色
  - 强调文本：红色、橙色
  - 链接：蓝色
- 字号建议：14px - 18px

### 3. 响应式考虑

- 避免固定宽度（使用百分比或不设置）
- 图片使用 `max-width: 100%`
- 长文本自动换行

## 📱 移动端适配

公告在移动端会自动适配，但建议：

- 避免使用过长的单词或URL（会导致横向滚动）
- 使用 `<br>` 适当分行
- 避免复杂的嵌套结构

## 🧪 测试公告模板

可���在公告管理中创建以下测试公告：

```html
<b>测试公告</b><br/>
这是一条<span style="color: red;">测试公告</span>，包含以下格式：<br/>
<ul>
  <li><b>粗体文字</b></li>
  <li><i>斜体文字</i></li>
  <li><u>下划线文字</u></li>
</ul>
<a href="https://example.com">点击查看详情</a>
```

## ✅ 验证清单

发布公告前，请确认：

- [ ] HTML 标签正确闭合
- [ ] 链接地址正确有效
- [ ] 没有使用危险标签（`<script>`, `<iframe>` 等）
- [ ] 在不同浏览器中测试显示效果
- [ ] 在移动端测试显示效果
- [ ] 文本内容清晰易读

---

**现在您可以在公告内容中使用 HTML 标签来创建更丰富的公告内容了！** 🎨

