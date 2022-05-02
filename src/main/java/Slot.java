import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class Slot {

    private int number;
    private double lowerBound, higherBound;
    private double middle;
    private long frequency;
    private double relativeFrequency;
    private double relativeFrequencyDensity;

    @Override
    public String toString() {
        return number + " " + lowerBound
            + " " + higherBound
            + " " + middle
            + " " + frequency
            + " " + relativeFrequency
            + " " + relativeFrequencyDensity;
    }
}
