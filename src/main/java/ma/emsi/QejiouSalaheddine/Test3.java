package ma.emsi.QejiouSalaheddine;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;  // ← Import corrigé
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;  // ← Import ajouté

import java.time.Duration;

public class Test3 {

    public static void main(String[] args) {

        // 1. Initialisation (Clé API)
        String geminiApiKey = System.getenv("GEMINI_KEY");
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.err.println("Erreur : GEMINI_KEY n'est pas définie.");
            return;
        }

        // 2. Créer le modèle d'Embedding
        EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName("embedding-001")
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofMillis(10000))
                .build();

        // 3. Définir les phrases à comparer
        String phraseSimilaire1 = "Le chat est assis sur le tapis.";
        String phraseSimilaire2 = "Un félin repose sur un paillasson.";
        String phraseDifferente = "J'aime le football.";

        // 4. Générer les embeddings (vecteurs) pour chaque phrase
        System.out.println("Génération des embeddings...");
        Response<Embedding> reponseEmbedding1 = embeddingModel.embed(phraseSimilaire1);
        Response<Embedding> reponseEmbedding2 = embeddingModel.embed(phraseSimilaire2);
        Response<Embedding> reponseEmbedding3 = embeddingModel.embed(phraseDifferente);

        Embedding embedding1 = reponseEmbedding1.content();
        Embedding embedding2 = reponseEmbedding2.content();
        Embedding embedding3 = reponseEmbedding3.content();

        // 5. Calculer la similarité (cosinus)
        double similariteEntre1et2 = CosineSimilarity.between(embedding1, embedding2);
        double similariteEntre1et3 = CosineSimilarity.between(embedding1, embedding3);

        // 6. Afficher les résultats
        System.out.println("================= RÉSULTATS DE SIMILARITÉ =================");
        System.out.printf("Similarité 1 vs 2 (similaires) : %.4f\n", similariteEntre1et2);
        System.out.printf("Similarité 1 vs 3 (différentes) : %.4f\n", similariteEntre1et3);
        System.out.println("==========================================================");
    }
}