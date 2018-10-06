package src;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;


public class AnnotateText {

    static final String REST_URL = "http://data.bioontology.org";
    static final String API_KEY = "ec511abb-8761-41a6-a094-e6f931afa672";
    static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String urlParameters;
        JsonNode annotations;
        String text = readFile("D:/Users/Gregory/Documents/GitHub/CS5560GregBrownLabs/Lab2/Bioportal_API/data/justMedWords.txt", Charset.defaultCharset());
        String textToAnnotate = URLEncoder.encode( text, "ISO-8859-1");
        System.out.println(textToAnnotate);
        // Get just annotations
        urlParameters = "text=" + textToAnnotate;
        annotations = jsonToNode(get(REST_URL + "/annotator?" + urlParameters));
        printAnnotations(annotations);
        System.out.println("Got here");
        // Annotations with hierarchy
        urlParameters = "max_level=3&text=" + textToAnnotate;
        annotations = jsonToNode(get(REST_URL + "/annotator?" + urlParameters));
        printAnnotations(annotations);

        // Annotations using POST (necessary for long text)
        urlParameters = "text=" + textToAnnotate;
        annotations = jsonToNode(post(REST_URL + "/annotator", urlParameters));
        printAnnotations(annotations);

        // Get labels, synonyms, and definitions with returned annotations
        urlParameters = "include=prefLabel,synonym,definition&text=" + textToAnnotate;
        annotations = jsonToNode(get(REST_URL + "/annotator?" + urlParameters));
        int i = 0;
        for (JsonNode annotation : annotations) {
            System.out.println(annotation.get("annotatedClass").get("prefLabel").asText());
            i++;
        }
        System.out.println("Num: \t" + i);
    }

    private static void printAnnotations(JsonNode annotations) {
        for (JsonNode annotation : annotations) {
            //try{TimeUnit.NANOSECONDS.sleep(1000000);System.out.println("Waited");}catch(InterruptedException e){e.printStackTrace();
            testWait();
            // Get the details for the class that was found in the annotation and print

            JsonNode classDetails = null;
            if(jsonToNode(get(annotation.get("annotatedClass").get("links").get("self").asText())) != null) {
                classDetails = jsonToNode(get(annotation.get("annotatedClass").get("links").get("self").asText()));
                System.out.println("Class details");
                System.out.println("\tid: " + classDetails.get("@id").asText());
                System.out.println("\tprefLabel: " + classDetails.get("prefLabel").asText());
                System.out.println("\tontology: " + classDetails.get("links").get("ontology").asText());
                System.out.println("\n");
            }else{System.out.println("Skipped: " + annotation.get("annotatedClass").get("links").get("self").asText());}
            JsonNode hierarchy = annotation.get("hierarchy");
            // If we have hierarchy annotations, print the related class information as well
            if (hierarchy.isArray() && hierarchy.elements().hasNext()) {
                System.out.println("\tHierarchy annotations");
                for (JsonNode hierarchyAnnotation : hierarchy) {

                    classDetails = jsonToNode(get(hierarchyAnnotation.get("annotatedClass").get("links").get("self").asText()));
                    System.out.println("\t\tClass details");
                    System.out.println("\t\t\tid: " + classDetails.get("@id").asText());
                    System.out.println("\t\t\tprefLabel: " + classDetails.get("prefLabel").asText());
                    System.out.println("\t\t\tontology: " + classDetails.get("links").get("ontology").asText());
                }
            }
        }
    }

    private static JsonNode jsonToNode(String json) {
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    private static String get(String urlToGet) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String post(String urlToGet, String urlParameters) {
        URL url;
        HttpURLConnection conn;

        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            conn.disconnect();

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void testWait(){
        final long INTERVAL = 100000000;
        long start = System.nanoTime();
        long end=0;
        do{
            end = System.nanoTime();
        }while(start + INTERVAL >= end);
        //System.out.println("Waited: " + (end - start));
    }

    //code from: https://stackoverflow.com/questions/326390/how-do-i-create-a-java-string-from-the-contents-of-a-file
    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
