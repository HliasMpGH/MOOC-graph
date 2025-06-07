# Performance Comparison: Neo4j vs SQLite

## Overview
This document contains the results of a comprehensive performance comparison between Neo4j graph database and SQLite relational database for MOOC (Massive Open Online Course) data queries.

## Test Results

### 1. Graph Size Query
**Purpose**: Get total counts of users, courses, and actions

| Database | Users | Courses | Actions | Execution Time |
|----------|-------|---------|---------|----------------|
| Neo4j    | 7,047 | 97      | 823,498 | 186.71 ms      |
| SQLite   | 7,047 | 97      | 411,749 | 9.43 ms        |

**Performance**: SQLite is **19.80x faster** (177.28 ms difference)

*Note: Different action counts suggest potential data inconsistency between databases*

### 2. Actions Per User Query
**Purpose**: Count total actions for each user (top 10 results)

| Database | Execution Time | Sample Results |
|----------|----------------|----------------|
| Neo4j    | 108.40 ms      | User "0": 152 actions, User "1": 52 actions |
| SQLite   | 176.57 ms      | User "0": 76 actions, User "1": 26 actions |

**Performance**: Neo4j is **1.63x faster** (68.18 ms difference)

*Note: Significant discrepancy in action counts between databases*

### 3. Top Targets Query
**Purpose**: Find top 10 targets by distinct user count

| Database | Execution Time | Top Target |
|----------|----------------|------------|
| Neo4j    | 97.71 ms       | Target "1": 6,695 users |
| SQLite   | 409.67 ms      | Target "1": 6,695 users |

**Performance**: Neo4j is **4.19x faster** (311.96 ms difference)

### 4. Average Actions Query
**Purpose**: Calculate average actions per user

| Database | Average Actions | Execution Time |
|----------|----------------|----------------|
| Neo4j    | 116.86         | 76.44 ms       |
| SQLite   | 58.43          | 208.92 ms      |

**Performance**: Neo4j is **2.73x faster** (132.48 ms difference)

*Note: Significant difference in calculated averages*

### 5. Positive Feature2 Query
**Purpose**: Find user/target pairs where Feature2 > 0

| Database | Execution Time | Sample Results |
|----------|----------------|----------------|
| Neo4j    | 180.76 ms      | User "1181", Target "39" |
| SQLite   | 0.38 ms        | User "6123", Target "8" |

**Performance**: SQLite is **481.89x faster** (180.38 ms difference)

### 6. Label=1 Per Target Query
**Purpose**: Count actions with label=1 for each target

| Database | Execution Time | Top Result |
|----------|----------------|------------|
| Neo4j    | 86.74 ms       | Target "13": 720 actions |
| SQLite   | 50.79 ms       | Target "13": 360 actions |

**Performance**: SQLite is **1.71x faster** (35.95 ms difference)

*Note: Neo4j shows exactly double the counts*

### 7. User-Specific Query (User ID: 0)
**Purpose**: Get actions and targets for a specific user

| Database | Execution Time | Results Count |
|----------|----------------|---------------|
| Neo4j    | 81.95 ms       | 10 records    |
| SQLite   | 14.59 ms       | 10 records    |

**Performance**: SQLite is **5.62x faster**