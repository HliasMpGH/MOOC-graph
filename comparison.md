# Performance Comparison: Neo4j vs SQLite

## Overview
This document contains the results of a comprehensive performance comparison between Neo4j graph database and SQLite relational database for MOOC (Massive Open Online Course) data queries.

## Test Results

### 1. Graph Size Query
**Purpose**: Get total counts of users, courses, and actions

| Database | Users | Courses | Actions | Execution Time |
|----------|-------|---------|---------|----------------|
| Neo4j    | 7,047 | 97      | 411,749 | 15,03 ms       |
| SQLite   | 7,047 | 97      | 411,749 | 189,79 ms      |

**Performance**: Neo4j is **12,63x** faster (174,76 ms difference)

### 2. Actions Per User Query
**Purpose**: Count total actions for each user (top 10 results)

| Database | Execution Time | Top Result     |
|----------|----------------|----------------|
| Neo4j    | 3,75 ms        | userId: "0"     totalActions: 76 |
| SQLite   | 384,20 ms      | userId: "0"     totalActions: 76 |

**Performance**: Neo4j is **102,54x** faster (380,45 ms difference)

### 3. Top Targets Query
**Purpose**: Find top 10 targets by distinct user count

| Database | Execution Time | Top Target |
|----------|----------------|------------|
| Neo4j    | 3,30 ms        | targetID: "1"   userCount: 6695 |
| SQLite   | 327,79 ms      | targetID: "1"   userCount: 6695 |

**Performance**: Neo4j is **99,23x** faster (324,49 ms difference)

### 4. Average Actions Query
**Purpose**: Calculate average action per user

| Database | Execution Time | Average Action |
|----------|----------------|----------------|
| Neo4j    | 2,80 ms        | 58.43          |
| SQLite   | 192,22 ms      | 58.43          |

**Performance**: Neo4j is **68,66x** faster (189,42 ms difference)

### 5. Positive Feature2 Query
**Purpose**: Find user/target pairs where Feature2 > 0

| Database | Execution Time | Top Result     |
|----------|----------------|----------------|
| Neo4j    | 3,69 ms        | userID: "0"     targetID: "0" |
| SQLite   | 161,51 ms      | userID: "0"     targetID: "0" |

**Performance**: Neo4j is **43,77x** faster (157,82 ms difference)

### 6. Label=1 Per Target Query
**Purpose**: Count actions with label=1 for each target

| Database | Execution Time | Top Result |
|----------|----------------|------------|
| Neo4j    | 2,99 ms        | targetID: "13"  labelOneCount: 360 |
| SQLite   | 29,20 ms       | targetID: "13"  labelOneCount: 360 |

**Performance**: Neo4j is **9,75x** faster (26,20 ms difference)

### 7. Actions & Targets of User Query (User ID: 0)
**Purpose**: Get actions and targets for a specific user

| Database | Execution Time | Top Result |
|----------|----------------|---------------|
| Neo4j    | 2,89 ms        | actionId: "0"   courseID: "0"    |
| SQLite   | 1,34 ms        | actionId: "0"   courseID: "0"    |

**Performance**: SQLite is **2,16x** faster (1,55 ms difference)

## Notes

1. The SQLite queries were executed using prepared statements and thus have an advantage because of the pre-compilation.

2. Indices were used only on Neo4j.

3. Each query was run 3 times and the results present the average times.

4. All the queries were run on a machine with 16GB of RAM, CPU: 11th Gen Intel(R) Core(TM) i7-1165G7 @ 2.80GHz 2.70 GHz and 64bit OS.
