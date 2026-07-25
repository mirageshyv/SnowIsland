# 游戏数据重置分析文档

## 1. 概述

本文档分析SnowIsland游戏系统中所有数据库表，明确哪些数据属于**游戏进程数据**（应在重置时清除），哪些属于**系统配置数据**（应保留），并分析数据间的依赖关系及安全机制建议。

---

## 2. 数据分类清单

### 2.1 可清除数据（游戏进程数据）

此类数据由玩家在游戏过程中产生，重置时应彻底清除以恢复至游戏初始状态。

| 表名 | 数据特征 | 说明 |
|------|----------|------|
| `player` | 玩家状态标记（受伤、过劳、死亡等） | 玩家角色数据，包含状态标记、阵营、职业等，游戏过程中会改变 |
| `player_action` | 玩家行动记录 | 记录玩家每日行动选择、结果等 |
| `player_daily_consumption` | 每日消耗记录 | 记录玩家每日食物/燃料消耗情况 |
| `player_exploration` | 探索记录 | 玩家探索地点的记录 |
| `player_items` | 玩家物品 | 玩家拥有的物品列表 |
| `player_npc_recognition` | NPC认知 | 玩家与NPC的互动认知记录 |
| `player_stealth` | 潜行记录 | 玩家潜行状态记录 |
| `game_state` | 游戏状态 | 当前天数、阶段、游戏是否结束等 |
| `game_day_settings` | 天数设置 | 游戏天数相关设置 |
| `game_activity_log` | 活动日志 | 游戏活动记录 |
| `ark_construction` | 方舟建造进度 | 当前木材、金属、发动机等建造进度 |
| `ark_construction_log` | 建造日志 | 方舟建造操作记录 |
| `ark_sail` | 航行记录 | 方舟航行相关数据 |
| `ark_voyage` | 航程记录 | 方舟航程数据 |
| `catastrophe_progress` | 天灾进度 | 天灾进度0-100 |
| `catastrophe_deck` | 天灾牌组状态 | 牌组的抽取/使用状态 |
| `drawn_cards` | 抽取记录 | 已抽取的天灾牌记录 |
| `selected_catastrophe` | 已选天灾牌 | 玩家选择的天灾牌 |
| `shelter_daily_labor` | 每日劳工 | 每日劳工名单及状态 |
| `shelter_labor_day` | 劳工天数 | 劳工结算记录 |
| `shelter_progress` | 避难所进度 | 避难所建造进度 |
| `shelter_stock` | 避难所库存 | 避难所物资库存 |
| `warehouse_ark` | 冒险者仓库 | 冒险者阵营仓库物资 |
| `warehouse_armory` | 镇武库 | 镇长厅武库物资 |
| `warehouse_dock` | 码头仓库 | 码头集换站仓库 |
| `warehouse_fuel` | 燃料仓库 | 燃料仓库物资 |
| `warehouse_general` | 普通仓库 | 矿场仓库等 |
| `warehouse_rebel` | 反叛者仓库 | 反叛者基地仓库 |
| `trade` | 交易记录 | 玩家间交易记录 |
| `trade_items` | 交易物品 | 交易涉及的物品 |
| `faction_action` | 阵营行动 | 阵营行动记录 |
| `night_action` | 夜间行动 | 夜间行动记录 |
| `quick_interaction` | 快速互动 | 快速互动记录 |
| `lore_player_grant` | 传说授予 | 玩家获得传说记录 |
| `clue_trigger_log` | 线索触发日志 | 线索触发记录 |
| `milestone_player_status` | 里程碑状态 | 玩家里程碑完成状态 |
| `location_governance` | 地点治理 | 地点控制权记录 |
| `npc_daily_dialogue_count` | NPC每日对话次数 | NPC对话次数限制 |
| `npc_daily_trade_count` | NPC每日交易次数 | NPC交易次数限制 |
| `npc_favor` | NPC好感度 | NPC对玩家的好感度（游戏进程中变化） |

### 2.2 需保留数据（系统配置数据）

此类数据为游戏基础配置，重置时应保持不变。

| 表名 | 数据特征 | 说明 |
|------|----------|------|
| `user` | 用户账户信息 | 登录用户名、密码、角色 |
| `item` | 物品定义 | 医疗包、手电筒、武器等物品配置 |
| `weapon` | 武器定义 | 手枪、猎枪等武器配置 |
| `ammo` | 弹药定义 | 手枪弹、猎枪弹等弹药配置 |
| `material` | 材料定义 | 木材、金属、燃料等材料配置 |
| `skill` | 技能定义 | 射击、格斗、医疗等技能配置 |
| `job` | 职业定义 | 镇长、渔民、猎户等职业配置 |
| `job_initial_items` | 职业初始物品 | 各职业初始物品配置 |
| `rule_book` | 规则书 | 游戏规则文档 |
| `island_event` | 探索事件 | 废弃猎人瞭望台、风暴后洞穴等事件配置 |
| `island_event_reward` | 事件奖励 | 事件奖励配置 |
| `special_clue` | 特殊线索 | 特殊线索配置 |
| `location` | 地点定义 | 警察局、镇长厅、码头等地点配置 |
| `location_facility` | 地点设施 | 燃料仓、发电机、渔船等设施配置 |
| `location_npc` | 地点NPC | 各地点的NPC配置 |
| `npc_dialogue` | NPC对话 | NPC对话内容配置 |
| `npc_favor_adjustment` | 好感度调整配置 | 好感度调整规则 |
| `npc_help_config` | NPC帮助配置 | NPC帮助规则 |
| `npc_trade_config` | NPC交易配置 | NPC交易规则 |
| `warehouse_config` | 仓库配置 | 仓库定义和配置 |
| `ark_config` | 方舟配置 | 方舟参数配置（载重、材料需求等） |
| `milestone` | 里程碑定义 | 游戏里程碑配置 |

---

## 3. 数据依赖关系分析

### 3.1 外键依赖关系图

```
user ──→ player (player_id)
player ──→ job (job_id), skill (skill_id)
player_items ──→ player (player_id), item (item_id), weapon (weapon_id), material (material_id)
player_action ──→ player (player_id)
player_daily_consumption ──→ player (player_id)

catastrophe_deck ──→ catastrophe_card (card_id)
drawn_cards ──→ catastrophe_deck (deck_id)
selected_catastrophe ──→ catastrophe_deck (deck_id), player (player_id)

shelter_daily_labor ──→ player (player_id)
shelter_labor_day ──→ shelter_daily_labor

ark_construction ──→ ark_config (隐式)

location_facility ──→ location (location_id)
location_npc ──→ location (location_id)

trade_items ──→ trade (trade_id)
trade ──→ player (双方玩家)

npc_dialogue ──→ location (location_id)
npc_favor ──→ player (player_id), location (location_id)
```

### 3.2 删除顺序建议

由于外键约束，删除操作需按以下顺序执行：

**第一级（无外键依赖）**：
- `player_action`, `player_exploration`, `player_stealth`, `player_npc_recognition`
- `game_activity_log`, `clue_trigger_log`
- `faction_action`, `night_action`, `quick_interaction`
- `trade_items`, `trade`
- `milestone_player_status`
- `endgame_ark_event`, `endgame_shelter_event`

**第二级（依赖第一级或无关键依赖）**：
- `player_daily_consumption`
- `player_items`
- `drawn_cards`, `selected_catastrophe`
- `shelter_daily_labor`, `shelter_labor_day`
- `location_governance`
- `npc_daily_dialogue_count`, `npc_daily_trade_count`
- `npc_favor`

**第三级（依赖player）**：
- `player`

**第四级（依赖其他可清除表）**：
- `catastrophe_deck`
- `shelter_progress`, `shelter_stock`
- `ark_construction`, `ark_construction_log`, `ark_sail`, `ark_voyage`
- `warehouse_ark`, `warehouse_armory`, `warehouse_dock`, `warehouse_fuel`, `warehouse_general`, `warehouse_rebel`
- `lore_player_grant`

**第五级（游戏状态）**：
- `game_state`, `game_day_settings`
- `catastrophe_progress`

### 3.3 需保留数据的保护

以下表之间存在外键依赖，清除数据时需确保不会影响保留数据：

| 保留表 | 被引用情况 | 风险评估 |
|--------|------------|----------|
| `job` | `player.job_id`, `job_initial_items.job_id` | 低风险，player会被删除 |
| `skill` | `player.skill_id` | 低风险，player会被删除 |
| `item` | `player_items.item_id`, `job_initial_items.item_id`, `warehouse_*.item_id` | 中等风险，需确保仓库数据先删除 |
| `weapon` | `player_items.weapon_id`, `ammo.weapon_id` | 低风险 |
| `material` | `player_items.material_id`, `warehouse_*.item_id` | 中等风险 |
| `location` | `location_facility.location_id`, `location_npc.location_id`, `location_governance.location_id`, `npc_dialogue.location_id`, `npc_favor.location_id` | 中等风险 |
| `catastrophe_card` | `catastrophe_deck.card_id` | 低风险，deck会被删除 |
| `warehouse_config` | 无外键依赖 | 无风险 |
| `ark_config` | `ark_construction`隐式依赖 | 低风险 |

---

## 4. 安全机制建议

### 4.1 操作前数据备份

**备份策略**：
1. **完整备份**：执行重置前对整个数据库进行完整备份
   ```sql
   mysqldump -u username -p snowisland > backup_before_reset_YYYYMMDD.sql
   ```

2. **增量备份**：记录每次重置操作的时间点，便于回滚

3. **备份存储**：备份文件应存储在独立于数据库服务器的位置，保留至少30天

### 4.2 操作权限控制

**权限要求**：
1. **DM权限**：重置操作仅限DM角色执行
2. **双重验证**：执行重置前需再次确认操作意图
3. **操作日志**：记录所有重置操作的执行时间、操作者、操作内容

**代码层面权限控制**：
- 在Controller层验证userRole参数
- 使用Spring Security进行API访问控制
- 记录操作日志到专门的审计表

### 4.3 操作日志记录

**日志内容**：
- 操作时间
- 操作者ID和用户名
- 操作类型（重置/清除）
- 影响的表和记录数量
- 操作结果（成功/失败）
- 失败原因（如失败）

**日志存储**：
- 存储到独立的审计日志表
- 保留期限：至少90天
- 不可篡改：日志记录写入后不可修改

### 4.4 数据校验机制

**重置后校验**：
1. **计数校验**：验证关键表的记录数量是否符合预期
2. **完整性校验**：验证保留表的数据完整性
3. **关系校验**：验证外键关系是否正常
4. **初始状态校验**：验证游戏状态是否恢复至初始值（game_day=1, phase=DAY等）

**校验示例**：
```sql
-- 验证保留表数据完整
SELECT COUNT(*) FROM job; -- 应返回职业总数
SELECT COUNT(*) FROM location; -- 应返回地点总数
SELECT COUNT(*) FROM rule_book; -- 应返回规则书条目数

-- 验证可清除表已清空
SELECT COUNT(*) FROM player; -- 应返回0或初始玩家数
SELECT COUNT(*) FROM player_action; -- 应返回0
SELECT COUNT(*) FROM ark_construction; -- 应返回0或初始状态
```

### 4.5 事务处理

**事务策略**：
1. **整体事务**：将所有删除操作包裹在一个事务中，确保原子性
2. **回滚机制**：任何一步失败，整体回滚
3. **批量操作**：使用`deleteAllInBatch()`而非`deleteAll()`提高性能

**事务隔离级别**：
- 使用`@Transactional`注解
- 设置合适的隔离级别，避免并发问题

---

## 5. 重置操作流程图

```
┌─────────────────────────────────────────────────────────────┐
│                      开始重置流程                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  1. 权限验证：检查操作者是否为DM角色                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  2. 二次确认：提示用户确认操作，显示影响范围                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  3. 数据备份：自动执行数据库备份                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  4. 事务开始：开启数据库事务                                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  5. 按序删除：按依赖顺序删除各表数据                          │
│     - 第一级：无外键依赖表                                   │
│     - 第二级：依赖player的表                                │
│     - 第三级：player表                                     │
│     - 第四级：其他可清除表                                   │
│     - 第五级：游戏状态表                                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  6. 数据初始化：恢复初始玩家、初始物资等                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  7. 事务提交：提交事务                                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  8. 数据校验：验证重置结果                                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  9. 日志记录：记录操作日志                                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      完成重置流程                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. 总结

### 6.1 数据分类统计

| 分类 | 表数量 | 说明 |
|------|--------|------|
| 可清除数据 | 40 | 游戏进程中产生的动态数据 |
| 需保留数据 | 28 | 游戏基础配置数据 |
| 总计 | 68 | 数据库所有表 |

### 6.2 关键注意事项

1. **外键约束**：删除操作必须按正确顺序执行，避免外键冲突
2. **事务安全**：使用事务保证操作的原子性，失败时自动回滚
3. **数据备份**：执行前自动备份，确保可恢复
4. **权限控制**：仅限DM执行，需二次确认
5. **操作日志**：记录所有操作，便于审计和追踪

### 6.3 实施建议

1. **分阶段实施**：先实现核心表的重置功能，再逐步扩展
2. **测试验证**：在测试环境充分验证后再部署到生产环境
3. **文档更新**：同步更新相关API文档和操作手册
