# 终局结算 - 数据库表结构

## endgame_shelter_event（避难所终局事件表）

| 字段名称 | 数据类型 | 约束条件 | 字段说明 |
|---------|---------|---------|---------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT, NOT NULL | 主键ID |
| title | VARCHAR(100) | NOT NULL | 事件标题 |
| description | TEXT | NOT NULL | 事件描述内容 |
| category | VARCHAR(50) | 可为NULL | 事件分类标签 |
| sort_order | INT | 可为NULL | 排序权重 |
| created_at | DATETIME | 可为NULL | 创建时间 |
| updated_at | DATETIME | 可为NULL | 更新时间 |

## endgame_ark_event（方舟终局事件表）

| 字段名称 | 数据类型 | 约束条件 | 字段说明 |
|---------|---------|---------|---------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT, NOT NULL | 主键ID |
| title | VARCHAR(100) | NOT NULL | 事件标题 |
| description | TEXT | NOT NULL | 事件描述内容 |
| category | VARCHAR(50) | 可为NULL | 事件分类标签 |
| sort_order | INT | 可为NULL | 排序权重 |
| created_at | DATETIME | 可为NULL | 创建时间 |
| updated_at | DATETIME | 可为NULL | 更新时间 |
