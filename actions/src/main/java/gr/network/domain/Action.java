package gr.network.domain;

/**
 * Represents an action edge in the graph.
 * Each action is associated with a user and a course.
 * @version 1.0
 */
public record Action(
    String action,
    String user,
    String course,
    String timestamp,
    double feature0,
    double feature1,
    double feature2,
    double feature3,
    int label
) {};