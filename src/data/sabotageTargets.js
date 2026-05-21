/** 阵营行动「破坏」可选设施（facilityId + locationId 须与数据库一致） */
export const SABOTAGE_FACILITY_OPTIONS = [
  { facilityId: 2, locationId: 1, label: '发电机（警察局）' },
  { facilityId: 4, locationId: 2, label: '发电机（镇长厅）' },
  { facilityId: 5, locationId: 3, label: '电报机（邮局）' },
  { facilityId: 10, locationId: 12, label: '烘焙炉（面包店）' },
  { facilityId: 11, locationId: 15, label: '木板蒸汽箱（伐木营地）' },
  { facilityId: 12, locationId: 15, label: '拖拉机（伐木营地）' },
  { facilityId: 13, locationId: 15, label: '发电机（伐木营地）' },
  { facilityId: 15, locationId: 18, label: '切石机（矿场）' },
]

export function getSabotageOptionByFacilityId(facilityId) {
  const id = parseInt(facilityId, 10)
  return SABOTAGE_FACILITY_OPTIONS.find((o) => o.facilityId === id) || null
}
