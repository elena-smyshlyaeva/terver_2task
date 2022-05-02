import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Application {

    private static final int INTERVAL_COUNT = 9;
    private static final double GAMMA = 0.9;
    private static final double T = 1.645;

    private static double sigma;
    private static double x;
    private static double d;
    private static double dv;
    private static double slotLength;

    private static FileWriter fileWriter;
    private static FileReader fileReader;
    private static Sequence sequence = new Sequence();

    private static List<Double> variationSequence;
    private static List<Slot> slots;

    private static int n;

    public static void main(String[] args) throws IOException {
        fileReader = new FileReader("input.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        int i = 0;
        String line = bufferedReader.readLine();
        while (line != null) {
            System.out.println(line);
            System.out.println("-----");
            fileWriter = new FileWriter("var" + ++i + ".txt");

            sequence.separate(line, " ");
            aTask();
            fileWriter.write("номер границы середина_интервала частота относ_частота плотность_относ_частоты");
            b_cTasks();
            gTask();
            dTask();
            eTask();

            fileWriter.close();
            line = bufferedReader.readLine();
        }
    }

    private static void eTask() throws IOException {
        dv = (double)n / (double)(n - 1) * d;
        double sigmaV = Math.sqrt(dv);

        double beta = sigmaV / Math.sqrt(10) * T;
        fileWriter.write("доверительный интервал: (" + (x - beta) + "; " + (x + beta) + ")\n");
    }

    private static void dTask() throws IOException {
        Set<Double> values = new HashSet<>(variationSequence);

        List<Double> u = values.stream()
            .map(e -> (e - x) / (sigma))
            .collect(Collectors.toList());

        List<Double> laplaceFunctionValue = u.stream()
            .map(Application::getLaplaceFunctionValue)
            .collect(Collectors.toList());

        double factor = (n * slotLength) / sigma;
        List<Double> theoreticalFrequencies = calculateTheoreticalFrequencies(factor, laplaceFunctionValue);
        List<Integer> actualFrequencies = calculateActualFrequencies(variationSequence);
        double criteria = findCriteria(theoreticalFrequencies, actualFrequencies);
        int libertyDegrees = theoreticalFrequencies.size() - 3;
        fileWriter.write("X^2набл.=" + criteria + "\nстепени свободы=" + libertyDegrees + "\n");
    }

    private static double findCriteria(List<Double> theoreticalFrequencies, List<Integer> actualFrequencies) {
        double sum = 0D;
        for (int i = 0; i < actualFrequencies.size(); i++) {
            int actual = actualFrequencies.get(i);
            double theoretical = theoreticalFrequencies.get(i);

            sum += Math.pow(actual - theoretical, 2) / theoretical;
        }

        return sum;
    }

    private static List<Integer> calculateActualFrequencies(List<Double> variationSequence) {
        List<Integer> frequencies = new ArrayList<>();
        for (int i = 0; i < variationSequence.size() - 1; i++) {
            int count = 1;
            while (i < variationSequence.size() - 1 && variationSequence.get(i).equals(variationSequence.get(i + 1))) {
                count++;
                i++;
            }
            frequencies.add(count);
        }

        return frequencies;
    }

    private static List<Double> calculateTheoreticalFrequencies(Double factor, List<Double> laplaceFunctionValue) {
        List<Double> theoreticalFrequencies = laplaceFunctionValue.stream()
            .map(e -> factor * e)
            .collect(Collectors.toList());

        return theoreticalFrequencies;
    }

    private static double getLaplaceFunctionValue(double x) {
        return (1/(Math.sqrt(2*Math.PI)))*Math.exp(-((x*x)/2));
    }

    private static void gTask() throws IOException {
        fileWriter.write("Xв=" + findX() + "\n");
        fileWriter.write("Dв=" + findDispersion() + "\n");
    }

    private static double findX() {
        List<Double> frequencyOnMiddleSequence = slots.stream()
            .map(slot -> slot.getFrequency() * slot.getMiddle())
            .collect(Collectors.toList());

        double xv = frequencyOnMiddleSequence.stream().mapToDouble(e -> e).sum() / n;
        x = xv;
        return xv;
    }

    private static double findDispersion() {
        List<Double> middleSquareOnFrequencySequence = slots.stream()
            .map(slot -> slot.getMiddle() * slot.getMiddle() * slot.getFrequency())
            .collect(Collectors.toList());

        double dispersion = middleSquareOnFrequencySequence.stream().mapToDouble(e -> e).sum() / n;
        sigma = Math.sqrt(dispersion);
        d = dispersion;
        return dispersion;
    }

    private static void b_cTasks() {
        slotLength = calculateSlotLength(variationSequence.get(0), variationSequence.get(n - 1));
        slots = splitToIntervals(slotLength);

        List<Double> bounds = slots.stream().map(Slot::getLowerBound).collect(Collectors.toList());
        bounds.add(slots.get(slots.size() - 1).getHigherBound());

        Map<Double, Double> probability = ProbabilityCalculator.calculateProbability(bounds, variationSequence);
        probability.forEach((key, value) -> {
            try {
                fileWriter.write(key + "=" + value + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static List<Slot> splitToIntervals(Double slotLength) {
        List<Slot> slots = new ArrayList<>();
        int i = 0;
        double lowerBound;
        double higherBound = variationSequence.get(0);;
        while (i < INTERVAL_COUNT) {
            Slot slot = new Slot();
            lowerBound = higherBound;
            higherBound += slotLength;
            double middle = (higherBound + lowerBound) / 2;
            long frequency = frequencyForSlot(lowerBound, higherBound);
            double relativeFrequency = (double) frequency / n;
            double relativeFrequencyDensity = relativeFrequency / slotLength;

            slot.setNumber(i+1);
            slot.setLowerBound(lowerBound);
            slot.setHigherBound(higherBound);
            slot.setMiddle(middle);
            slot.setFrequency(frequency);
            slot.setRelativeFrequency(relativeFrequency);
            slot.setRelativeFrequencyDensity(relativeFrequencyDensity);

            slots.add(slot);
            i++;
        }

        writeSlots(slots);
        return slots;
    }

    private static void writeSlots(List<Slot> slots) {
        slots.forEach(slot -> {
            try {
                fileWriter.write(slot.toString() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static long frequencyForSlot(double lowerBound, double higherBound) {
        return variationSequence.stream()
            .filter(e -> e >= lowerBound && e <= higherBound)
            .count();
    }

    private static void aTask() {
        variationSequence = sequence.getVariationSequence();
        writeVariationSequence(variationSequence);
    }

    private static void writeVariationSequence(List<Double> variationSequence) {
        AtomicInteger count = new AtomicInteger();
        variationSequence.forEach(e -> {
            count.getAndIncrement();
            try {
                fileWriter.write(e.toString());
                if (count.get() % 10 == 0) {
                    fileWriter.write("\n");
                } else {
                    fileWriter.write(" ");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        n = count.get();
    }

    private static Double calculateSlotLength(Double min, Double max) {
        return (max - min) / INTERVAL_COUNT;
    }
}
