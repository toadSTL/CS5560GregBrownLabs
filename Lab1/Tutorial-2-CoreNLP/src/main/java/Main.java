import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Created by Mayanka on 13-Jun-16.
 * Updated by Greg Brown on 10-Sep-18.
 */
public class Main {
    public static void main(String args[]) {
        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
// LOAD TEXT OF ABSTRACTS HERE TO PERFORM CORE NLP
// read some text in the text variable
        String text = "BACKGROUND: Management of locally-advanced breast cancer is determined by multiple factors, but in patients without distant metastases often involves neoadjuvant systemic therapy, surgery and radiation. If the primary tumour remains unresectable following systemic therapy, radiotherapy may be used for tumour shrinkage prior to surgery. When metastatic disease is present, locoregional radiotherapy is generally reserved for management of tumour-related symptoms. We reviewed our experience of high-dose radiotherapy for unresected locally-advanced breast cancer. METHODS: A retrospective chart review was conducted of patients with unresected locally advanced breast cancer (LABC) receiving external beam radiotherapy to the breast, chest wall and/or regional lymph nodes. Patients were stratified based on the presence of metastatic disease at presentation. Patient demographics, disease characteristics, and treatment outcomes were recorded. RESULTS: Forty-three cases were analyzed between 2004 and 2016. Median follow-up was 25 months from diagnosis and 14 months from completion of radiotherapy. There were 24 cases (56%) with metastatic disease on presentation, and 19 (44%) without. Tumour shrinkage occurred within 3 months of completing radiotherapy in 36 cases (84%). Ulceration and bleeding improved following radiotherapy in 13 (54%) of the 24 applicable cases. Twenty-six patients (60%) developed moist desquamation but none experienced grade 4 or 5 radiation dermatitis. Median locoregional progression-free survival for all patients was 12 months from completion of radiotherapy. Locoregional progression-free survival (P=0.2) and overall survival (OS) (P=0.4) were not significantly different between patients with and without distant metastases at presentation. CONCLUSIONS: Radiotherapy provided good response and symptom control in most patients in this study; there is a role for palliative radiotherapy in patients with LABC."; // Add your text here!

        int i = 0;
        int n = 0, nn = 0, nns = 0;
        String nStr = "", nnStr = "", nnsStr = "";
        int v = 0, vb = 0, vbd = 0, vbp = 0, vbz = 0, vbn = 0;
        String vStr = "", vbStr = "", vbdStr = "", vbpStr = "", vbzStr = "", vbnStr = "";
        int in = 0, to = 0, ex = 0;
        String inStr = "", toStr = "", exStr = "";
        int dt = 0, wdt = 0;
        String dtStr = "", wdtStr = "";
        int cc = 0, cd = 0;
        String ccStr = "", cdStr = "";
        int jj = 0, jjr = 0;
        String jjStr = "", jjrStr = "";
        int rb = 0, rbr = 0;
        String rbStr = "", rbrStr = "";
        int prp = 0;
        String prpStr = "", otherStr = "";
        int other = 0;
        int ner = 0;
        String nerStr = "";

// create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

// run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                i++;
                System.out.println("\n" + token);

                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                System.out.println("Text Annotation");
                System.out.println(token + "\t" + word);
                // this is the POS tag of the token

                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                System.out.println("Lemma Annotation");
                System.out.println(token + "\t" + lemma);
                // this is the Lemmatized tag of the token


                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                System.out.println("POS");
                System.out.println(token + "\t" + pos);

                if(pos.equals("NN")||pos.equals("NNS")){
                    n++;
                    nStr += lemma + "\t";
                    if(pos.equals("NN")){
                        nn++;
                        nnStr += word + "\t";
                    }
                    if(pos.equals("NNS")){
                        nns++;
                        nnsStr += word + "\t";
                    }
                }else if(pos.equals("VB")||pos.equals("VBD")||pos.equals("VBP")||pos.equals("VBZ")||pos.equals("VBN")){
                    v++;
                    vStr += lemma + "\t";
                    if(pos.equals("VB")){
                        vb++;
                        vbStr += word + "\t";
                    }
                    if(pos.equals("VBD")){
                        vbd++;
                        vbdStr += word + "\t";
                    }
                    if(pos.equals("VBP")){
                        vbp++;
                        vbpStr += word + "\t";
                    }
                    if(pos.equals("VBZ")){
                        vbz++;
                        vbzStr += word + "\t";
                    }
                    if(pos.equals("VBN")){
                        vbn++;
                        vbnStr += word + "\t";
                    }
                }else if(pos.equals("IN")){
                    in++;
                    inStr += word + "\t";
                }else if(pos.equals("TO")){
                    to++;
                    toStr += word + "\t";
                }else if(pos.equals("EX")){
                    ex++;
                    exStr += word + "\t";
                }else if(pos.equals("DT")){
                    dt++;
                    dtStr += word + "\t";
                }else if(pos.equals("WDT")){
                    wdt++;
                    wdtStr += word + "\t";
                }else if(pos.equals("CC")){
                    cc++;
                    ccStr += word + "\t";
                }else if(pos.equals("CD")){
                    cd++;
                    cdStr += word + "\t";
                }else if(pos.equals("JJ")){
                    jj++;
                    jjStr += word + "\t";
                }else if(pos.equals("JJR")){
                    jjr++;
                    jjrStr += word + "\t";
                }else if(pos.equals("RB")){
                    rb++;
                    rbStr += word + "\t";
                }else if(pos.equals("RBR")){
                    rbr++;
                    rbrStr += word + "\t";
                }else if(pos.equals("PRP")){
                    prp++;
                    prpStr += word + "\t";
                }else{
                    other++;
                    otherStr += word + "\t";
                }

                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                System.out.println("NER");
                System.out.println(token + "\t" + ne);
                if(!ne.equals("O")){
                    ner++;
                    nerStr += word + "-" + ne + "\t";
                }


                System.out.println("\n\nWord Count: " + i + "\n" +
                        "Total Nouns: \t" + n + "\n" +
                        "Total Verbs: \t" + v + "\n" +
                        "NN: \t" + nn + "\n" +
                        "NNS: \t" + nns + "\n" +
                        "VB: \t" + vb + "\n" +
                        "VBD: \t" + vbd + "\n" +
                        "VBP: \t" + vbp + "\n" +
                        "VBZ: \t" + vbz + "\n" +
                        "VBN: \t" + vbn + "\n" +
                        "IN: \t" + in + "\n" +
                        "TO: \t" + to + "\n" +
                        "EX: \t" + ex + "\n" +
                        "DT: \t" + dt + "\n" +
                        "WDT: \t" + wdt + "\n" +
                        "CC: \t" + cc + "\n" +
                        "CD: \t" + cd + "\n" +
                        "JJ: \t" + jj + "\n" +
                        "JJR: \t" + jjr + "\n" +
                        "RB: \t" + rb + "\n" +
                        "RBR: \t" + rbr + "\n" +
                        "PRP: \t" + prp + "\n" +
                        "other: \t" + other + "\n" +
                        "NER: \t" + ner + "\n" +
                        "\n\n");
            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            System.out.println(tree);
            // this is the Stanford dependency graph of the current sentence


            System.out.println("Verbs: \t" + vStr);
            System.out.println("Nouns: \t" + nStr);
            System.out.println("NN: \t" + nnStr);
            System.out.println("NNS: \t" + nnsStr);
            System.out.println("VB: \t" + vbStr);
            System.out.println("VBD: \t" + vbdStr);
            System.out.println("VBP: \t" + vbpStr);
            System.out.println("VBZ: \t" + vbzStr);
            System.out.println("VBN: \t" + vbnStr);
            System.out.println("IN: \t" + inStr);
            System.out.println("TO: \t" + toStr);
            System.out.println("EX: \t" + exStr);
            System.out.println("DT: \t" + dtStr);
            System.out.println("WDT: \t" + wdtStr);
            System.out.println("CC: \t" + ccStr);
            System.out.println("CD: \t" + cdStr);
            System.out.println("JJ: \t" + jjStr);
            System.out.println("JJR: \t" + jjrStr);
            System.out.println("RB: \t" + rbStr);
            System.out.println("RBR: \t" + rbrStr);
            System.out.println("PRP: \t" + prpStr);
            System.out.println("OTHER: \t" + otherStr);
            System.out.println("Name Entity Recognition: \t" + nerStr);




            Map<Integer, CorefChain> graph =
                    document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
            System.out.println(graph.values().toString());
        }

    }
}
