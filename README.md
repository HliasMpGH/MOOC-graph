# Graph Loader & Query Tool

This Java application allows you to load a graph into Neo4j and SQLite from a CSV file and execute predefined queries using command-line arguments. The purpose of this tool is to measure performance between a relational and a graph database on the same queries.

## Build
```bash
cd /actions

mvn clean install
```

## Setup
Set up a Neo4j DB on your local machine and provide its credentials in [`actions/.env.example`](actions/.env.example) and change its name to `actions/.env`

## Usage

- `java -jar graph-tool-jar-with-dependencies.jar <args>`

## Argument Options

- `--load`
Load the graph from the default CSV file (`mooc_actions_merged.csv`).

- `--load <path_to_csv_file>`
Load the graph from a specified CSV file.

- `--query <query_alias>`
Run a predefined query on the graph. The alias must match a query defined in the code.

- `--query`
Run queries interactively (prompts for query selection).

- `--sql <query_alias>`
Run SQL queries only for the specified query alias.

- `--sql`
Run SQL queries only in interactive mode.

- `--compare <query_alias>`
Compare Neo4j vs SQLite performance for the specified query.

- `--compare`
Compare Neo4j vs SQLite performance in interactive mode.

- `--load --query <query_name>`
Load the graph (from default file) and run the specified query.

- `--load --compare <query_name>`
Load the graph (from default file) and run performance comparison.

- `--load <path_to_csv_file> --query <query_name>`
Load the graph from a specified file and query it with the given query name.

- `--load <path_to_csv_file> --compare <query_name>`
Load the graph from a specified file and run performance comparison.

- `[NO ARGS]`
Run interactively and choose a query (includes comparison options).

## Perform Complete Interactive Benchmarking

```bash
# Load the data in both the databases (SQLite & Neo4j) and execute on interactive mode
java -jar graph-tool-jar-with-dependencies.jar --load
```

And choose option **9: compareall - Compare all queries performance (Neo4j vs SQLite)**