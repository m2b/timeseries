import java.net.URI;
import java.net.URISyntaxException;

public class URISandbox {
    public static void main(String[] args) throws URISyntaxException
    {
        URI phdtag=new URI("phd://hostoroip:5000/tagname");
        URI pitag=new URI("pi://hostoroip:1000/tagname");
        URI bad=new URI("bad");
        System.out.println(bad);
    }
}
