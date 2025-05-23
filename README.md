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

- `--load --query <query_alias>`
Load the graph (from default file) and run the specified query.

- `--load <path_to_csv_file> --query <query_name>`
Load the graph from a specified file and query it with the given query name;
