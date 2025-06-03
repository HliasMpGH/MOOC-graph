CREATE TABLE IF NOT EXISTS Users(
    userId TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS Courses(
    courseId TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS Actions(
    actionId TEXT PRIMARY KEY,
    userId TEXT,
    courseId TEXT,
    tmsmp DATETIME,
    label INTEGER,
    feature0 REAL,
    feature1 REAL,
    feature2 REAL,
    feature3 REAL,
    FOREIGN KEY (userId) REFERENCES Users(userId),
    FOREIGN KEY (courseId) REFERENCES Courses(courseId)
);