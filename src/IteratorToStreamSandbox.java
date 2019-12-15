import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class IteratorToStreamSandbox {

    static <T> Stream<T> iteratorToStream(final Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), true);
    }

    public static void main(String[] args) {
        // Test objects
        Iterator<String> sourceIterator = Arrays.asList("A", "B", "C", "D", "E", "F", "G").iterator();

        // Where to collect them all
        Stream<String> outer = Stream.empty();

        // Create iterator and streams that process/convert
        Iterable<String> iterable = () -> sourceIterator;
        Stream<String> uppercase= StreamSupport.stream(iterable.spliterator(), true);
        Stream<String> lowercase= uppercase.map(s->s.toLowerCase());
        // Collect in a stream of lists
        Stream<List<String>> collectionStream=lowercase.map(lc->
        {
            List<String> lcs=new ArrayList<>();
            lcs.add(lc);
            return lcs;
        });
        outer=collectionStream.flatMap(coll -> coll.stream());

        for(String s:outer.collect(Collectors.toList()))
            System.out.println(s);
    }

}
