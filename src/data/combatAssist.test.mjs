/**
 * Node smoke tests for combat assist math / hit table / settlement.
 * Run: node src/data/combatAssist.test.mjs
 */
import {
  computeAdjustedValue,
  resolveCombatOutcome,
  shouldTriggerExtraHit,
  resolveExtraHit,
  resolveWeaponThreat,
  WEAPON_THREAT_BY_ID,
  EXTRA_HIT_TABLE,
  settleCombatRound,
  statusLabel,
  formatCombatReportPublic,
  applySettlementToFighters,
  DEFAULT_BONUS_LABEL,
} from './combatAssist.js'

function assert(cond, msg) {
  if (!cond) throw new Error(msg)
}

assert(computeAdjustedValue(2, 2) === 3, 'ex1 adjusted')
assert(resolveCombatOutcome(2, 2).name === '小胜', 'ex1 outcome')
assert(computeAdjustedValue(16, 7) === 7, 'ex2 adjusted')
assert(resolveCombatOutcome(16, 7).name === '大胜', 'ex2 outcome')
assert(resolveCombatOutcome(4, 5).name === '小胜', 'ceil 2.4→3 小胜')

assert(WEAPON_THREAT_BY_ID[6] === 3, 'spear threat 3')
assert(resolveWeaponThreat(12, 'outer') === 5, 'explosive outer')
assert(resolveWeaponThreat(12, 'inner') === 10, 'explosive inner')
assert(EXTRA_HIT_TABLE[3], 'threat-3 hit row')
assert(resolveWeaponThreat(1, 'inner', { applyNoShootHalving: true, jobSkills: '' }) === 2, 'pistol halved without 射击')
assert(resolveWeaponThreat(1, 'inner', { applyNoShootHalving: true, jobSkills: '射击' }) === 5, 'pistol full with 射击')

assert(shouldTriggerExtraHit(3, 5) === true, 'roll < threat triggers')
assert(shouldTriggerExtraHit(5, 5) === false, 'roll == threat no trigger')
assert(resolveExtraHit(5, 6).result === '重伤', 'pistol roll 6')
assert(resolveCombatOutcome(-5, 3).attacker.includes('重伤'), 'failure uses 重伤')

function seqRng(seq) {
  let i = 0
  return () => {
    const v = seq[i % seq.length]
    i += 1
    return (v - 1) / 6 + 1e-9
  }
}

const report = settleCombatRound({
  attackers: [
    {
      uid: 'a1',
      playerName: '攻甲',
      weaponId: 7,
      weaponThreat: 4,
      jobSkills: '射击',
      basePower: 1,
      skillBonus: 0,
      armorChoice: 'none',
      bonuses: [],
    },
  ],
  defenders: [
    {
      uid: 'd1',
      playerName: '守乙',
      weaponId: 1,
      weaponThreat: 5,
      jobSkills: '射击',
      basePower: 1,
      skillBonus: 0,
      armorChoice: 'body',
      bonuses: [],
    },
  ],
  locationDefense: 2,
  insiderBetrayal: false,
  round: 1,
  rng: seqRng([3, 1, 4, 4, 4, 4]),
})

assert(!report.error, 'settlement ok')
assert(report.atkBase === 5, 'atk base 1+4')
assert(report.defBase === 8, 'def base 1+5+2')
assert(report.atkFinal === 8, 'atk final')
assert(report.defFinal === 9, 'def final')
assert(statusLabel(report.defenders[0]), 'defender status')

const insider = settleCombatRound({
  attackers: [{ uid: 'a1', playerName: 'A', weaponId: 3, basePower: 1, skillBonus: 0, armorChoice: 'none', bonuses: [] }],
  defenders: [{ uid: 'd1', playerName: 'D', weaponId: 3, basePower: 1, skillBonus: 0, armorChoice: 'none', bonuses: [] }],
  locationDefense: 10,
  insiderBetrayal: true,
  round: 1,
  rng: seqRng([1, 1]),
})
assert(insider.locationDefenseUsed === 0, 'insider zeros defense')

const r2 = settleCombatRound({
  attackers: [{ uid: 'a1', playerName: 'A', weaponId: 3, basePower: 1, skillBonus: 0, armorChoice: 'none', bonuses: [] }],
  defenders: [{ uid: 'd1', playerName: 'D', weaponId: 3, basePower: 1, skillBonus: 0, armorChoice: 'none', bonuses: [] }],
  locationDefense: 5,
  round: 2,
  rng: seqRng([1, 1]),
})
assert(r2.locationDefenseUsed === 3, 'round2 ceil(5/2)=3')

// 旧版 1d4 标签已移除
assert(DEFAULT_BONUS_LABEL === '', 'no legacy 1d4 label')

// 手动填写的实体骰优先于自动掷骰
const manual = settleCombatRound({
  attackers: [{ uid: 'a1', playerName: 'A', weaponId: 3, basePower: 1, skillBonus: 0, armorChoice: 'none', bonuses: [], attackRoll: 6 }],
  defenders: [{ uid: 'd1', playerName: 'D', weaponId: 3, basePower: 1, skillBonus: 0, armorChoice: 'none', bonuses: [], attackRoll: 1 }],
  locationDefense: 0,
  round: 1,
  rng: seqRng([3, 3]),
})
assert(manual.atkRollSum === 6, 'manual attacker roll used')
assert(manual.defRollSum === 1, 'manual defender roll used')
assert(manual.attackers[0].rollWasManual === true, 'manual roll flagged')

// 公屏报告包含关键段落
const publicText = formatCombatReportPublic(manual)
assert(publicText.includes('战斗结算'), 'report header')
assert(publicText.includes('① 基础战力计算'), 'report step 1')
assert(publicText.includes('④ 战斗结果'), 'report step 4')
assert(publicText.includes('结算完毕'), 'report footer')

// 结算后清空骰输入，避免下一轮误用上一轮点数
const written = applySettlementToFighters(
  [{ uid: 'a1', playerName: 'A', attackRoll: 6, bonuses: [] }],
  manual.attackers,
)
assert(written[0].attackRoll === '', 'attackRoll cleared after settlement')

console.log('combatAssist.test.mjs: all passed')
