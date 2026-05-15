package com.example.sceneenglish.data.ai

import com.example.sceneenglish.domain.model.Dialogue
import com.example.sceneenglish.domain.model.DialogueTurn
import com.example.sceneenglish.domain.model.ImageLabel
import com.example.sceneenglish.domain.model.LearningPack
import com.example.sceneenglish.domain.model.PhraseItem
import com.example.sceneenglish.domain.model.ReviewQuizItem
import com.example.sceneenglish.domain.model.RoleplayTask
import com.example.sceneenglish.domain.model.SentenceItem
import com.example.sceneenglish.domain.model.VocabularyItem
import com.example.sceneenglish.domain.model.WordMeaning
import com.example.sceneenglish.util.DateTimeUtils

object MockLearningPack {
    fun tennis(sourceInput: String, level: String): LearningPack {
        val createdAt = DateTimeUtils.nowIso()
        fun word(
            id: Int,
            english: String,
            chinese: String,
            partOfSpeech: String,
            category: String,
            exampleEn: String,
            exampleZh: String,
            note: String,
            priority: Int = 1
        ) = VocabularyItem(
            id = "word_%03d".format(id),
            english = english,
            chinese = chinese,
            partOfSpeech = partOfSpeech,
            usageNoteZh = note,
            exampleEn = exampleEn,
            exampleZh = exampleZh,
            phonetic = "",
            categoryZh = category,
            priority = priority
        )
        fun phrase(
            id: Int,
            english: String,
            chinese: String,
            category: String,
            exampleEn: String,
            exampleZh: String,
            note: String = "网球课高频短语。",
            priority: Int = 1
        ) = PhraseItem(
            id = "phrase_%03d".format(id),
            english = english,
            chinese = chinese,
            usageNoteZh = note,
            exampleEn = exampleEn,
            exampleZh = exampleZh,
            categoryZh = category,
            priority = priority
        )
        fun sentence(
            id: Int,
            english: String,
            chinese: String,
            category: String,
            context: String,
            priority: Int = 1,
            wordByWord: List<WordMeaning> = english.split(" ")
                .take(4)
                .map { WordMeaning(it.trim(',', '.', '?'), "句中关键词") }
        ) = SentenceItem(
            id = "sentence_%03d".format(id),
            english = english,
            chinese = chinese,
            wordByWord = wordByWord,
            usageContextZh = context,
            categoryZh = category,
            priority = priority
        )
        return LearningPack(
            id = "pack_${System.currentTimeMillis()}",
            scenarioTitle = "Beginner Tennis Lesson",
            scenarioDescription = "A beginner student takes a tennis lesson with an English-speaking coach.",
            sourceInput = sourceInput,
            level = level,
            targetAccent = "american",
            createdAt = createdAt,
            vocabulary = listOf(
                word(1, "coach", "教练", "noun", "基础称呼", "My coach is very patient.", "我的教练很有耐心。", "称呼网球教练。"),
                word(2, "player / student", "学员 / 球员", "noun", "基础称呼", "The student is a beginner.", "这位学员是初学者。", "课上指学习者。"),
                word(3, "beginner", "初学者", "noun", "基础称呼", "I'm a beginner.", "我是初学者。", "介绍自己的水平。"),
                word(4, "tennis lesson", "网球课", "noun", "基础称呼", "I have a tennis lesson today.", "我今天有网球课。", "描述课程。"),
                word(5, "private lesson", "私教课", "noun", "基础称呼", "This is a private lesson.", "这是一节私教课。", "一对一课程。", 2),
                word(6, "drill", "训练项目", "noun", "训练相关", "Let's do a forehand drill.", "我们做一个正手训练。", "教练安排的练习。"),
                word(7, "rally", "对拉", "noun / verb", "训练相关", "Let's rally for five minutes.", "我们对拉五分钟。", "连续来回击球。"),
                word(8, "court", "球场", "noun", "场地区域", "Walk onto the court.", "走到球场上。", "网球场地。"),
                word(9, "net", "球网", "noun", "场地区域", "The ball hit the net.", "球打到了网上。", "球场中间的网。"),
                word(10, "baseline", "底线", "noun", "场地区域", "Stand near the baseline.", "站在底线附近。", "球场后方的线。"),
                word(11, "service line", "发球线", "noun", "场地区域", "Aim past the service line.", "瞄准发球线后面。", "发球区后侧的线。"),
                word(12, "sideline", "边线", "noun", "场地区域", "The ball landed near the sideline.", "球落在边线附近。", "判断出界常用。"),
                word(13, "service box", "发球区", "noun", "场地区域", "Serve into the service box.", "把球发进发球区。", "发球目标区域。"),
                word(14, "racket / racquet", "球拍", "noun", "器材与物品", "Hold the racket like this.", "像这样握球拍。", "美式英语常写作 racket。"),
                word(15, "tennis ball", "网球", "noun", "器材与物品", "Watch the tennis ball.", "盯着网球。", "黄色网球。"),
                word(16, "basket", "球筐", "noun", "器材与物品", "The balls are in the basket.", "球在球筐里。", "装训练球的筐。", 2),
                word(17, "ball machine", "发球机", "noun", "器材与物品", "We can use the ball machine.", "我们可以用发球机。", "自动送球设备。", 3),
                word(18, "forehand", "正手", "noun", "基础动作", "I want to practice my forehand.", "我想练正手。", "网球课高频词。"),
                word(19, "backhand", "反手", "noun", "基础动作", "My backhand feels weak.", "我的反手感觉弱。", "和 forehand 对应。"),
                word(20, "serve", "发球", "noun / verb", "基础动作", "Can we work on my serve?", "我们能练我的发球吗？", "既可作名词也可作动词。"),
                word(21, "return", "接发球", "noun / verb", "基础动作", "Let's practice returns.", "我们练接发球。", "对方发球后的回击。", 2),
                word(22, "volley", "截击", "noun / verb", "基础动作", "Keep the racket up for the volley.", "截击时把球拍举起来。", "网前不落地击球。", 2),
                word(23, "slice", "削球", "noun / verb", "基础动作", "Try a soft slice.", "试一个轻的削球。", "带下旋的击球。", 2),
                word(24, "topspin", "上旋", "noun", "技术细节", "Add more topspin.", "加更多上旋。", "让球向前下坠的旋转。", 2),
                word(25, "grip", "握拍", "noun", "技术细节", "Check your grip first.", "先检查你的握拍。", "握球拍的方式。"),
                word(26, "stance", "站姿", "noun", "技术细节", "Is my stance okay?", "我的站姿可以吗？", "脚和身体的站位。"),
                word(27, "split step", "分腿垫步", "noun", "技术细节", "Use a split step.", "做分腿垫步。", "准备移动的小跳步。", 2),
                word(28, "footwork", "脚步", "noun", "技术细节", "Your footwork is improving.", "你的脚步在进步。", "移动和站位。"),
                word(29, "contact point", "击球点", "noun", "技术细节", "Hit in front at the contact point.", "在前方击球点击球。", "球和拍接触的位置。", 2),
                word(30, "follow-through", "随挥", "noun", "技术细节", "Finish your follow-through.", "完成你的随挥。", "击球后的完整动作。"),
                word(31, "warm-up", "热身", "noun", "训练相关", "Let's start with a warm-up.", "我们先热身。", "上课开始的准备。"),
                word(32, "timing", "时机", "noun", "训练相关", "My timing is off.", "我的击球时机不对。", "击球早晚节奏。"),
                word(33, "control", "控制", "noun", "训练相关", "Control first.", "先控制。", "先稳定再加力。"),
                word(34, "consistency", "稳定性", "noun", "训练相关", "Consistency first.", "先稳定。", "连续把球打进。", 2),
                word(35, "target", "目标区域", "noun", "训练相关", "Aim at the target.", "瞄准目标区域。", "练习瞄准的位置。", 2),
                word(36, "deuce", "平分", "noun", "比分比赛", "The score is deuce.", "比分是平分。", "40:40 后的平分。", 3),
                word(37, "advantage", "占先", "noun", "比分比赛", "Advantage server.", "发球方占先。", "平分后的领先。", 3),
                word(38, "in", "界内", "adjective", "比分比赛", "Was that in?", "那球界内吗？", "判断球是否在界内。", 2),
                word(39, "out", "出界", "adjective", "比分比赛", "I think it was out.", "我觉得它出界了。", "判断球出界。", 2),
                word(40, "wrist", "手腕", "noun", "体力安全", "My wrist hurts.", "我的手腕疼。", "身体不适时说明。", 2),
                word(41, "shoulder", "肩膀", "noun", "体力安全", "My shoulder hurts.", "我的肩膀疼。", "常见运动部位。", 2),
                word(42, "break", "休息", "noun", "体力安全", "Can we take a short break?", "我们可以休息一下吗？", "请求暂停休息。")
            ),
            phrases = listOf(
                phrase(1, "watch the ball", "盯球", "教练指令", "Watch the ball and follow through.", "盯球，然后完成随挥。", "教练常用指令。"),
                phrase(2, "keep your eyes on the ball", "一直盯球", "教练指令", "Keep your eyes on the ball.", "眼睛一直看球。"),
                phrase(3, "bend your knees", "屈膝", "教练指令", "Bend your knees before you hit.", "击球前屈膝。"),
                phrase(4, "stay low", "重心低一点", "教练指令", "Stay low and stay balanced.", "重心低一点，保持平衡。"),
                phrase(5, "relax your grip", "放松握拍", "教练指令", "Relax your grip a little.", "稍微放松握拍。"),
                phrase(6, "turn your shoulders", "转肩", "教练指令", "Turn your shoulders earlier.", "早点转肩。"),
                phrase(7, "step into the ball", "向球迈步击球", "教练指令", "Step into the ball.", "向球迈步击球。"),
                phrase(8, "hit in front", "在身体前方击球", "技术动作", "Try to hit in front.", "试着在身体前方击球。"),
                phrase(9, "follow through", "随挥", "技术动作", "Don't stop. Follow through.", "不要停，完成随挥。"),
                phrase(10, "finish your swing", "完成挥拍动作", "技术动作", "Finish your swing across your body.", "把挥拍完成到身体另一侧。"),
                phrase(11, "move your feet", "调整脚步", "脚步移动", "Move your feet before you hit.", "击球前调整脚步。"),
                phrase(12, "small steps", "小碎步", "脚步移动", "Use small steps.", "用小碎步。"),
                phrase(13, "split step", "分腿垫步", "脚步移动", "Start with a split step.", "从分腿垫步开始。"),
                phrase(14, "recover to the middle", "回到中间", "脚步移动", "Recover to the middle after each shot.", "每次击球后回到中间。"),
                phrase(15, "hit cross-court", "打斜线", "击球方向", "Hit cross-court this time.", "这次打斜线。"),
                phrase(16, "hit down the line", "打直线", "击球方向", "Now hit down the line.", "现在打直线。"),
                phrase(17, "aim deeper", "打深一点", "击球方向", "Aim deeper, near the baseline.", "打深一点，靠近底线。"),
                phrase(18, "add more topspin", "加更多上旋", "击球方向", "Add more topspin for control.", "加更多上旋来控制球。"),
                phrase(19, "work on my serve", "练我的发球", "学员请求", "I want to work on my serve.", "我想练发球。"),
                phrase(20, "practice my forehand", "练我的正手", "学员请求", "Can we practice my forehand?", "我们能练我的正手吗？"),
                phrase(21, "show me again", "再示范一次", "听不懂求助", "Could you show me again?", "你能再示范一次吗？"),
                phrase(22, "say that again", "再说一遍", "听不懂求助", "Could you say that again?", "你能再说一遍吗？"),
                phrase(23, "speak a little slower", "说慢一点", "听不懂求助", "Could you speak a little slower?", "你能说慢一点吗？"),
                phrase(24, "take a short break", "短暂休息", "体力安全", "Can we take a short break?", "我们可以休息一下吗？"),
                phrase(25, "get some water", "喝点水", "体力安全", "Get some water and rest.", "喝点水，休息一下。"),
                phrase(26, "same time next week", "下周同一时间", "课程结束", "Same time next week?", "下周同一时间吗？")
            ),
            sentences = listOf(
                sentence(1, "Hi coach.", "教练好。", "上课开始", "见到教练时打招呼。"),
                sentence(2, "I'm a beginner.", "我是初学者。", "上课开始", "说明自己的水平。"),
                sentence(3, "I just started playing tennis.", "我刚开始打网球。", "上课开始", "补充自己的学习背景。"),
                sentence(4, "What should we practice today?", "今天我们练什么？", "上课开始", "询问课程安排。"),
                sentence(5, "Could we start with warm-up?", "我们可以先热身吗？", "上课开始", "请求先热身。"),
                sentence(6, "I want to practice my forehand.", "我想练正手。", "学员请求", "告诉教练今天想练什么。", 1, listOf(WordMeaning("I want to", "我想要"), WordMeaning("practice", "练习"), WordMeaning("my forehand", "我的正手"))),
                sentence(7, "I want to work on my serve.", "我想练发球。", "学员请求", "表达练习目标。"),
                sentence(8, "Could you show me again?", "你能再给我示范一次吗？", "听不懂与请求示范", "没有看清教练示范时使用。", 1, listOf(WordMeaning("Could you", "你能否"), WordMeaning("show me", "给我展示"), WordMeaning("again", "再一次"))),
                sentence(9, "Could you say that again?", "你能再说一遍吗？", "听不懂与请求示范", "没听清时使用。"),
                sentence(10, "Could you speak a little slower?", "你能说慢一点吗？", "听不懂与请求示范", "听不懂英文指令时使用。"),
                sentence(11, "What does that mean?", "那是什么意思？", "听不懂与请求示范", "询问单词或指令含义。"),
                sentence(12, "Like this?", "像这样吗？", "请求反馈", "做动作时确认是否正确。"),
                sentence(13, "Is this correct?", "这样对吗？", "请求反馈", "请教练确认动作。"),
                sentence(14, "How was that?", "刚才那个怎么样？", "请求反馈", "击球后请求评价。"),
                sentence(15, "Was that better?", "这样有好一点吗？", "请求反馈", "调整后询问效果。"),
                sentence(16, "What should I fix first?", "我应该先改哪里？", "请求反馈", "让教练给一个优先改进点。"),
                sentence(17, "Is my grip correct?", "我的握拍对吗？", "请求反馈", "检查握拍。"),
                sentence(18, "Should I bend my knees more?", "我应该多弯膝盖吗？", "请求反馈", "确认站姿细节。"),
                sentence(19, "I'll watch the ball.", "我会盯球。", "练习过程中", "回应教练提醒。"),
                sentence(20, "I'll focus on footwork.", "我会专注脚步。", "练习过程中", "说明自己会注意脚步。"),
                sentence(21, "I'll try to hit in front.", "我会试着在身体前方击球。", "练习过程中", "回应击球点建议。"),
                sentence(22, "Let's try again.", "我们再试一次。", "练习过程中", "请求继续练。"),
                sentence(23, "My timing is off.", "我的击球时机不对。", "描述问题", "描述节奏问题。"),
                sentence(24, "I'm hitting too late.", "我击球太晚了。", "描述问题", "正手或反手常见问题。"),
                sentence(25, "I keep missing the ball.", "我总是打不到球。", "描述问题", "描述常见失误。"),
                sentence(26, "I keep hitting the ball into the net.", "我总是把球打到网上。", "描述问题", "描述自己的常见问题。", 1, listOf(WordMeaning("I keep", "我一直 / 总是"), WordMeaning("hitting the ball", "打球"), WordMeaning("into the net", "到网上"))),
                sentence(27, "I keep hitting it out.", "我老是打出界。", "描述问题", "描述出界问题。"),
                sentence(28, "Can we take a short break?", "我们可以休息一下吗？", "体力安全", "需要休息时使用。"),
                sentence(29, "My wrist hurts.", "我的手腕疼。", "体力安全", "说明身体不适。"),
                sentence(30, "What should I practice at home?", "我回去应该练什么？", "课程结束", "课后询问练习建议。"),
                sentence(31, "Can we practice serve next time?", "下次我们可以练发球吗？", "课程结束", "预约下次课程内容。"),
                sentence(32, "Thank you, coach.", "谢谢教练。", "课程结束", "课程结束时道谢。")
            ),
            dialogues = listOf(
                Dialogue(
                    id = "dialogue_001",
                    title = "Starting the lesson",
                    descriptionZh = "课程开始时的真实对话。",
                    turns = listOf(
                        DialogueTurn("turn_001", "Coach", "What do you want to practice today?", "你今天想练什么？"),
                        DialogueTurn("turn_002", "Student", "I want to practice my forehand.", "我想练正手。"),
                        DialogueTurn("turn_003", "Coach", "Great. Let's start with a short warm-up.", "很好。我们先做一个简短热身。")
                    )
                ),
                Dialogue(
                    id = "dialogue_002",
                    title = "Asking for a demonstration",
                    descriptionZh = "听不懂或没看清动作时请求示范。",
                    turns = listOf(
                        DialogueTurn("turn_004", "Student", "Sorry, could you show me again?", "不好意思，你能再示范一次吗？"),
                        DialogueTurn("turn_005", "Coach", "Sure. Watch my shoulders and follow-through.", "当然。看我的转肩和随挥。"),
                        DialogueTurn("turn_006", "Student", "Like this?", "像这样吗？"),
                        DialogueTurn("turn_007", "Coach", "Yes, that's better. Now relax your grip.", "对，这样好多了。现在放松握拍。")
                    )
                ),
                Dialogue(
                    id = "dialogue_003",
                    title = "Forehand practice",
                    descriptionZh = "正手练习中的纠错对话。",
                    turns = listOf(
                        DialogueTurn("turn_008", "Coach", "Turn your shoulders earlier.", "早点转肩。"),
                        DialogueTurn("turn_009", "Student", "Am I hitting too late?", "我是不是击球太晚了？"),
                        DialogueTurn("turn_010", "Coach", "Yes. Prepare earlier and hit in front.", "是的。早点准备，在身体前方击球。"),
                        DialogueTurn("turn_011", "Student", "Okay, I'll try again.", "好的，我再试一次。")
                    )
                ),
                Dialogue(
                    id = "dialogue_004",
                    title = "Taking a break",
                    descriptionZh = "体力不够时请求休息。",
                    turns = listOf(
                        DialogueTurn("turn_012", "Student", "Can we take a short break?", "我们可以休息一下吗？"),
                        DialogueTurn("turn_013", "Coach", "Of course. Get some water.", "当然。喝点水。"),
                        DialogueTurn("turn_014", "Student", "Thanks. I need one minute.", "谢谢。我需要一分钟。")
                    )
                ),
                Dialogue(
                    id = "dialogue_005",
                    title = "After class",
                    descriptionZh = "课程结束时询问回家练习内容。",
                    turns = listOf(
                        DialogueTurn("turn_015", "Student", "What should I practice at home?", "我回去应该练什么？"),
                        DialogueTurn("turn_016", "Coach", "Practice shadow swings and footwork.", "练徒手挥拍和脚步。"),
                        DialogueTurn("turn_017", "Student", "Got it. Thank you, coach.", "明白了。谢谢教练。"),
                        DialogueTurn("turn_018", "Coach", "See you next time.", "下次见。")
                    )
                )
            ),
            imagePrompt = "A clear semi-realistic tennis lesson scene on a tennis court, with a coach, a beginner student, rackets, tennis balls, and a net. No text, captions, logos, or labels.",
            imageLabels = listOf(
                ImageLabel("label_001", "coach", "教练", "person", "指导学生的人。", "word_001", 0.64f, 0.18f),
                ImageLabel("label_002", "student", "学员", "person", "正在学习网球的人。", "word_002", 0.32f, 0.36f),
                ImageLabel("label_003", "racket", "球拍", "object", "打网球用的球拍。", "word_014", 0.46f, 0.54f),
                ImageLabel("label_004", "forehand", "正手", "action", "身体同侧击球动作。", "word_018", 0.20f, 0.22f),
                ImageLabel("label_005", "net", "球网", "object", "球场中间的网。", "word_009", 0.70f, 0.45f),
                ImageLabel("label_006", "baseline", "底线", "court", "球场后方的线。", "word_010", 0.12f, 0.78f),
                ImageLabel("label_007", "grip", "握拍", "action", "拿球拍的方式。", "word_025", 0.38f, 0.66f),
                ImageLabel("label_008", "tennis ball", "网球", "object", "黄色网球。", "word_015", 0.18f, 0.56f)
            ),
            roleplayTasks = listOf(
                RoleplayTask(
                    id = "roleplay_001",
                    userRole = "Student",
                    assistantRole = "Coach",
                    starterEnglish = "What do you want to practice today?",
                    starterChinese = "你今天想练什么？"
                ),
                RoleplayTask("roleplay_002", "Student", "Coach", "Show me your forehand grip.", "给我看一下你的正手握拍。"),
                RoleplayTask("roleplay_003", "Student", "Coach", "Try to hit the ball in front of your body.", "试着在身体前方击球。"),
                RoleplayTask("roleplay_004", "Student", "Coach", "Do you want to work on your serve now?", "你现在想练发球吗？"),
                RoleplayTask("roleplay_005", "Student", "Coach", "How does your wrist feel?", "你的手腕感觉怎么样？"),
                RoleplayTask("roleplay_006", "Student", "Coach", "What should you practice before next lesson?", "下次课前你应该练什么？")
            ),
            reviewQuiz = listOf(
                ReviewQuizItem("quiz_001", "我想练正手。", "I want to practice my forehand."),
                ReviewQuizItem("quiz_002", "你能再给我示范一次吗？", "Could you show me again?"),
                ReviewQuizItem("quiz_003", "你能说慢一点吗？", "Could you speak a little slower?"),
                ReviewQuizItem("quiz_004", "我总是下网。", "I keep hitting the ball into the net."),
                ReviewQuizItem("quiz_005", "我的击球时机不对。", "My timing is off."),
                ReviewQuizItem("quiz_006", "我应该多弯膝盖吗？", "Should I bend my knees more?"),
                ReviewQuizItem("quiz_007", "我们可以休息一下吗？", "Can we take a short break?"),
                ReviewQuizItem("quiz_008", "我回去应该练什么？", "What should I practice at home?"),
                ReviewQuizItem("quiz_009", "我想练发球。", "I want to work on my serve."),
                ReviewQuizItem("quiz_010", "这样对吗？", "Is this correct?"),
                ReviewQuizItem("quiz_011", "我刚开始打网球。", "I just started playing tennis."),
                ReviewQuizItem("quiz_012", "下次我们可以练发球吗？", "Can we practice serve next time?")
            )
        )
    }
}
