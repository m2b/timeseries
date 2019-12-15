import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamsSandbox {

    static class Person {  // Static so it can be accessed by Main
        String name;
        int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args)
    {
        List<Integer> listNumbers = Arrays.asList(3, 9, 1, 4, 7, 2, 5, 3, 8, 9, 1, 3, 8, 6);
        System.out.println(listNumbers);
        Set<Integer> uniqueNumbers = new HashSet<>(listNumbers);
        System.out.println(uniqueNumbers);
        listNumbers
                .stream()
                .filter(i->i<5)
                .sorted()
                .forEach(System.out::println);
        uniqueNumbers
                .stream()
                .map(i->square(i))
                .sorted((i1, i2) -> Integer.compare(i2,i1))
                .forEach(System.out::println);
       String joined=listNumbers
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
       System.out.println(joined);

        List<Person> persons =
                Arrays.asList(
                        new Person("Max", 18),
                        new Person("Peter", 23),
                        new Person("Pamela", 24),
                        new Person("David", 12));

        // Streams cannot be reused
        Stream<String> stream =
                Stream.of("d2", "a2", "b1", "b3", "c");

        stream.anyMatch(s->s.startsWith(("b")));    // ok
        try {
            stream.noneMatch(s->s.startsWith(("b")));   // exception
        }
        catch(IllegalStateException e)
        {
            e.printStackTrace();
        }

        //  How to reuse them using get() and supplier
        Supplier<Stream<String>> supplier =
                ()-> Stream.of("d2", "a2", "b1", "b3", "c");
        System.out.println(supplier.get().anyMatch(s->s.startsWith(("b"))));
        System.out.println(supplier.get().noneMatch(s->s.startsWith("b")));


        /* Reduce examples */
        // Binary operator overload
        persons
                .stream()
                .reduce((p1, p2) -> p1.age > p2.age ? p1 : p2)
                .ifPresent(System.out::println);    // Pamela
        // Identity and binary operator overload
        Person result =
                persons
                        .stream()
                        .reduce(new Person("", 0), (p1, p2) -> {
                            p1.age += p2.age;
                            p1.name += p2.name;
                            return p1;
                        });
        System.out.format("name=%s; age=%s%n", result.name, result.age);
        // identity bifunction and binary operator overload with parallel stream - AWSOME
        Integer ageSum = persons
                .parallelStream()
                .reduce(0,
                        (sum, p) -> {
                            System.out.format("accumulator: sum=%s; person=%s%n", sum, p);
                            return sum += p.age;
                        },
                        (sum1, sum2) -> {
                            System.out.format("combiner: sum1=%s; sum2=%s%n", sum1, sum2);
                            return sum1 + sum2;
                        });
        System.out.format("total age=%s%n",ageSum);
    }

    static int square(int i)
    {
        return i*i;
    }
}
