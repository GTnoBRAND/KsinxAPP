package org.jas.ksinxapp.dtos;

public record CourseProgressResponse(
        Long studentId,
        Long courseId,
        String courseTitle,
        int totalTasks,
        int totalCompletedTasks,
        double completionPercentage
) {
}
