package gr.network.domain;

import com.opencsv.bean.CsvBindByName;

/**
 * Represents an action edge in the graph.
 * Each action is associated with a user and a course.
 * @version 1.0
 */
public class Action {
    @CsvBindByName(column = "ACTIONID")
    private String action;

    @CsvBindByName(column = "USERID")
    private String user;

    @CsvBindByName(column = "TARGETID")
    private String course;

    @CsvBindByName(column = "TIMESTAMP")
    private String timestamp;

    @CsvBindByName(column = "FEATURE0")
    private double feature0;

    @CsvBindByName(column = "FEATURE1")
    private double feature1;

    @CsvBindByName(column = "FEATURE2")
    private double feature2;

    @CsvBindByName(column = "FEATURE3")
    private double feature3;

    @CsvBindByName(column = "LABEL")
    private int label;

    public Action() {}

    public String getAction() { return action; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public double getFeature0() { return feature0; }
    public void setFeature0(double feature0) { this.feature0 = feature0; }

    public double getFeature1() { return feature1; }
    public void setFeature1(double feature1) { this.feature1 = feature1; }

    public double getFeature2() { return feature2; }
    public void setFeature2(double feature2) { this.feature2 = feature2; }

    public double getFeature3() { return feature3; }
    public void setFeature3(double feature3) { this.feature3 = feature3; }

    public int getLabel() { return label; }
    public void setLabel(int label) { this.label = label; }
}
