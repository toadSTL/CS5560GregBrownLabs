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
import java.util.concurrent.TimeUnit;

public class AnnotateText {

    static final String REST_URL = "http://data.bioontology.org";
    static final String API_KEY = "ec511abb-8761-41a6-a094-e6f931afa672";
    static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String urlParameters;
        JsonNode annotations;
        String textToAnnotate = URLEncoder.encode("BACKGROUND: Management of locally-advanced breast cancer is determined by multiple factors, but in patients without distant metastases often involves neoadjuvant systemic therapy, surgery and radiation. If the primary tumour remains unresectable following systemic therapy, radiotherapy may be used for tumour shrinkage prior to surgery. When metastatic disease is present, locoregional radiotherapy is generally reserved for management of tumour-related symptoms. We reviewed our experience of high-dose radiotherapy for unresected locally-advanced breast cancer. METHODS: A retrospective chart review was conducted of patients with unresected locally advanced breast cancer (LABC) receiving external beam radiotherapy to the breast, chest wall and/or regional lymph nodes. Patients were stratified based on the presence of metastatic disease at presentation. Patient demographics, disease characteristics, and treatment outcomes were recorded. RESULTS: Forty-three cases were analyzed between 2004 and 2016. Median follow-up was 25 months from diagnosis and 14 months from completion of radiotherapy. There were 24 cases (56%) with metastatic disease on presentation, and 19 (44%) without. Tumour shrinkage occurred within 3 months of completing radiotherapy in 36 cases (84%). Ulceration and bleeding improved following radiotherapy in 13 (54%) of the 24 applicable cases. Twenty-six patients (60%) developed moist desquamation but none experienced grade 4 or 5 radiation dermatitis. Median locoregional progression-free survival for all patients was 12 months from completion of radiotherapy. Locoregional progression-free survival (P=0.2) and overall survival (OS) (P=0.4) were not significantly different between patients with and without distant metastases at presentation. CONCLUSIONS: Radiotherapy provided good response and symptom control in most patients in this study; there is a role for palliative radiotherapy in patients with LABC.", "ISO-8859-1");
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

}
