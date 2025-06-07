# Graph Loader & Query Tool

This Java application allows you to load a graph into Neo4j from a CSV file and execute predefined queries using command-line arguments.

## Usage

- `java -jar <jar_name>.jar <args>`

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