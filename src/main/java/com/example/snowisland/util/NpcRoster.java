package com.example.snowisland.util;

import com.example.snowisland.entity.LocationNpc;
import com.example.snowisland.entity.LocationNpc.Attitude;
import com.example.snowisland.entity.LocationNpc.Gender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 《最新版规则.doc》中当前开放的 12 名 NPC，以及标注「没开」不应出现的名单。
 */
public final class NpcRoster {

    public static final class Spec {
        public final String name;
        public final String job;
        public final Gender gender;
        public final String introduction;
        public final String personality;
        public final String dialogueStyle;
        public final int locationId;
        public final Attitude attitudeRuler;
        public final Attitude attitudeRebel;
        public final Attitude attitudeAdventurer;
        public final Attitude attitudeScourge;

        private Spec(String name, String job, Gender gender, String introduction,
                     String personality, String dialogueStyle, int locationId,
                     Attitude attitudeRuler, Attitude attitudeRebel,
                     Attitude attitudeAdventurer, Attitude attitudeScourge) {
            this.name = name;
            this.job = job;
            this.gender = gender;
            this.introduction = introduction;
            this.personality = personality;
            this.dialogueStyle = dialogueStyle;
            this.locationId = locationId;
            this.attitudeRuler = attitudeRuler;
            this.attitudeRebel = attitudeRebel;
            this.attitudeAdventurer = attitudeAdventurer;
            this.attitudeScourge = attitudeScourge;
        }
    }

    private static final int DOCK = 7;
    private static final int MARKET = 10;
    private static final int LUMBER = 15;
    private static final int MINE = 18;
    private static final int PRISON = 19;

    private static final List<Spec> CANONICAL = Collections.unmodifiableList(Arrays.asList(
            spec("克拉拉·南丁格尔", "渔民", Gender.女,
                    "家中贫困的普通渔民，只希望镇上保持平静。她每天修补渔网，数着日子过，祈祷暴雪别落到她头上。",
                    "胆怯温和，只想安稳过日子。",
                    "声音小，经常低头说话，提到“暴雪”时会下意识攥紧衣角。",
                    DOCK, Attitude.忽视, Attitude.忽视, Attitude.喜好, Attitude.厌恶),
            spec("杰克·塔克", "水手", Gender.男,
                    "曾在商船当水手，船沉后困在岛上，做梦都想再上一次船。他每天在码头踱步，盯着海平线发呆。",
                    "焦躁不安，像被困在笼子里的海鸟。",
                    "说话带着海风味的粗犷，每三句话里有一句是“等船来了我就走”。",
                    DOCK, Attitude.忽视, Attitude.厌恶, Attitude.喜好, Attitude.忽视),
            spec("鲍勃·塔克", "装卸工", Gender.男,
                    "一直在港口讨生活的搬运工，膀大腰圆，嘴里永远叼着半根烟。他谁都不信，只信工钱和拳头。",
                    "粗鲁务实，谁给钱多就给谁干活。",
                    "话不多，用“嗯”“行”“钱呢”三个词就能完成一次交易。",
                    DOCK, Attitude.喜好, Attitude.厌恶, Attitude.忽视, Attitude.忽视),
            spec("托马斯·伍德", "伐木工", Gender.男,
                    "沉默寡言的伐木工，靠砍树和做木工为生，只求安稳度日。他住在林中小屋，镇上的人很少见到他。",
                    "孤僻寡言，对政治毫无兴趣。",
                    "答话永远慢半拍，好像你说话他要先翻译成木头语言。使用单音节词为主。",
                    LUMBER, Attitude.喜好, Attitude.厌恶, Attitude.忽视, Attitude.忽视),
            spec("卡尔·铁锤", "矿工", Gender.男,
                    "脾气火爆的矿场工人，谁给好处就帮谁。他嗓门大、拳头硬，在矿场里混得开，没人敢惹他。",
                    "暴躁直率，利益至上，不在乎对错。",
                    "说话像在吼，每句话都带脏字，谈到“好处”时眼睛会亮起来。",
                    MINE, Attitude.喜好, Attitude.厌恶, Attitude.忽视, Attitude.厌恶),
            spec("维克多·斯通", "矿工", Gender.男,
                    "体格强壮的矿工，相信权力才是活下去的依靠。他崇拜强者，认为统治者镇得住场子，镇上才不至于乱套。",
                    "务实忠诚，迷信权力，看不起弱者。",
                    "说话简短有力，像在砸石头。提到“统治者”时语气会放尊重点。",
                    MINE, Attitude.喜好, Attitude.厌恶, Attitude.忽视, Attitude.厌恶),
            spec("塞缪尔·格雷", "农户", Gender.男,
                    "善良而质朴的普通农户，乐于帮助他人。他种的菜总有多余的分给邻居，从不计较回报。",
                    "温和宽厚，发自内心地相信善良。",
                    "语速舒缓，像在慢悠悠地翻土。喜欢用“我总觉得啊”开头，但从不强加观点。",
                    MARKET, Attitude.厌恶, Attitude.忽视, Attitude.喜好, Attitude.忽视),
            spec("弗雷德里克·波特", "农户", Gender.男,
                    "性格孤僻的住在镇外的农户，对别人的生死毫不在意。他种自己的地，吃自己的粮，从不参与镇上任何事。",
                    "冷漠自私，独来独往，不关心任何人。",
                    "能用点头摇头解决的绝不开腔，开了腔也是“关我什么事”。",
                    MARKET, Attitude.厌恶, Attitude.喜好, Attitude.忽视, Attitude.忽视),
            spec("米玛·雷铁斯托", "手工艺人", Gender.女,
                    "老实本分的手工艺人，喜欢待在自己的小屋偶尔出门。她编篮子、织布，手艺好但不爱张扬。",
                    "安静本分，不惹事也不怕事。",
                    "说话轻声细语，手上永远在忙活——编东西、缝东西、磨东西。句子短，不议论人。",
                    MARKET, Attitude.厌恶, Attitude.忽视, Attitude.喜好, Attitude.忽视),
            spec("汉斯·施密特", "工匠", Gender.男,
                    "什么都能修的工匠，从钟表到农具都难不倒他，只认工钱不认人。他修东西时从不说话，修完报价，拿钱走人。",
                    "理性冷漠，技术至上，人情淡薄。",
                    "除非在谈工钱，否则不开口。谈价格时句句精准，一句废话没有。",
                    MARKET, Attitude.喜好, Attitude.忽视, Attitude.忽视, Attitude.厌恶),
            spec("乔克·汤姆", "民兵", Gender.男,
                    "初始就跟着统治者干的监狱看守，一名很忠诚的下属。只是他有点小小的缺点，但统治者们也只能视而不见。",
                    "忠诚但管不住嘴，有小毛病但不致命。",
                    "话多，喜欢吹嘘自己和统治者的关系，说到一半会突然压低声音说“其实我告诉你个小秘密”。",
                    PRISON, Attitude.喜好, Attitude.厌恶, Attitude.忽视, Attitude.厌恶),
            spec("斯特·贝斯", "民兵", Gender.女,
                    "初始就跟着统治者干活的一名很忠心的下属。她会一直遵从统治者的决定，除非她看不到希望。",
                    "忠诚可靠，但有自己的底线。",
                    "话少，回答简洁。提到“统治者”时语气恭敬，提到“暴雪”时语气会变犹豫。",
                    PRISON, Attitude.喜好, Attitude.厌恶, Attitude.忽视, Attitude.厌恶)
    ));

    /** 规则文档标注「没开」、当前不进入对局的 NPC。 */
    private static final List<String> UNUSED_NAMES = Collections.unmodifiableList(Arrays.asList(
            "艾琳娜·费舍尔",
            "奥拉夫·斯滕",
            "莉莉安·韦弗"
    ));

    private NpcRoster() {
    }

    public static List<Spec> canonical() {
        return CANONICAL;
    }

    public static List<String> unusedNames() {
        return UNUSED_NAMES;
    }

    private static Spec spec(String name, String job, Gender gender, String introduction,
                             String personality, String dialogueStyle, int locationId,
                             Attitude attitudeRuler, Attitude attitudeRebel,
                             Attitude attitudeAdventurer, Attitude attitudeScourge) {
        return new Spec(name, job, gender, introduction, personality, dialogueStyle, locationId,
                attitudeRuler, attitudeRebel, attitudeAdventurer, attitudeScourge);
    }
}
