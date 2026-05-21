/** Parse transport notes saved on PlayerAction. */
export function parseTransportNotes(notes) {
  const out = { mode: '', source: '', dest: '', items: [] }
  if (!notes || typeof notes !== 'string') return out
  for (const line of notes.split('\n')) {
    const trimmed = line.trim()
    let m = trimmed.match(/^\[mode:(.+)\]$/)
    if (m) {
      out.mode = m[1]
      continue
    }
    m = trimmed.match(/^\[source:(.+)\]$/)
    if (m) {
      out.source = m[1]
      continue
    }
    m = trimmed.match(/^\[dest:(.+)\]$/)
    if (m) {
      out.dest = m[1]
      continue
    }
    m = trimmed.match(/^\[item:([^|]+)\|(\d+)\|(\d+)\|/)
    if (m) {
      out.items.push({
        itemType: m[1],
        itemId: parseInt(m[2], 10),
        quantity: parseInt(m[3], 10),
      })
    }
  }
  return out
}

export function applyTransportQuantities(transportItems, parsedItems) {
  if (!Array.isArray(transportItems) || !parsedItems?.length) return
  for (const pi of parsedItems) {
    const row = transportItems.find(
      (i) => i.itemType === pi.itemType && Number(i.itemId) === Number(pi.itemId)
    )
    if (row) row.quantity = pi.quantity
  }
}

export function applyFactionPayload(type, payload, forms) {
  if (!payload || !type || !forms[type]) return
  const f = forms[type]
  switch (type) {
    case 'assign_personnel': {
      const kind = payload.targetKind || 'player'
      const tid = payload.targetId
      f.targetId = tid != null ? `${kind}:${tid}` : ''
      const assigned = Array.isArray(payload.assignedActions) ? payload.assignedActions : []
      f.actionCount = Math.min(2, Math.max(1, assigned.length || payload.actionCount || 1))
      f.assignedActions = [
        {
          action: assigned[0]?.action || '',
          targetLocationId:
            assigned[0]?.targetLocationId != null ? String(assigned[0].targetLocationId) : '',
          targetPlayerId:
            assigned[0]?.targetPlayerId != null ? String(assigned[0].targetPlayerId) : '',
        },
        {
          action: assigned[1]?.action || '',
          targetLocationId:
            assigned[1]?.targetLocationId != null ? String(assigned[1].targetLocationId) : '',
          targetPlayerId:
            assigned[1]?.targetPlayerId != null ? String(assigned[1].targetPlayerId) : '',
        },
      ]
      f.note = payload.note || ''
      break
    }
    case 'assign_guard':
      f.actorId = payload.actorId != null ? String(payload.actorId) : ''
      f.targetLocationId =
        payload.targetLocationId != null ? String(payload.targetLocationId) : ''
      break
    case 'extra_labor':
      f.note = payload.note || ''
      break
    case 'secret_contact':
      f.targetPlayerId = payload.targetPlayerId != null ? String(payload.targetPlayerId) : ''
      f.message = payload.message || ''
      f.anonymous = Boolean(payload.anonymous)
      break
    case 'sabotage':
      f.targetLocationId =
        payload.targetLocationId != null ? String(payload.targetLocationId) : ''
      f.facilityId = payload.facilityId != null ? String(payload.facilityId) : ''
      break
    case 'extra_investigate':
      f.investigateType = payload.investigateType || 'investigate_player'
      f.targetId =
        payload.targetId != null
          ? String(payload.targetId)
          : payload.target1 != null
            ? String(payload.target1)
            : ''
      break
    case 'guard_ark':
      f.guardId = payload.guardId != null ? String(payload.guardId) : ''
      f.useWeaponOrSkill = Boolean(payload.useWeaponOrSkill)
      break
    case 'ark_construction':
      f.actionPoints = payload.actionPoints != null ? Number(payload.actionPoints) : 1
      f.useSpecialMaterials = Boolean(payload.useSpecialMaterials)
      f.note = payload.note || ''
      break
    case 'curse':
      f.weaponId = payload.weaponId != null ? String(payload.weaponId) : ''
      f.target1 = payload.target1 != null ? String(payload.target1) : ''
      f.target2 = payload.target2 != null ? String(payload.target2) : ''
      break
    default:
      break
  }
}

export function applyNightPayload(type, payload, forms) {
  if (!payload || !type || !forms[type]) return
  const f = forms[type]
  switch (type) {
    case 'night_personal_action':
      f.actionType = payload.actionType || ''
      f.targetId = payload.targetId != null ? String(payload.targetId) : ''
      f.npcId = payload.npcId != null ? String(payload.npcId) : ''
      f.notes = payload.notes || ''
      break
    case 'public_trial':
      f.targetPlayerId = payload.targetPlayerId != null ? String(payload.targetPlayerId) : ''
      f.note = payload.note || ''
      break
    case 'pressure_ruler':
      f.demand = payload.demand || ''
      f.note = payload.note || ''
      break
    case 'publicity':
      f.message = payload.message || ''
      f.note = payload.note || ''
      break
    case 'ark_build':
      f.actionPoints = payload.actionPoints != null ? Number(payload.actionPoints) : 1
      f.note = payload.note || ''
      break
    case 'conspiracy':
      f.conspiracySubtype = payload.conspiracySubtype || ''
      f.targetLocationId =
        payload.targetLocationId != null ? String(payload.targetLocationId) : ''
      f.targetPlayerId =
        payload.targetPlayerId != null ? String(payload.targetPlayerId) : ''
      f.participantIds = Array.isArray(payload.participantIds)
        ? payload.participantIds.map((id) => Number(id))
        : []
      f.raidOutcome = payload.raidOutcome || ''
      f.note = payload.note || ''
      break
    default:
      break
  }
}
