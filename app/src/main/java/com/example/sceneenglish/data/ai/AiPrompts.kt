package com.example.sceneenglish.data.ai

import com.example.sceneenglish.domain.model.LearningPack

object AiPrompts {
    fun learningPackPrompt(sourceInput: String, level: String): String {
        return initialLearningPackPrompt(sourceInput, level)
    }

    fun initialLearningPackPrompt(sourceInput: String, level: String): String = """
        You are an expert American English learning content designer for Chinese-speaking learners.

        Generate ONLY the fast initial learning pack for this scenario. This first response must be quick.
        Include scenario metadata, vocabulary, image prompt, and image labels. Leave phrases, sentences, dialogues, roleplayTasks, and reviewQuiz as empty arrays.

        Rules:
        1. Natural American English for level $level.
        2. Chinese must be Simplified Chinese.
        3. Return valid JSON only. Do not wrap it in another object.
        4. Top-level JSON must match this shape exactly:
        {
          "id": "pack_generated_id",
          "scenarioTitle": "Short English title",
          "scenarioDescription": "One English sentence describing the scene",
          "sourceInput": "$sourceInput",
          "level": "$level",
          "targetAccent": "american",
          "createdAt": "2026-05-13T00:00:00-04:00",
          "vocabulary": [
            {
              "id": "word_001",
              "english": "racket",
              "chinese": "球拍",
              "partOfSpeech": "noun",
              "usageNoteZh": "中文用法说明",
              "exampleEn": "Hold the racket like this.",
              "exampleZh": "像这样握球拍。",
              "phonetic": "/'raekit/",
              "categoryZh": "器材与物品",
              "priority": 1
            }
          ],
          "phrases": [],
          "sentences": [],
          "dialogues": [],
          "imagePrompt": "Create a wordless realistic scene. No text, letters, captions, signs, logos, or watermarks.",
          "imageLabels": [
            {
              "id": "label_001",
              "english": "racket",
              "chinese": "球拍",
              "labelType": "object",
              "descriptionZh": "中文说明",
              "relatedItemId": "word_001",
              "x": 0.45,
              "y": 0.55
            }
          ],
          "roleplayTasks": [],
          "reviewQuiz": []
        }

        Quantity:
        - vocabulary: 35-60 practical high-frequency words, grouped by real sub-scenes/categories
        - imageLabels: 12-24 visible labels, preferably matching vocabulary

        Vocabulary requirements:
        - Add a simple American English phonetic hint in "phonetic" for every vocabulary item.
        - Add "categoryZh" for every vocabulary item.
        - Add "priority": 1 for must-know, 2 for useful, 3 for optional.
        - Prefer words that are visually memorable and useful in the real scene.
        - Cover both visible nouns and action/concept words the learner must understand.
        - For sports/lesson scenarios, include categories similar to: 基础称呼, 学员沟通, 教练指令, 场地区域, 基础动作, 技术细节, 训练相关, 比分比赛, 体力安全.
        - For other scenarios, create equally practical real-life categories based on the scenario.
        - Avoid duplicates; split alternatives with slash only when they are true variants such as "racket / racquet".

        User scenario:
        $sourceInput
    """.trimIndent()

    fun phrasesPrompt(pack: LearningPack): String = """
        Generate common spoken phrases for this learning pack.
        Scenario: ${pack.scenarioTitle}
        Description: ${pack.scenarioDescription}
        Level: ${pack.level}
        Vocabulary: ${pack.vocabulary.joinToString(", ") { it.english }}

        Return JSON only:
        {
          "phrases": [
            {
              "id": "phrase_001",
              "english": "watch the ball",
              "chinese": "盯球",
              "usageNoteZh": "中文用法说明",
              "exampleEn": "Watch the ball and follow through.",
              "exampleZh": "盯球，然后完成随挥。",
              "categoryZh": "教练指令",
              "priority": 1
            }
          ]
        }

        Generate 25-40 phrases. Group them with categoryZh.
        Include must-know collocations, short commands, learner requests, common problem descriptions, and scenario-specific chunks.
        Keep them short and natural.
    """.trimIndent()

    fun sentencesPrompt(pack: LearningPack): String = """
        Generate practical spoken sentences for Chinese-speaking learners.
        Scenario: ${pack.scenarioTitle}
        Description: ${pack.scenarioDescription}
        Level: ${pack.level}
        Vocabulary: ${pack.vocabulary.joinToString(", ") { it.english }}

        Return JSON only:
        {
          "sentences": [
            {
              "id": "sentence_001",
              "english": "Could you show me again?",
              "chinese": "你能再给我示范一次吗？",
              "wordByWord": [
                { "text": "Could you", "meaningZh": "你能否" },
                { "text": "show me", "meaningZh": "给我展示" }
              ],
              "usageContextZh": "什么时候使用这句话",
              "categoryZh": "听不懂与请求示范",
              "priority": 1
            }
          ]
        }

        Generate 40-70 sentences. Group them with categoryZh.
        Include:
        - what the learner says
        - what the other person/coach/service person says
        - misunderstanding/clarification sentences
        - asking for feedback
        - describing problems
        - safety/rest/ending sentences when relevant
        Keep A1-B1 sentences short, spoken, and practical.
    """.trimIndent()

    fun dialoguesPrompt(pack: LearningPack): String = """
        Generate realistic short dialogues for this scenario.
        Scenario: ${pack.scenarioTitle}
        Description: ${pack.scenarioDescription}
        Level: ${pack.level}

        Return JSON only:
        {
          "dialogues": [
            {
              "id": "dialogue_001",
              "title": "Starting the lesson",
              "descriptionZh": "中文说明",
              "turns": [
                {
                  "id": "turn_001",
                  "speaker": "Coach",
                  "english": "What do you want to practice today?",
                  "chinese": "你今天想练什么？"
                }
              ]
            }
          ]
        }

        Generate 4-6 dialogues, each 4-8 turns.
        Cover different sub-scenes/categories, not the same conversation repeated.
        Keep them natural and simple.
    """.trimIndent()

    fun roleplayTasksPrompt(pack: LearningPack): String = """
        Generate roleplay tasks for this scenario.
        Scenario: ${pack.scenarioTitle}
        Description: ${pack.scenarioDescription}
        Level: ${pack.level}

        Return JSON only:
        {
          "roleplayTasks": [
            {
              "id": "roleplay_001",
              "userRole": "Student",
              "assistantRole": "Coach",
              "starterEnglish": "What do you want to practice today?",
              "starterChinese": "你今天想练什么？"
            }
          ]
        }

        Generate 6-10 tasks covering different sub-scenes.
    """.trimIndent()

    fun reviewQuizPrompt(pack: LearningPack): String = """
        Generate Chinese-to-English review quiz items for this scenario.
        Scenario: ${pack.scenarioTitle}
        Description: ${pack.scenarioDescription}
        Level: ${pack.level}

        Return JSON only:
        {
          "reviewQuiz": [
            {
              "id": "quiz_001",
              "promptZh": "我想练正手。",
              "expectedAnswer": "I want to practice my forehand."
            }
          ]
        }

        Generate 12-20 items using practical spoken English from the most useful sentences and phrases.
    """.trimIndent()
}
