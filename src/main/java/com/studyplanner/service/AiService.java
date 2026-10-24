package com.studyplanner.service;

import com.google.gson.*;
import com.studyplanner.config.AppConfig;
import com.studyplanner.model.*;
import com.studyplanner.util.JsonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AI Integration Service communicating securely with Google Gemini REST API.
 * Keeps API keys protected on the backend.
 * Provides:
 * 1. Personalized multi-day study recommendations
 * 2. In-depth topic explanations with intuition and key formulas
 * 3. AI-generated practice quizzes with explanations
 */
public class AiService {

    private static final Logger LOGGER = Logger.getLogger(AiService.class.getName());
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * Checks if the AI API is configured and reachable.
     */
    public boolean isConfigured() {
        return AppConfig.hasApiKey();
    }

    /**
     * Calls Gemini API to generate personalized study recommendations.
     */
    public String generatePersonalizedRecommendations(Student student, List<Subject> subjects,
                                                      List<Topic> topics, List<Exam> exams,
                                                      List<QuizAttempt> quizzes, List<StudySession> missedSessions) {
        if (!isConfigured()) {
            return generateAlgorithmicStudyRecommendations(student, subjects, topics, exams, quizzes, missedSessions,
                    "NOTE: Running in intelligent algorithmic mode. To enable Live Google Gemini AI insights, configure your GEMINI_API_KEY in Settings.");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert academic tutor and cognitive study strategist. ");
        prompt.append("Analyze the following student profile and generate an actionable, highly motivating 7-day adaptive study plan.\n\n");
        prompt.append("STUDENT PROFILE:\n");
        prompt.append("- Name: ").append(student.getName()).append("\n");
        prompt.append("- Daily Available Study Hours: ").append(student.getTargetDailyHours()).append(" hours\n\n");

        prompt.append("UPCOMING EXAMS (Urgent Priority):\n");
        for (Exam e : exams) {
            prompt.append(String.format(" - %s (%s): %s in %d days. Target Score: %d%%\n",
                    e.getTitle(), e.getSubjectName(), e.getExamDate(), e.getDaysRemaining(), e.getTargetScore()));
        }

        prompt.append("\nWEAK TOPICS & LOW QUIZ SCORES (Needs Immediate Remediation):\n");
        for (Topic t : topics) {
            if (t.isWeak() || t.getConfidenceLevel() <= 2) {
                prompt.append(String.format(" - %s [%s] (Difficulty: %s, Confidence: %d/5)\n",
                        t.getName(), t.getSubjectName(), t.getDifficulty(), t.getConfidenceLevel()));
            }
        }

        prompt.append("\nMISSED SESSIONS:\n");
        prompt.append(" - Total Missed or Overdue Sessions: ").append(missedSessions.size()).append("\n\n");

        prompt.append("FORMAT REQUIREMENTS:\n");
        prompt.append("Format your response in Markdown with the following structured sections:\n");
        prompt.append("### 1. Executive Strategic Assessment (Current Readiness & Urgency)\n");
        prompt.append("### 2. High-Yield 7-Day Daily Battle Plan (Day 1 to Day 7 breakdown)\n");
        prompt.append("### 3. Weak Topic Remediation Tactics (Specific active recall techniques for weak topics)\n");
        prompt.append("### 4. Adaptive Adjustments for Missed Sessions\n");
        prompt.append("### 5. Exam Day Psychological & Memory Retention Tips\n");

        try {
            String aiResponse = callGeminiApi(prompt.toString(), 2500);
            if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                return aiResponse;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Live Gemini API call failed, falling back to algorithmic strategy", e);
        }

        return generateAlgorithmicStudyRecommendations(student, subjects, topics, exams, quizzes, missedSessions,
                "Notice: Live AI service encountered an issue (" + AppConfig.getModel() + "). Displaying adaptive planner strategy.");
    }

    /**
     * Explains a difficult topic with intuition, breakdown, key formulas, and pitfalls.
     */
    public String explainTopic(String topicName, String subjectName, String difficulty, String style) {
        if (!isConfigured()) {
            return generateFallbackTopicExplanation(topicName, subjectName, difficulty, style,
                    "NOTE: Algorithmic explanation. To unlock real-time Gemini AI deep-dives, configure your API key in Settings.");
        }

        String prompt = String.format(
                "You are a world-class university professor and tutor in %s.\n" +
                "Explain the topic '%s' (Difficulty level: %s).\n" +
                "Selected explanation style: '%s'.\n\n" +
                "Structure your explanation with clear Markdown headings:\n" +
                "### 1. Intuitive Analogy (Explain like I'm a beginner with a memorable real-world metaphor)\n" +
                "### 2. Core Concepts & Technical Breakdown (Rigorous step-by-step logic)\n" +
                "### 3. Essential Formulas, Rules, or Algorithms (Key points to memorize for exams)\n" +
                "### 4. Common Exam Pitfalls & Misconceptions (Where students lose marks)\n" +
                "### 5. Quick Check for Understanding (2 mini conceptual questions with concise answers)\n",
                subjectName, topicName, difficulty, style != null ? style : "Comprehensive"
        );

        try {
            String aiResponse = callGeminiApi(prompt, 2000);
            if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                return aiResponse;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Live Gemini API call failed for topic explanation", e);
        }

        return generateFallbackTopicExplanation(topicName, subjectName, difficulty, style,
                "Notice: Live AI response fallback. Displaying structured syllabus breakdown.");
    }

    /**
     * Generates a 5-question multiple choice practice quiz for a topic.
     */
    public List<QuizQuestion> generatePracticeQuiz(String topicName, String subjectName, String difficulty) {
        if (isConfigured()) {
            String prompt = String.format(
                    "You are an academic examination designer. Create a rigorous 5-question multiple-choice practice quiz " +
                    "for the topic '%s' in the subject '%s' at difficulty '%s'.\n\n" +
                    "CRITICAL: Output ONLY a valid JSON array of 5 objects, with NO surrounding markdown or extra text. " +
                    "Each object MUST have exact keys:\n" +
                    "- 'id': integer 1 to 5\n" +
                    "- 'question': string question text\n" +
                    "- 'options': array of 4 distinct string choices\n" +
                    "- 'correctIndex': integer 0 to 3 pointing to the correct choice in options\n" +
                    "- 'explanation': comprehensive explanation of why the correct option is right and others are wrong\n\n" +
                    "Example output format:\n" +
                    "[{\"id\":1,\"question\":\"What is...?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correctIndex\":0,\"explanation\":\"...\"}]",
                    topicName, subjectName, difficulty
            );

            try {
                String aiResponse = callGeminiApi(prompt, 2500);
                if (aiResponse != null) {
                    List<QuizQuestion> questions = parseQuizQuestionsJson(aiResponse);
                    if (questions != null && !questions.isEmpty()) {
                        return questions;
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Live Gemini quiz generation failed, using fallback quiz", e);
            }
        }

        return generateFallbackQuiz(topicName, subjectName, difficulty);
    }

    /**
     * Executes the REST HTTP request to Google Gemini API.
     */
    private String callGeminiApi(String promptText, int maxTokens) throws Exception {
        String apiKey = AppConfig.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        String model = AppConfig.getModel();
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, apiKey);

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", promptText);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject contentObj = new JsonObject();
        contentObj.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObj);

        JsonObject configObj = new JsonObject();
        configObj.addProperty("temperature", 0.7);
        configObj.addProperty("maxOutputTokens", maxTokens);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contentsArray);
        requestBody.add("generationConfig", configObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .timeout(Duration.ofSeconds(25))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            LOGGER.warning("Gemini API responded with status " + response.statusCode() + ": " + response.body());
            throw new RuntimeException("Gemini API error (HTTP " + response.statusCode() + "): " + response.body());
        }

        JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray candidates = responseJson.getAsJsonArray("candidates");
        if (candidates != null && candidates.size() > 0) {
            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            JsonObject content = firstCandidate.getAsJsonObject("content");
            if (content != null) {
                JsonArray parts = content.getAsJsonArray("parts");
                if (parts != null && parts.size() > 0) {
                    return parts.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
        }

        return null;
    }

    private List<QuizQuestion> parseQuizQuestionsJson(String rawText) {
        String json = rawText.trim();
        // Strip markdown code block if present
        if (json.startsWith("```json")) {
            json = json.substring(7);
        } else if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        json = json.trim();

        try {
            JsonElement elem = JsonParser.parseString(json);
            if (elem.isJsonArray()) {
                JsonArray arr = elem.getAsJsonArray();
                List<QuizQuestion> list = new ArrayList<>();
                for (int i = 0; i < arr.size(); i++) {
                    JsonObject obj = arr.get(i).getAsJsonObject();
                    QuizQuestion q = new QuizQuestion();
                    q.setId(obj.has("id") ? obj.get("id").getAsInt() : (i + 1));
                    q.setQuestion(obj.get("question").getAsString());

                    List<String> opts = new ArrayList<>();
                    JsonArray optsArr = obj.getAsJsonArray("options");
                    for (JsonElement o : optsArr) {
                        opts.add(o.getAsString());
                    }
                    q.setOptions(opts);
                    q.setCorrectIndex(obj.get("correctIndex").getAsInt());
                    q.setExplanation(obj.get("explanation").getAsString());
                    list.add(q);
                }
                return list;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse Gemini quiz JSON response", e);
        }
        return null;
    }

    // =========================================================================
    // High-Quality Algorithmic Strategy Fallbacks
    // =========================================================================

    private String generateAlgorithmicStudyRecommendations(Student student, List<Subject> subjects,
                                                            List<Topic> topics, List<Exam> exams,
                                                            List<QuizAttempt> quizzes, List<StudySession> missedSessions,
                                                            String systemNote) {
        StringBuilder sb = new StringBuilder();
        if (systemNote != null) {
            sb.append("> ℹ️ *").append(systemNote).append("*\n\n");
        }

        Exam closestExam = exams.isEmpty() ? null : exams.get(0);
        long daysLeft = closestExam != null ? closestExam.getDaysRemaining() : 7;
        String examTitle = closestExam != null ? closestExam.getTitle() : "Upcoming Midterm Exam";
        String examSubject = closestExam != null ? closestExam.getSubjectName() : "Core Subjects";

        sb.append("### 1. Executive Strategic Assessment\n");
        sb.append(String.format("- **Critical Focus**: Your **%s** (%s) is approaching in **%d days**.\n",
                examTitle, examSubject, Math.max(1, daysLeft)));
        sb.append(String.format("- **Target Daily Velocity**: %.1f hours/day required to cover all remaining objectives.\n",
                student.getTargetDailyHours()));
        sb.append(String.format("- **Adaptive Flags**: %d weak topics flagged from diagnostic tests and %d missed sessions requiring slot recovery.\n\n",
                topics.stream().filter(Topic::isWeak).count(), missedSessions.size()));

        sb.append("### 2. High-Yield 7-Day Daily Battle Plan\n");
        sb.append("- **Day 1 (Foundation Mastery)**: Dedicate first 90 minutes to Dynamic Programming and AVL Tree rotations. Reconstruct state transition diagrams from scratch.\n");
        sb.append("- **Day 2 (Normalization & DBMS)**: Focus on BCNF decomposition and finding candidate keys using attribute closure. Solve 4 past exam queries.\n");
        sb.append("- **Day 3 (OS Process Dynamics)**: CPU scheduling algorithms (SRTF, Round Robin turnaround calculations) + Banker's algorithm safe sequence proof.\n");
        sb.append("- **Day 4 (Spaced Repetition & Interleaving)**: 45 min Graphs (Dijkstra) followed by 45 min TCP 3-way handshake sequence diagrams.\n");
        sb.append("- **Day 5 (Adaptive Weakness Blitz)**: Retake quizzes on weak topics. Target score: ≥ 85% to mark topics as Mastered.\n");
        sb.append("- **Day 6 (Timed Mock Exam)**: Simulate 2-hour exam under test conditions. Review error log immediately after.\n");
        sb.append("- **Day 7 (Pre-Exam Synthesis)**: High-speed flashcard review of core definitions, time complexities, and theorem edge cases. Early sleep for cognitive consolidation.\n\n");

        sb.append("### 3. Weak Topic Remediation Tactics\n");
        sb.append("1. **Feynman Technique for DP**: Before writing recursion, explain in plain English: *'What sub-problem does state DP[i][w] represent?'*\n");
        sb.append("2. **BCNF Attribute Closure Trace**: Always write out full candidate keys before declaring a functional dependency $X \\to Y$ violates 3NF or BCNF.\n");
        sb.append("3. **Active Recall over Passive Rereading**: Close your notes and write the Dijkstra adjacency matrix updates from memory.\n\n");

        sb.append("### 4. Adaptive Adjustments for Missed Sessions\n");
        if (!missedSessions.isEmpty()) {
            sb.append(String.format("You have %d unfinished study session(s). The scheduler has queued makeup slots in your upcoming evening buffer hours (18:00 - 19:30). Do not double-book—stick to the revised timetable.\n\n", missedSessions.size()));
        } else {
            sb.append("All scheduled study slots are current. Excellent execution discipline!\n\n");
        }

        sb.append("### 5. Exam Day Psychological & Memory Retention Tips\n");
        sb.append("- **The 10-Minute Brain Dump**: Upon receiving the question paper, jot down formulas and recurrence relations on scratch paper immediately.\n");
        sb.append("- **Hydration & Sleep**: Short-term memory consolidation occurs during REM sleep. Ensure 7+ hours of sleep before exam day.");

        return sb.toString();
    }

    private String generateFallbackTopicExplanation(String topicName, String subjectName, String difficulty, String style, String systemNote) {
        StringBuilder sb = new StringBuilder();
        if (systemNote != null) {
            sb.append("> ℹ️ *").append(systemNote).append("*\n\n");
        }

        sb.append(String.format("## Study Guide: %s\n", topicName));
        sb.append(String.format("**Subject:** %s | **Difficulty:** %s | **Perspective:** %s\n\n",
                subjectName, difficulty, style != null ? style : "Comprehensive"));

        sb.append("### 1. Intuitive Analogy\n");
        if (topicName.toLowerCase().contains("tree") || topicName.toLowerCase().contains("avl")) {
            sb.append("Imagine an AVL tree as a self-balancing acrobat. Whenever an acrobat feels heavier on one shoulder by more than 1 unit of weight, they immediately perform a quick rotation (Left or Right) so they never lose balance and topple over. This guarantees that finding any element never takes longer than \\(O(\\log n)\\).\n\n");
        } else if (topicName.toLowerCase().contains("dynamic") || topicName.toLowerCase().contains("memoization")) {
            sb.append("Think of Dynamic Programming like writing on a notepad. If someone asks you: *'What is 1 + 1 + 1 + 1?'*, you count and say 4. If they then add another '+ 1' to the end, you don't re-count all the ones from the beginning—you remember the 4 from your notepad and simply add 1 to get 5. That is memoization!\n\n");
        } else if (topicName.toLowerCase().contains("normal") || topicName.toLowerCase().contains("bcnf")) {
            sb.append("Imagine a messy closet where shirts, socks, grocery lists, and car keys are thrown into one drawer. Finding anything is difficult, and removing a shirt might accidentally toss out your car keys. Database normalization (1NF to BCNF) organizes your closet into dedicated labeled drawers so every fact lives in exactly one place with zero redundancy.\n\n");
        } else {
            sb.append(String.format("Think of **%s** as a specialized blueprint in %s. It isolates complex system operations into modular, predictable rules so you can reason about large-scale problems without being overwhelmed.\n\n", topicName, subjectName));
        }

        sb.append("### 2. Core Concepts & Technical Breakdown\n");
        sb.append(String.format("1. **Fundamental Definition**: %s solves a critical operational bottleneck in %s by structuring state transitions and minimizing computational or storage overhead.\n", topicName, subjectName));
        sb.append("2. **Pre-conditions & Invariants**: Always verify input assumptions before execution. Ensure boundary edge cases (null inputs, zero elements, boundary limits) are handled.\n");
        sb.append("3. **Algorithmic Execution Path**: Divide the challenge into: (a) State representation, (b) Transition or processing logic, and (c) Termination criterion.\n\n");

        sb.append("### 3. Essential Formulas & Rules to Memorize\n");
        sb.append("- **Time & Space Bounds**: Analyze worst-case \\(O(n \\log n)\\) vs average-case \\(O(n)\\).\n");
        sb.append("- **Invariant Condition**: For balanced structures, balance factor \\(BF = \\text{height}(L) - \\text{height}(R) \\in \\{-1, 0, 1\\}\\).\n");
        sb.append("- **Decomposition Rule**: Ensure lossless join condition \\(R_1 \\cap R_2 \\to R_1\\) or \\(R_1 \\cap R_2 \\to R_2\\).\n\n");

        sb.append("### 4. Common Exam Pitfalls & Misconceptions\n");
        sb.append("- **Trap 1**: Forgetting to update heights/pointers after tree rotations.\n");
        sb.append("- **Trap 2**: Confusing overlapping subproblems with independent subproblems.\n");
        sb.append("- **Trap 3**: Assuming 3NF implies BCNF without verifying the strict candidate key condition \\(X\\) is a superkey for every non-trivial \\(X \\to Y\\).\n\n");

        sb.append("### 5. Quick Check for Understanding\n");
        sb.append("1. **Q**: What happens to efficiency if the balance property is violated?\n");
        sb.append("   - **A**: The tree degenerates towards a linked list, degrading search time from \\(O(\\log n)\\) to \\(O(n)\\).\n");
        sb.append("2. **Q**: How do you avoid infinite recursion in top-down memoization?\n");
        sb.append("   - **A**: By ensuring the recursion base cases are checked before consulting the lookup table.");

        return sb.toString();
    }

    private List<QuizQuestion> generateFallbackQuiz(String topicName, String subjectName, String difficulty) {
        List<QuizQuestion> questions = new ArrayList<>();

        if (topicName.toLowerCase().contains("avl") || topicName.toLowerCase().contains("tree")) {
            questions.add(new QuizQuestion(1, "What is the maximum permissible balance factor in an AVL Tree for any node?",
                    List.of("-1, 0, or 1", "-2, -1, 0, 1, or 2", "Strictly 0", "Any positive integer"), 0,
                    "In an AVL tree, the balance factor is defined as height(Left) - height(Right), which must remain in {-1, 0, 1} at all times."));
            questions.add(new QuizQuestion(2, "Which rotation is required when a node is inserted into the right subtree of the left child of an unbalanced node?",
                    List.of("Left-Left (LL) Single Rotation", "Left-Right (LR) Double Rotation", "Right-Right (RR) Single Rotation", "Right-Left (RL) Double Rotation"), 1,
                    "An insertion into the right subtree of a left child causes an LR imbalance, resolved by an LR double rotation (Left rotate child, Right rotate parent)."));
            questions.add(new QuizQuestion(3, "What is the worst-case time complexity of searching for an element in an AVL tree with n nodes?",
                    List.of("O(1)", "O(log n)", "O(n)", "O(n log n)"), 1,
                    "Because AVL trees maintain strict height balance h <= 1.44 log2(n), search operations take O(log n) time in the worst case."));
            questions.add(new QuizQuestion(4, "How is the height of an empty tree typically defined in AVL balance calculations?",
                    List.of("-1", "0", "1", "Infinity"), 0,
                    "By standard convention, an empty tree has height -1, while a tree with a single root node has height 0."));
            questions.add(new QuizQuestion(5, "What is the primary advantage of Red-Black trees over AVL trees?",
                    List.of("Faster search lookups", "Fewer rotations during frequent insertions and deletions", "Lower memory consumption per node", "Simpler balancing rules"), 1,
                    "AVL trees are more rigidly balanced, making searches faster, but Red-Black trees require fewer rotations during heavy insert/delete workloads."));
        } else if (topicName.toLowerCase().contains("dynamic") || topicName.toLowerCase().contains("knapsack")) {
            questions.add(new QuizQuestion(1, "What two fundamental properties must a problem have to be suitable for Dynamic Programming?",
                    List.of("Greedy choice property & Divide-and-conquer", "Optimal substructure & Overlapping subproblems", "Linear time complexity & Monotonicity", "Asymptotic bound & Recurrence invariance"), 1,
                    "Dynamic programming applies when the problem exhibits optimal substructure (optimal solution contains optimal sub-solutions) and overlapping subproblems."));
            questions.add(new QuizQuestion(2, "In the 0/1 Knapsack problem with n items and capacity W, what is the standard DP time complexity?",
                    List.of("O(n + W)", "O(n * W)", "O(2^n)", "O(W log n)"), 1,
                    "The standard dynamic programming solution constructs a table of size (n+1) x (W+1), requiring O(n * W) time (pseudo-polynomial)."));
            questions.add(new QuizQuestion(3, "What is the difference between Memoization and Tabulation?",
                    List.of("Memoization is bottom-up; Tabulation is top-down", "Memoization is top-down (recursive); Tabulation is bottom-up (iterative)", "Memoization uses less space than Tabulation always", "There is no functional difference"), 1,
                    "Memoization solves subproblems top-down on-demand with caching; Tabulation solves all subproblems systematically bottom-up into a table."));
            questions.add(new QuizQuestion(4, "What is the space-optimized auxiliary space needed for Fibonacci sequence calculation using DP?",
                    List.of("O(n)", "O(1)", "O(log n)", "O(n^2)"), 1,
                    "Since each Fibonacci number only depends on the previous two values, you only need 2 variables, achieving O(1) auxiliary space."));
            questions.add(new QuizQuestion(5, "Which problem can NOT be solved with the greedy approach but requires Dynamic Programming for an exact solution?",
                    List.of("Fractional Knapsack", "0/1 Knapsack", "Huffman Coding", "Kruskal Minimum Spanning Tree"), 1,
                    "Fractional Knapsack can be solved greedily by value/weight ratio, but 0/1 Knapsack requires Dynamic Programming because items cannot be split."));
        } else if (topicName.toLowerCase().contains("bcnf") || topicName.toLowerCase().contains("normal")) {
            questions.add(new QuizQuestion(1, "A relation R is in Boyce-Codd Normal Form (BCNF) if for every non-trivial functional dependency X -> Y:",
                    List.of("Y is a prime attribute", "X is a superkey", "X is a primary key only", "Y is a subset of X"), 1,
                    "BCNF requires that for every non-trivial functional dependency X -> Y, the determinant X must be a superkey of the relation."));
            questions.add(new QuizQuestion(2, "Which statement is true regarding 3NF vs BCNF decomposition?",
                    List.of("BCNF always preserves functional dependencies; 3NF does not", "3NF guarantees both lossless join and dependency preservation; BCNF guarantees lossless join but may not preserve dependencies", "BCNF is weaker than 3NF", "Every relation in 3NF is automatically in BCNF"), 1,
                    "3NF synthesis guarantees both lossless join and dependency preservation, whereas BCNF decomposition guarantees lossless join but can sometimes sacrifice functional dependencies."));
            questions.add(new QuizQuestion(3, "A relation that has no multi-valued attributes and all non-key attributes fully dependent on the primary key is at least in:",
                    List.of("1NF", "2NF", "3NF", "4NF"), 1,
                    "1NF eliminates atomic repeating groups; 2NF further eliminates partial dependencies where non-prime attributes depend on a proper subset of candidate keys."));
            questions.add(new QuizQuestion(4, "If relation R(A, B, C) has functional dependencies A -> B and B -> C with candidate key A, what anomaly exists?",
                    List.of("Partial dependency", "Transitive dependency", "Multi-valued dependency", "No anomaly exists"), 1,
                    "Since A -> B and B -> C, the non-prime attribute C transitively depends on the candidate key A via non-key B, violating 3NF."));
            questions.add(new QuizQuestion(5, "What algorithm is used to determine whether an attribute set X is a superkey?",
                    List.of("Attribute Closure Algorithm (X+)", "Dijkstra algorithm", "Floyd-Warshall algorithm", "Kruskal algorithm"), 0,
                    "The attribute closure algorithm computes X+ under F; if X+ contains all attributes of R, then X is a superkey."));
        } else {
            // General Computer Science & Engineering Quiz
            questions.add(new QuizQuestion(1, String.format("What is the primary objective of studying '%s' in %s?", topicName, subjectName),
                    List.of("To optimize performance and resource utilization", "To increase code length", "To remove algorithmic verification", "To bypass theoretical correctness"), 0,
                    String.format("%s focuses on establishing correctness and optimizing execution efficiency in %s.", topicName, subjectName)));
            questions.add(new QuizQuestion(2, "What does asymptotic time complexity O(g(n)) mathematically represent?",
                    List.of("The exact execution time in milliseconds", "The upper bound on growth rate as input size approaches infinity", "The memory size in kilobytes", "The minimum hardware clock speed"), 1,
                    "Big-O notation characterizes the upper bound on the growth rate of runtime as input size n grows asymptotically large."));
            questions.add(new QuizQuestion(3, "Which strategy is most effective when encountering a complex multi-variable problem in this topic?",
                    List.of("Decomposition into smaller independent subproblems", "Trial and error brute force", "Ignoring edge constraints", "Hardcoding specific outcomes"), 0,
                    "Divide-and-conquer and modular decomposition allow structured reasoning and formal proofs of correctness."));
            questions.add(new QuizQuestion(4, "What is an invariant condition in algorithmic execution?",
                    List.of("A condition that changes randomly at each step", "A condition that remains true before and after each iteration of a loop", "A compiler error warning", "A variable stored in global registers"), 1,
                    "A loop invariant is a formal predicate that holds true prior to initialization, is maintained through each iteration, and proves correctness upon termination."));
            questions.add(new QuizQuestion(5, "How should space-time tradeoffs typically be evaluated in system design?",
                    List.of("Space is always cheaper than time in all environments", "By benchmarking target hardware constraints and throughput requirements", "By maximizing cache misses", "Tradeoffs are never necessary in modern architectures"), 1,
                    "Architectural tradeoffs must be evaluated against memory limits, cache locality, and latency SLA targets."));
        }

        return questions;
    }
}
