package davd33.aoc;

import com.google.common.io.Resources;
import davd33.aoc.domain.ElfStuff;
import io.vavr.collection.List;
import io.vavr.collection.Vector;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.lang.Integer.parseInt;

@Log4j2
public class App {

    public static void main(String[] args) {
        // day 1
        List<ElfStuff> elfStuffs = top3ElvesCalories();
        log.info(elfStuffs.head());
        log.info(elfStuffs.map(ElfStuff::getCalories).sum());
    }

    private static List<ElfStuff> top3ElvesCalories() {
        URL day1P1URL = Resources.getResource("input/d1-p1");
        return Try.of(() -> Resources.toString(day1P1URL, StandardCharsets.UTF_8))
                .map(s -> Vector.of(s.split("\n"))
                        .foldLeft(List.<ElfStuff>empty(), (acc, line) -> line.isBlank() ?
                                acc.push(ElfStuff.of()) :
                                acc.tail().push(acc.head().addItem(parseInt(line))))
                        .sortBy(ElfStuff::getCalories)
                        .takeRight(3)
                        .reverse())
                .onFailure(log::error)
                .get();
    }
}
