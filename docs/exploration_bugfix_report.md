# 探索功能Bug修复报告

## 修复日期
2026-06-24

## 问题概述

本次修复解决了探索功能存在的两个主要bug：
1. **探索值为0的异常数据问题**：玩家提交探索时未投入任何物品仍能成功创建记录
2. **探索结果展示不完整**：探索完成后夜晚行动页面未能显示探索的具体内容

## 问题分析

### Bug 1: 探索值为0的异常数据

#### 根本原因
在 `IslandExplorationService.exploreIsland()` 方法中，当玩家没有投入任何探索物品时：
- `investPoints` 初始值为 0
- 即使 `investItems` 为空或无效，代码仍然允许保存探索记录
- 没有对投入的探索值进行有效性验证

#### 影响范围
- 数据库中可能存在 `investPoints = 0` 的无效探索记录
- 游戏平衡性被破坏（零投入也能触发探索）
- 资源消耗逻辑存在漏洞

### Bug 2: 探索结果展示不完整

#### 根本原因
1. 后端 `NightActionService.getContext()` 方法未包含探索信息到历史记录
2. 前端 `NightActionSubmitView.vue` 提交探索后未正确保存探索详情
3. 历史记录中缺少 `investPoints`、`diceResult` 等关键字段

#### 影响范围
- 玩家刷新页面后无法查看探索统计信息
- 夜晚行动历史中探索记录不完整
- 用户体验不佳

## 解决方案

### 修复 1: 探索值为0的数据验证

#### 后端修改 - IslandExplorationService.java

```java
// 添加验证：探索必须投入至少1点探索值
if (investItems == null || investItems.isEmpty()) {
    result.put("success", false);
    result.put("message", "探索必须投入至少1点探索值，请添加探索物资（火把、手电筒、蜡烛或绳索）");
    return result;
}

// 处理物品投入逻辑...

// 再次验证：确保投入了有效探索值
if (investPoints <= 0) {
    result.put("success", false);
    result.put("message", "探索必须投入至少1点探索值，请检查物品是否有效或库存是否充足");
    return result;
}
```

### 修复 2: 探索内容完整显示

#### 后端修改 - NightActionService.java

```java
// 添加探索信息到历史记录
todayExploration.ifPresent(exploration -> {
    Map<String, Object> explorationMap = new LinkedHashMap<>();
    explorationMap.put("id", exploration.getId());
    explorationMap.put("actionType", "explore_island");
    explorationMap.put("actionTypeLabel", "探索岛屿");
    explorationMap.put("result", exploration.getResult());
    explorationMap.put("investPoints", exploration.getInvestPoints());
    explorationMap.put("diceResult", exploration.getDiceResult());
    explorationMap.put("totalExplorationValue", exploration.getTotalExplorationValue());
    explorationMap.put("status", exploration.getStatus().name());
    explorationMap.put("gameDay", exploration.getGameDay());
    // 将探索添加到历史列表的开头
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> history = (List<Map<String, Object>>) ctx.get("history");
    history.add(0, explorationMap);
});
```

#### 前端修改 - NightActionSubmitView.vue

1. 添加探索详情状态变量：
```javascript
const explorationDetails = ref(null)
```

2. 提交成功后保存并展示探索详情：
```javascript
if (selectedType.value === 'explore_island' && res.data) {
    const data = res.data
    explorationDetails.value = {
        investPoints: data.investPoints,
        diceResult: data.diceResult,
        totalExplorationValue: data.totalExplorationValue,
        targetDifficulty: data.targetDifficulty,
    }
    // 生成友好的探索结果显示
    let detailText = `✓ 已提交【探索岛屿】\n\n`
    detailText += `📊 探索统计\n`
    detailText += `• 投入探索值: ${data.investPoints}\n`
    detailText += `• 骰子结果: ${data.diceResult}\n`
    detailText += `• 总探索值: ${data.totalExplorationValue}\n`
    // ...
}
```

3. 页面展示探索详情标签：
```vue
<div v-if="explorationDetails && selectedType === 'explore_island'" class="mb-3">
  <div class="flex flex-wrap gap-2 text-xs">
    <span class="bg-indigo-500/20 text-indigo-300 px-2 py-1 rounded">
      投入: {{ explorationDetails.investPoints }}
    </span>
    <span class="bg-amber-500/20 text-amber-300 px-2 py-1 rounded">
      骰子: {{ explorationDetails.diceResult }}
    </span>
    <span class="bg-emerald-500/20 text-emerald-300 px-2 py-1 rounded">
      总计: {{ explorationDetails.totalExplorationValue }}
    </span>
    <span v-if="explorationDetails.targetDifficulty" class="bg-purple-500/20 text-purple-300 px-2 py-1 rounded">
      难度: {{ explorationDetails.targetDifficulty }}
    </span>
  </div>
</div>
```

### 额外优化: 探索值超过20的特殊事件处理

#### 后端修改 - IslandExplorationService.java

```java
Optional<IslandEvent> optEvent = Optional.empty();

// 探索值超过20时，优先从难度20的特殊事件中抽取
if (totalValue > MAX_DIFFICULTY) {
    optEvent = islandEventRepository.findRandomSpecialByDifficulty(MAX_DIFFICULTY);
}

// 如果没有找到特殊事件或探索值不超过20，则按正常难度抽取
if (!optEvent.isPresent()) {
    optEvent = drawEventByDifficulty(targetDifficulty);
}
```

#### 新增方法 - IslandEventRepository.java

```java
/**
 * 查找特定难度的未触发特殊事件
 */
@Query(value = "SELECT * FROM island_event WHERE event_difficulty = :difficulty AND is_special = TRUE AND triggered = FALSE ORDER BY RAND() LIMIT 1", nativeQuery = true)
Optional<IslandEvent> findRandomSpecialByDifficulty(@Param("difficulty") Integer difficulty);
```

## 测试验证

### 测试 1: 验证探索值必须大于0

```bash
# 测试空物品提交
POST /api/exploration/submit
{
  "playerId": 8,
  "gameDay": 1,
  "investItems": {}
}

Response:
{
  "success": false,
  "message": "探索必须投入至少1点探索值，请添加探索物资（火把、手电筒、蜡烛或绳索）"
}
```
✅ 测试通过：空物品提交被正确拒绝

### 测试 2: 验证探索值超过20时的特殊事件处理

当玩家投入15点探索值 + 掷出6点骰子 = 总计21点时：
- 系统会优先从难度20的特殊事件中抽取
- 如果没有特殊事件可用，则降级到普通事件抽取

### 测试 3: 验证夜晚行动历史包含探索信息

```bash
GET /api/night-actions/context/{playerId}?gameDay={gameDay}

Response:
{
  "history": [
    {
      "actionType": "explore_island",
      "actionTypeLabel": "探索岛屿",
      "investPoints": 15,
      "diceResult": 5,
      "totalExplorationValue": 20,
      "status": "pending",
      "result": "✓ 已提交【探索岛屿】..."
    }
  ]
}
```
✅ 测试通过：探索信息正确添加到历史记录

## 边界值测试

| 探索投入 | 骰子结果 | 总探索值 | 预期行为 |
|---------|---------|---------|---------|
| 0 | - | - | ❌ 拒绝提交 |
| 1 | 1 | 2 | ✅ 正常提交，难度限制为2 |
| 7 | 6 | 13 | ✅ 正常提交，难度限制为13 |
| 15 | 6 | 21 | ✅ 正常提交，优先抽取难度20特殊事件 |
| 15 | 6 | 21 | ✅ totalExplorationValue记录为21（超过20） |

## 功能稳定性

- ✅ 后端编译通过
- ✅ 前端构建成功
- ✅ API接口正常工作
- ✅ 数据验证逻辑完整
- ✅ 历史记录展示正确

## 影响范围

### 受影响的文件
1. `IslandExplorationService.java` - 添加数据验证和特殊事件处理
2. `IslandEventRepository.java` - 新增查询特殊事件方法
3. `NightActionService.java` - 添加探索信息到历史记录
4. `NightActionSubmitView.vue` - 增强探索结果显示

### 兼容性说明
- 现有探索记录（investPoints=0）不会被自动清理，但新记录会强制验证
- 前端改动仅影响UI展示，不影响后端数据格式
- API返回格式保持向后兼容

## 总结

本次修复解决了探索功能的两个关键bug，并添加了探索值超过20时的特殊事件处理机制。修复后的系统能够：
1. 有效防止无效探索数据的产生
2. 完整展示探索过程的详细信息
3. 为高探索值玩家提供特殊事件体验
4. 保持数据完整性和用户体验一致性
