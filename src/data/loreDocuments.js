import imgDeadHandwriting from '@/assets/某位亡者留下的手书.png?url'
import imgWeatherNotice from '@/assets/来自气象观测站的紧急通告.png?url'
import imgChurchBell from '@/assets/圣铃哀鸣：教堂爆炸惨案纪略.png?url'
import imgExSheriff from '@/assets/《某前警长日录残页》——经年辗转，原主姓名已佚，唯笔迹犹存.png?url'
import imgWallWhispers from '@/assets/ 《壁语》.png?url'
import imgCatNotes from '@/assets/自称【猫】的残缺笔记.png?url'
import imgBrewerNotes from '@/assets/酿酒师残破手记.png?url'

export const LORE_DISCOVERY_WARNING =
  '我不确定这是否是所有人都知道的信息，也许我得小心不能暴露这个信息的来源。说出去又有什么好处呢？'

/** @type {Array<{ slug: string, title: string, fileName: string, imageUrl: string }>} */
export const LORE_DOCUMENTS = [
  {
    slug: 'dead-handwriting',
    title: '某位亡者留下的手书',
    fileName: '某位亡者留下的手书.png',
    imageUrl: imgDeadHandwriting,
  },
  {
    slug: 'weather-station-notice',
    title: '来自气象观测站的紧急通告',
    fileName: '来自气象观测站的紧急通告.png',
    imageUrl: imgWeatherNotice,
  },
  {
    slug: 'church-bell-tragedy',
    title: '圣铃哀鸣：教堂爆炸惨案纪略',
    fileName: '圣铃哀鸣：教堂爆炸惨案纪略.png',
    imageUrl: imgChurchBell,
  },
  {
    slug: 'ex-sheriff-journal',
    title: '《某前警长日录残页》——经年辗转，原主姓名已佚，唯笔迹犹存',
    fileName: '《某前警长日录残页》——经年辗转，原主姓名已佚，唯笔迹犹存.png',
    imageUrl: imgExSheriff,
  },
  {
    slug: 'wall-whispers',
    title: '《壁语》',
    fileName: ' 《壁语》.png',
    imageUrl: imgWallWhispers,
  },
  {
    slug: 'cat-notes',
    title: '自称【猫】的残缺笔记',
    fileName: '自称【猫】的残缺笔记.png',
    imageUrl: imgCatNotes,
  },
  {
    slug: 'brewer-tattered-notes',
    title: '酿酒师残破手记',
    fileName: '酿酒师残破手记.png',
    imageUrl: imgBrewerNotes,
  },
]

export function getLoreBySlug(slug) {
  return LORE_DOCUMENTS.find((d) => d.slug === slug) || null
}
