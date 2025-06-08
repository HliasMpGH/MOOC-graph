# Performance Comparison: Neo4j vs SQLite

## Overview
This document contains the results of a comprehensive performance comparison between Neo4j graph database and SQLite relational database for MOOC (Massive Open Online Course) data queries.

## Test Results

### 1. Graph Size Query
**Purpose**: Get total counts of users, courses, and actions

| Database | Users | Courses | Actions | Execution Time |
|----------|-------|---------|---------|----------------|
| Neo4j    | 7,047 | 97      | 411,749 | 235,42 ms      |
| SQLite   | 7,047 | 97      | 411,749 | 9,00 ms        |

**Performance**: SQLite is **26,15x** faster (226,42 ms difference)

### 2. Actions Per User Query
**Purpose**: Count total actions for each user (top 10 results)

| Database | Execution Time | Top Result |
|----------|----------------|----------------|
| Neo4j    | 4,29 ms        | userId: "0"     totalActions: 76 |
| SQLite   | 166,71 ms      | userId: "0"     totalActions: 76 |

**Performance**: Neo4j is **38,85x** faster (162,42 ms difference)

### 3. Top Targets Query
**Purpose**: Find top 10 targets by distinct user count

| Database | Execution Time | Top Target |
|----------|----------------|------------|
| Neo4j    | 3,88 ms        | targetID: "1"   userCount: 6695 |
| SQLite   | 262,78 ms      | targetID: "1"   userCount: 6695 |

**Performance**: Neo4j is **67,79x** faster (258,90 ms difference)

### 4. Average Actions Query
**Purpose**: Calculate average action per user

| Database | Execution Time | Average Action |
|----------|----------------|----------------|
| Neo4j    | 11,16 ms       | 58.43          |
| SQLite   | 158,90 ms      | 58.43          |

**Performance**: Neo4j is **14,24x faster** (147,74 ms difference)

### 5. Positive Feature2 Query
**Purpose**: Find user/target pairs where Feature2 > 0

| Database | Execution Time | Top Result |
|----------|----------------|----------------|
| Neo4j    | 4,14 ms        | userID: "0"     targetID: "0" |
| SQLite   | 130,25         | userID: "0"     targetID: "0" |

**Performance**: Neo4j is **31,48x** faster (126,12 ms difference)

### 6. Label=1 Per Target Query
**Purpose**: Count actions with label=1 for each target

| Database | Execution Time | Top Result |
|----------|----------------|------------|
| Neo4j    | 5,07 ms        | targetID: "13"  labelOneCount: 360 |
| SQLite   | 28,18 ms       | targetID: "13"  labelOneCount: 360 |

**Performance**: Neo4j is **5,55x** faster (23,11 ms difference)

### 7. Actions & Targets of User Query (User ID: 0)
**Purpose**: Get actions and targets for a specific user

| Database | Execution Time | Top Result |
|----------|----------------|---------------|
| Neo4j    | 8,47 ms        | actionId: "0"   courseID: "0"    |
| SQLite   | 0,23 ms        | actionId: "0"   courseID: "0"    |

**Performance**: SQLite is **36,66x** faster (8,24 ms difference)

## Notes

1. As all the queries were run in a sequence, the execution of the first query (Neo4j user count) had a time overhead (session and connection initialization etc..).

2. The SQLite queries were executed using prepared statements and thus have an advantage because of the pre-compilation.

3. Indices were used only on Neo4j.

4. Each query was run 3 times and the results present the average times.

5. All the queries were run on a machine with 16GB of RAM, CPU: 11th Gen Intel(R) Core(TM) i7-1165G7 @ 2.80GHz 2.70 GHz and 64bit OS
