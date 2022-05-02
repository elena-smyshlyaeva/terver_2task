import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Sequence {

    private List<Double> container = new ArrayList<>();

    public void separate(String line, String regex) {
        String[] strings = line.split(regex);
        container = Arrays.stream(strings)
            .map(element -> element.replace(',', '.'))
            .map(Double::parseDouble)
            .collect(Collectors.toList());
    }

    public List<Double> getVariationSequence() {
        return container.stream().sorted().collect(Collectors.toList());
    }

}
