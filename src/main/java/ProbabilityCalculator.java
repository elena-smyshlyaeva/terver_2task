import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProbabilityCalculator {

    public static Map<Double, Double> calculateProbability(List<Double> bounds, List<Double> sequence) {
        Map<Double, Double> boundProbability = new HashMap<>();
        int n = sequence.size();
        for (Double bound : bounds) {
            long repeats = sequence.stream()
                .filter(element -> element <= bound)
                .count();

            boundProbability.put(bound, (double)repeats /(double)n);
        }

        return boundProbability;
    }
}
