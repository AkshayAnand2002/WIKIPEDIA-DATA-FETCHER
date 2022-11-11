import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikipediaDownloader implements Runnable{
    private String keyword;
    //private String result;
    public WikipediaDownloader(){

    }
    public WikipediaDownloader(String keyword){
        this.keyword=keyword;
    }
    public void run(){
        //1)get clean keyword.
        //2)get url for wikipedia
        //3)make a get request to wikipedia
        //4)parsing the useful results using jsoup.
        //showing results.
        if(this.keyword == null || this.keyword.length() == 0){
            return;
        }
        //get clean keyword
        this.keyword=this.keyword.trim().replaceAll("[ ]+","_");//empty or blank spaces replaced by _.
        //get url for wikipedia
        String wikiUrl= getWikipediaUrlForQuery(this.keyword);
        String response="";
        String imageUrl="";
        try {
            //make a get request to wikipedia
            String wikipediaResponse = HttpURLConnectionExample.sendGet(wikiUrl);
            //System.out.println(wikipediaResponse);
            //parsing the useful results using jsoup.
            Document document = Jsoup.parse(wikipediaResponse,"https://en.wikipedia.org");
            Elements childElements=document.body().select(".mw-parser-output > *");
            int state=0;
            for(Element childElement : childElements)
            {
                if(state==0)
                {
                    if(childElement.tagName().equals("table"))
                    {
                        state=1;
                    }
                }
                else if(state==1)
                {
                    if(childElement.tagName().equals("p"))
                    {
                        state=2;
                        response = childElement.text();
                    }
                }
            }
            try{
                imageUrl=document.body().select(".infobox.biography.vcard img").get(0).attr("src");
            }catch(Exception ex){
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //this.result=response;
        WikiResult wikiresult = new WikiResult(this.keyword,response,imageUrl);
        //we will only print in json format.
        System.out.println(new Gson().toJson(wikiresult));
    }

    private String getWikipediaUrlForQuery(String cleanKeyword)
    {
        return "https://en.wikipedia.org/wiki/"+ cleanKeyword;
    }

    public static void main(String[] args) {
        TaskManager taskmanager = new TaskManager(20);
        WikipediaDownloader wikipediadownloader= new WikipediaDownloader("Albert Einstein");
        taskmanager.waitTillQueueIsFreeAndAddTask(wikipediadownloader);
    }
}
