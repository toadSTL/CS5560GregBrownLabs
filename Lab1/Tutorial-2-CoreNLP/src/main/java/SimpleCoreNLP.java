/**
 * Created by Mayanka on 14-Jun-16.
 */
import edu.stanford.nlp.simple.*;

public class SimpleCoreNLP {
    public static void main(String[] args) {
        // Create a document. No computation is done yet.
        Document doc = new Document("People are at higher risk for malignancy as they get older or have a strong family history of cancer. This study aims to collect family history of cancer in a large cohort of patients with pituitary adenomas (PA) in outpatient clinic from years 2005-2017. Overall, 46.6% of 1062 patients with PA had a family member affected with cancer. Breast cancer in family members was reported in 15.3% of patients with prolactinomas which was significantly higher than in families of patients with non-functioning pituitary adenomas (NFPA) (10.0%) or acromegaly (6.8%) (p   =   0.004). Lung cancer in family members was reported in 12.1% of patients with prolactinomas, significantly higher than in families of NFPA patients (7.0%, p   =   0.049). Colorectal cancer in relatives of patients with PA was reported with any type of PA. Furthermore, patients with a positive family history of malignancy were diagnosed with PA at an earlier age than patients with a negative family history (43.6        15.9 vs 46.0        16.4  years, p   =   0.015). Female patients with prolactinoma are more commonly diagnosed before the age of 25  years. Forty-two percent of patients with PA diagnosed before the age of 25  years had a second- and third-degree relative with cancer, significantly higher than patients with PA diagnosed later in life (25.8%, p   <   0.001). Breast, lung, and colon cancers in second- and third-degree relatives were reported in significantly higher proportion of patients with PA diagnosed before the age of 25  years, compared with patients with PA diagnosed later in life (breast cancer: 10.9 vs 6.1%, p   =   0.033; lung cancer: 10.9 vs 5.8%, p   =   0.02; colon cancer: 9.5 vs 4.0%, p   =   0.004). These results suggest familial cancer clustering in patients with prolactinoma and young patients with PA (younger than 25  years at diagnosis of PA). In particular, there is a strong association between prolactinoma and family history of breast and lung cancers. Further research of possible shared genetic susceptibility of prolactinoma and breast and lung cancers is needed.");
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            // We're only asking for words -- no need to load any models yet
            System.out.println("The second word of the sentence '" + sent + "' is " + sent.word(1));
            // When we ask for the lemma, it will load and run the part of speech tagger
            System.out.println("The third lemma of the sentence '" + sent + "' is " + sent.lemma(2));
            // When we ask for the parse, it will load and run the parser
            System.out.println("The parse of the sentence '" + sent + "' is " + sent.parse());
            // ...
            System.out.println("The triplet extraction of the sentence '" + sent + "' is " + sent.openie());
        }
    }
}