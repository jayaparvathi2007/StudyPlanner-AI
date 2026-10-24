package com.studyplanner.service;

import com.studyplanner.dao.ExamDAO;
import com.studyplanner.dao.StudySessionDAO;
import com.studyplanner.dao.TopicDAO;
import com.studyplanner.model.Exam;
import com.studyplanner.model.StudySession;
import com.studyplanner.model.Topic;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

/**
 * Intelligent Core Java Study Scheduler and Adaptive Rescheduling Engine.
 * Schedules study sessions using:
 * 1. Exam deadline proximity (urgency weight)
 * 2. Topic difficulty & estimated hours
 * 3. Confidence level deficit & weak topic flags
 * 4. Cognitive spaced interleaving (alternating subjects for higher retention)
 */
public class SchedulerService {

    private static final Logger LOGGER = Logger.getLogger(SchedulerService.class.getName());

    private final TopicDAO topicDAO;
    private final ExamDAO examDAO;
    private final StudySessionDAO sessionDAO;

    public SchedulerService() {
        this.topicDAO = new TopicDAO();
        this.examDAO = new ExamDAO();
        this.sessionDAO = new StudySessionDAO();
    }

    public SchedulerService(TopicDAO topicDAO, ExamDAO examDAO, StudySessionDAO sessionDAO) {
        this.topicDAO = topicDAO;
        this.examDAO = examDAO;
        this.sessionDAO = sessionDAO;
    }

    /**
     * Internal container for scoring topic scheduling priority.
     */
    public static class PrioritizedTopic {
        private final Topic topic;
        private final double priorityScore;
        private final LocalDate nearestExamDate;

        public PrioritizedTopic(Topic topic, double priorityScore, LocalDate nearestExamDate) {
            this.topic = topic;
            this.priorityScore = priorityScore;
            this.nearestExamDate = nearestExamDate;
        }

        public Topic getTopic() { return topic; }
        public double getPriorityScore() { return priorityScore; }
        public LocalDate getNearestExamDate() { return nearestExamDate; }
    }

    /**
     * Generates a new daily or weekly timetable starting from startDate for daysAhead days.
     */
    public List<StudySession> generateTimetable(int studentId, LocalDate startDate, int daysAhead, double dailyHours, boolean clearFuture) {
        if (daysAhead <= 0) daysAhead = 7;
        if (dailyHours <= 0) dailyHours = 3.0;

        if (clearFuture) {
            sessionDAO.clearFutureScheduledSessions(studentId, startDate);
        }

        List<Topic> allTopics = topicDAO.getAll(studentId);
        List<Exam> upcomingExams = examDAO.getUpcoming(studentId);

        // Map subjectId to nearest exam date
        Map<Integer, LocalDate> subjectToExamDate = new HashMap<>();
        for (Exam exam : upcomingExams) {
            if (exam.getExamDate() != null) {
                LocalDate ed = exam.getExamDate().toLocalDate();
                if (!subjectToExamDate.containsKey(exam.getSubjectId()) || ed.isBefore(subjectToExamDate.get(exam.getSubjectId()))) {
                    subjectToExamDate.put(exam.getSubjectId(), ed);
                }
            }
        }

        // Filter for topics requiring study (not yet completed, or weak topics needing review)
        List<PrioritizedTopic> prioritized = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Topic t : allTopics) {
            if ("COMPLETED".equalsIgnoreCase(t.getStatus()) && !t.isWeak()) {
                continue; // Skip fully mastered completed topics
            }

            LocalDate examDate = subjectToExamDate.get(t.getSubjectId());
            long daysToExam = (examDate != null) ? java.time.temporal.ChronoUnit.DAYS.between(today, examDate) : 30;
            if (daysToExam < 0) daysToExam = 30;

            // 1. Exam Proximity Factor
            double urgency;
            if (daysToExam <= 3) urgency = 5.0;
            else if (daysToExam <= 7) urgency = 4.0;
            else if (daysToExam <= 14) urgency = 2.5;
            else if (daysToExam <= 30) urgency = 1.5;
            else urgency = 1.0;

            // 2. Topic Difficulty Factor
            double diffFactor;
            String diff = t.getDifficulty() != null ? t.getDifficulty().toUpperCase() : "MEDIUM";
            switch (diff) {
                case "HARD": diffFactor = 3.0; break;
                case "EASY": diffFactor = 1.0; break;
                default: diffFactor = 2.0; break;
            }

            // 3. Confidence Deficit Factor (1-5 scale)
            int conf = (t.getConfidenceLevel() >= 1 && t.getConfidenceLevel() <= 5) ? t.getConfidenceLevel() : 3;
            double confDeficit = 6.0 - conf; // 1 -> 5.0, 5 -> 1.0

            // 4. Boosts
            double weakBoost = t.isWeak() ? 4.0 : 0.0;
            double inProgressBoost = "IN_PROGRESS".equalsIgnoreCase(t.getStatus()) ? 2.0 : 0.0;

            // Weighted composite priority score
            double totalScore = (urgency * 1.5) + (diffFactor * confDeficit * 1.2) + weakBoost + inProgressBoost;
            prioritized.add(new PrioritizedTopic(t, totalScore, examDate));
        }

        // Sort descending by priority score
        prioritized.sort((a, b) -> Double.compare(b.getPriorityScore(), a.getPriorityScore()));

        // Standard time slots throughout the day
        Time[] standardSlots = new Time[] {
                Time.valueOf("09:00:00"),
                Time.valueOf("11:00:00"),
                Time.valueOf("14:30:00"),
                Time.valueOf("16:30:00"),
                Time.valueOf("19:00:00")
        };

        // Determine how many sessions per day based on daily target hours
        int sessionsPerDay = (int) Math.round(dailyHours);
        if (sessionsPerDay < 1) sessionsPerDay = 1;
        if (sessionsPerDay > 4) sessionsPerDay = 4;

        List<StudySession> newSessions = new ArrayList<>();
        if (prioritized.isEmpty()) {
            return newSessions;
        }

        int topicIndex = 0;
        int lastSubjectId = -1;

        for (int dayOffset = 0; dayOffset < daysAhead; dayOffset++) {
            LocalDate currentDate = startDate.plusDays(dayOffset);

            for (int slotIdx = 0; slotIdx < sessionsPerDay; slotIdx++) {
                // Cognitive Interleaving: Find next topic with different subject if possible
                int chosenIdx = topicIndex % prioritized.size();
                if (prioritized.size() > 1 && prioritized.get(chosenIdx).getTopic().getSubjectId() == lastSubjectId) {
                    for (int look = 1; look < prioritized.size(); look++) {
                        int candidate = (topicIndex + look) % prioritized.size();
                        if (prioritized.get(candidate).getTopic().getSubjectId() != lastSubjectId) {
                            chosenIdx = candidate;
                            break;
                        }
                    }
                }

                PrioritizedTopic pt = prioritized.get(chosenIdx);
                Topic top = pt.getTopic();
                lastSubjectId = top.getSubjectId();

                // Check if this topic has already passed its exam date
                if (pt.getNearestExamDate() != null && currentDate.isAfter(pt.getNearestExamDate())) {
                    // Try to pick another topic
                    boolean foundAlternative = false;
                    for (PrioritizedTopic alt : prioritized) {
                        if (alt.getNearestExamDate() == null || !currentDate.isAfter(alt.getNearestExamDate())) {
                            top = alt.getTopic();
                            lastSubjectId = top.getSubjectId();
                            foundAlternative = true;
                            break;
                        }
                    }
                    if (!foundAlternative) {
                        continue;
                    }
                }

                int duration = "HARD".equalsIgnoreCase(top.getDifficulty()) ? 90 : 60;
                Time startTime = standardSlots[slotIdx % standardSlots.length];

                StudySession s = new StudySession();
                s.setStudentId(studentId);
                s.setTopicId(top.getId());
                s.setTopicName(top.getName());
                s.setSubjectId(top.getSubjectId());
                s.setScheduledDate(Date.valueOf(currentDate));
                s.setStartTime(startTime);
                s.setDurationMinutes(duration);
                s.setStatus("SCHEDULED");
                s.setActualDurationMinutes(0);
                s.setRemedial(top.isWeak());
                s.setNotes(top.isWeak() ? "Remedial review session: high priority." : "Scheduled study session.");

                newSessions.add(s);
                topicIndex++;
            }
        }

        sessionDAO.bulkCreate(newSessions);
        return newSessions;
    }

    /**
     * Adaptive Rescheduling Engine:
     * Identifies overdue sessions (< TODAY) still marked SCHEDULED or sessions marked MISSED.
     * Reallocates them into upcoming available timetable slots before the exam date!
     */
    public List<StudySession> rescheduleMissedSessions(int studentId) {
        List<StudySession> missedOrOverdue = sessionDAO.getOverdueMissedSessions(studentId);
        List<StudySession> newlyScheduled = new ArrayList<>();
        if (missedOrOverdue.isEmpty()) {
            return newlyScheduled;
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Exam> upcomingExams = examDAO.getUpcoming(studentId);

        Map<Integer, LocalDate> subjectToExamDate = new HashMap<>();
        for (Exam ex : upcomingExams) {
            if (ex.getExamDate() != null) {
                subjectToExamDate.put(ex.getSubjectId(), ex.getExamDate().toLocalDate());
            }
        }

        Time[] remedialSlots = new Time[] {
                Time.valueOf("18:00:00"),
                Time.valueOf("20:00:00"),
                Time.valueOf("08:00:00")
        };

        int dayOffset = 0;
        int slotIndex = 0;

        for (StudySession overdue : missedOrOverdue) {
            LocalDate targetDate = tomorrow.plusDays(dayOffset);
            LocalDate examDate = subjectToExamDate.get(overdue.getSubjectId());

            // If exam is before target date, push to today evening if possible
            if (examDate != null && targetDate.isAfter(examDate)) {
                targetDate = LocalDate.now();
            }

            StudySession remedial = new StudySession();
            remedial.setStudentId(studentId);
            remedial.setTopicId(overdue.getTopicId());
            remedial.setScheduledDate(Date.valueOf(targetDate));
            remedial.setStartTime(remedialSlots[slotIndex % remedialSlots.length]);
            remedial.setDurationMinutes(overdue.getDurationMinutes() > 0 ? overdue.getDurationMinutes() : 60);
            remedial.setStatus("SCHEDULED");
            remedial.setActualDurationMinutes(0);
            remedial.setRemedial(true);
            remedial.setNotes("Adaptive Rescheduled: Makeup for missed session on " + overdue.getScheduledDate());

            int newId = sessionDAO.create(remedial);
            if (newId > 0) {
                remedial.setId(newId);
                newlyScheduled.add(remedial);
                // Mark original session as RESCHEDULED
                sessionDAO.updateStatus(overdue.getId(), "RESCHEDULED");
            }

            slotIndex++;
            if (slotIndex >= remedialSlots.length) {
                slotIndex = 0;
                dayOffset++;
            }
        }

        return newlyScheduled;
    }

    /**
     * Adaptive response to poor quiz performance:
     * If score < 60%, marks topic as weak and schedules immediate remedial session.
     */
    public boolean handleQuizResult(int studentId, int topicId, double score) {
        if (score < 60.0) {
            // Mark topic weak
            topicDAO.setWeakTopic(topicId, true);

            // Schedule remedial session for tomorrow
            LocalDate targetDate = LocalDate.now().plusDays(1);
            StudySession remedial = new StudySession();
            remedial.setStudentId(studentId);
            remedial.setTopicId(topicId);
            remedial.setScheduledDate(Date.valueOf(targetDate));
            remedial.setStartTime(Time.valueOf("17:30:00"));
            remedial.setDurationMinutes(60);
            remedial.setStatus("SCHEDULED");
            remedial.setRemedial(true);
            remedial.setNotes(String.format("Adaptive Remedial Review: Low quiz score (%.0f%%). Review core formulas & practice.", score));

            sessionDAO.create(remedial);
            return true;
        } else if (score >= 80.0) {
            // Student showed mastery, increase confidence and unmark weak
            Topic t = topicDAO.getById(topicId);
            if (t != null) {
                int newConf = Math.min(5, t.getConfidenceLevel() + 1);
                topicDAO.updateStatusAndConfidence(topicId, "COMPLETED", newConf);
                topicDAO.setWeakTopic(topicId, false);
            }
        }
        return false;
    }
}
