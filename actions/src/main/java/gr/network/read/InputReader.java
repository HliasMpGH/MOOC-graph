package gr.network.read;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;

import gr.network.domain.Action;

/**
 * Reads TSV files in-memory to
 * provide data to be passed to
 * the Graphloader.
 * @version 1.0
 */
public class InputReader {

    private Set<Action> actions;
    private Set<String> userIds;
    private Set<String> courseIds;

    public InputReader() throws Exception {
        this.actions = this.getActionsFromCsv();
        this.userIds = this.extractUserIds(actions);
        this.courseIds = this.extractCourseIds(actions);
    }

    public Set<Action> getActionsFromCsv() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream("mooc_actions_merged.csv")) {
            if (inputStream == null) {
                throw new FileNotFoundException("mooc_actions_merged.csv not found in resources");
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            // create mapping strategy
            HeaderColumnNameMappingStrategy<Action> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(Action.class);

            // build CSV to Bean converter
            CsvToBean<Action> csvToBean = new CsvToBeanBuilder<Action>(inputStreamReader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();

            // inputStreamReader.close();
            // parse and return the list
            return new HashSet<>(csvToBean.parse());
        }
    }

    public Set<String> extractUserIds(Set<Action> actions) {
        return actions.stream()
            .map(Action::getUser)
            .collect(Collectors.toSet());
    }

    public Set<String> extractCourseIds(Set<Action> actions) {
        return actions.stream()
            .map(Action::getCourse)
            .collect(Collectors.toSet());
    }

    public Set<Action> getActions() {
        return actions;
    }

    public Set<String> getUserIds() {
        return userIds;
    }

    public Set<String> getCourseIds() {
        return courseIds;
    }
}
