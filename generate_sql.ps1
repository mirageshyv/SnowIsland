﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿# Read the extracted rules text
$content = Get-Content "f:\java\SnowIsland\rules_extracted.txt" -Raw -Encoding UTF8
$searchStr = [string]::new([char[]]@(27599,26085,28216,25103,27969,31243))
$idx = $content.IndexOf($searchStr)
$cleanContent = $content.Substring($idx, 23250)

# Define sections with precise boundaries based on content analysis
$sections = @(
    @{Section='每日游戏流程'; Title='每日游戏流程'; Start=0; End=424}
    @{Section='自由行动'; Title='白天自由行动'; Start=7; End=424}
    @{Section='自由讨论'; Title='自由讨论'; Start=424; End=545}
    @{Section='协议'; Title='协议'; Start=545; End=933}
    @{Section='夜间阶段'; Title='夜间阶段'; Start=933; End=1162}
    @{Section='群组与交流'; Title='群组与交流'; Start=1162; End=1490}
    @{Section='资源交换'; Title='资源交换'; Start=1490; End=1809}
    @{Section='资源移动'; Title='资源移动'; Start=1809; End=2182}
    @{Section='战斗规则'; Title='战斗规则'; Start=2182; End=2700}
    @{Section='伤亡规则'; Title='伤亡规则'; Start=2700; End=3000}
    @{Section='夜间结算'; Title='夜间结算阶段规则'; Start=3000; End=3571}
    @{Section='天灾系统'; Title='天灾系统'; Start=3571; End=3811}
    @{Section='天灾牌'; Title='天灾牌'; Start=3811; End=4331}
    @{Section='地点描述'; Title='地点描述'; Start=4331; End=7707}
    @{Section='NPC规则'; Title='NPC规则'; Start=7707; End=9313}
    @{Section='阵营机制'; Title='阵营机制-统治者'; Start=9313; End=11158}
    @{Section='阵营机制'; Title='阵营机制-反抗者'; Start=11158; End=14523}
    @{Section='阵营机制'; Title='阵营机制-冒险者'; Start=14523; End=16612}
    @{Section='阵营机制'; Title='阵营机制-天灾使者'; Start=16612; End=18238}
    @{Section='阵营机制'; Title='阵营机制-平民'; Start=18238; End=18578}
    @{Section='方舟建造'; Title='方舟建造'; Start=14801; End=16612}
    @{Section='技能表'; Title='技能表'; Start=18578; End=20167}
    @{Section='标记系统'; Title='标记系统'; Start=20167; End=20447}
    @{Section='终局结算'; Title='进入避难所与方舟'; Start=20447; End=20846}
    @{Section='终局结算'; Title='避难所结局结算'; Start=20846; End=22120}
    @{Section='终局结算'; Title='方舟结局结算'; Start=22120; End=23250}
)

# Generate SQL
$sqlLines = [System.Collections.ArrayList]::new()
[void]$sqlLines.Add("-- Snow Island Rule Book Data")
[void]$sqlLines.Add("-- Generated from rules_extracted.txt")
[void]$sqlLines.Add("")
[void]$sqlLines.Add("DELETE FROM rule_book;")
[void]$sqlLines.Add("")

$orderNum = 1
foreach ($sec in $sections) {
    $len = $sec.End - $sec.Start
    if ($len -le 0) { continue }
    if ($sec.Start -ge $cleanContent.Length) { continue }
    if ($sec.End -gt $cleanContent.Length) {
        $len = $cleanContent.Length - $sec.Start
    }
    $sectionContent = $cleanContent.Substring($sec.Start, $len)

    # Escape single quotes by doubling them
    $escapedContent = $sectionContent.Replace("'", "''")
    $sectionName = $sec.Section.Replace("'", "''")
    $title = $sec.Title.Replace("'", "''")

    [void]$sqlLines.Add("INSERT INTO rule_book (section, title, content, order_num, created_at, updated_at)")
    [void]$sqlLines.Add("VALUES ('$sectionName', '$title', '$escapedContent', $orderNum, NOW(), NOW());")
    [void]$sqlLines.Add("")

    $orderNum++
}

$sqlContent = $sqlLines -join "`r`n"
Set-Content -Path "f:\java\SnowIsland\sql\rule_book_data.sql" -Value $sqlContent -Encoding UTF8
Write-Output "SQL file generated with $($sections.Count) sections, orderNum=$orderNum"
