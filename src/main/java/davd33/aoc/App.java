package davd33.aoc;

import com.google.common.io.Resources;
import davd33.aoc.domain.ElfStuff;
import io.vavr.Function2;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static io.vavr.API.*;
import static io.vavr.Patterns.$Tuple2;
import static java.lang.Integer.parseInt;

@Log4j2
public class App {

    public static void main(String[] args) {
        runDay3();
    }

    private static void runDay3() {
        final Map<Character, Integer> priorities = Vector.ofAll("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                        .toCharArray())
                .zipWithIndex().toMap(t -> Tuple.of(t._1, t._2 + 1));

        URL day2URL = Resources.getResource("input/d3");
        Try<String> inputTry = Try.of(() -> Resources.toString(day2URL, StandardCharsets.UTF_8));

        inputTry.map(s -> Vector.of(s.split("\n"))).map(lines ->
                        lines.map(line ->
                                Vector.ofAll(line.toCharArray()).zipWithIndex().sortBy(t -> t._2)
                                        .takeUntil(charIndexTuple -> charIndexTuple._2 == (line.length() / 2))
                                        .map(t -> t._1))
                        .zipWith(lines.map(line ->
                                        Vector.ofAll(line.toCharArray()).zipWithIndex().sortBy(t -> t._2)
                                                .dropUntil(charIndexTuple -> charIndexTuple._2 == (line.length() / 2))
                                                .map(t -> t._1)),
                                Tuple2::new)
                                .flatMap(firstSecondRucksack -> firstSecondRucksack._1
                                        .filter(firstSecondRucksack._2::contains)
                                        .distinct()
                                        .map(priorities::get)
                                        .filter(Option::isDefined)
                                        .map(Option::get))
                                .sum())
                .forEach(log::info);

        inputTry.map(input -> input.split("\n"))
                .map(lines -> Vector.ofAll(Vector.of(lines).grouped(3))
                        .map(elfGroup -> elfGroup.map(elfSack -> Vector.ofAll(elfSack.toCharArray()).toSet())
                                .fold(Set(), (acc, set) -> acc.isEmpty() ? set : set.intersect(acc))
                                .map(priorities::get)
                                .filter(Option::isDefined)
                                .map(Option::get)
                                .sum())
                        .sum())
                .forEach(log::info);
    }

    private static void runDay2() {
        final String OPPONENT_ROCK = "A";
        final String OPPONENT_PAPER = "B";
        final String OPPONENT_SCISSOR = "C";
        final String ROCK = "X";
        final String PAPER = "Y";
        final String SCISSOR = "Z";
        final int LOOSE = 0;
        final int DRAW = 3;
        final int WIN = 6;

        Function2<String, String, Integer> roundScore = (opponentShape, selectedShape) ->
                Match(Tuple.of(opponentShape, selectedShape)).of(
                        Case($Tuple2($(OPPONENT_ROCK), $(ROCK)), DRAW),
                        Case($Tuple2($(OPPONENT_ROCK), $(PAPER)), WIN),
                        Case($Tuple2($(OPPONENT_ROCK), $(SCISSOR)), LOOSE),

                        Case($Tuple2($(OPPONENT_PAPER), $(ROCK)), LOOSE),
                        Case($Tuple2($(OPPONENT_PAPER), $(PAPER)), DRAW),
                        Case($Tuple2($(OPPONENT_PAPER), $(SCISSOR)), WIN),

                        Case($Tuple2($(OPPONENT_SCISSOR), $(ROCK)), WIN),
                        Case($Tuple2($(OPPONENT_SCISSOR), $(PAPER)), LOOSE),
                        Case($Tuple2($(OPPONENT_SCISSOR), $(SCISSOR)), DRAW));

        URL day2URL = Resources.getResource("input/d2");
        Try<String> inputTry = Try.of(() -> Resources.toString(day2URL, StandardCharsets.UTF_8));

        // part 1
        inputTry
                .map(input -> Vector.of(input.split("\n"))
                        .map(l -> l.split(" ")).map(shapes ->
                                Match(shapes[1]).of(
                                        Case($(ROCK), 1),
                                        Case($(PAPER), 2),
                                        Case($(SCISSOR), 3))
                                        +
                                        roundScore.apply(shapes[0], shapes[1]))
                        .sum().intValue())
                .forEach(score -> log.info("---RESULT = {}---", score));

        // part 2
        inputTry
                .map(input -> Vector.of(input.split("\n"))
                        .map(l -> l.split(" "))
                        .map(shapes -> Option.of(Match(shapes[1]).of(
                                        Case($(ROCK /*need loose*/), Match(shapes[0]).of(
                                                Case($(OPPONENT_ROCK), SCISSOR),
                                                Case($(OPPONENT_PAPER), ROCK),
                                                Case($(OPPONENT_SCISSOR), PAPER))),
                                        Case($(PAPER /*need draw*/), Match(shapes[0]).of(
                                                Case($(OPPONENT_ROCK), ROCK),
                                                Case($(OPPONENT_PAPER), PAPER),
                                                Case($(OPPONENT_SCISSOR), SCISSOR))),
                                        Case($(SCISSOR /*need win*/), Match(shapes[0]).of(
                                                Case($(OPPONENT_ROCK), PAPER),
                                                Case($(OPPONENT_PAPER), SCISSOR),
                                                Case($(OPPONENT_SCISSOR), ROCK)))))
                                .map(myNewShape -> Match(myNewShape).of(
                                        Case($(ROCK), 1),
                                        Case($(PAPER), 2),
                                        Case($(SCISSOR), 3))
                                        +
                                        roundScore.apply(shapes[0], myNewShape))
                                .get())
                        .sum().intValue())
                .forEach(score -> log.info("---RESULT PART 2 = {}---", score));

    }

    private static void runDay1() {
        List<ElfStuff> elfStuffs = top3ElvesCalories();
        log.info(elfStuffs.head());
        log.info(elfStuffs.map(ElfStuff::getCalories).sum());
    }

    private static List<ElfStuff> top3ElvesCalories() {
        URL day1URL = Resources.getResource("input/d1");
        return Try.of(() -> Resources.toString(day1URL, StandardCharsets.UTF_8))
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
