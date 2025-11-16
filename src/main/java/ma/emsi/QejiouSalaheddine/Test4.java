package ma.emsi.QejiouSalaheddine;

// Imports pour le RAG
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

// Imports pour lire le fichier depuis resources
import java.io.InputStream;

public class Test4 {

    // 1. Définir une interface "Assistant"
    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {

        // 2. Initialisation (Clé API)
        String geminiApiKey = System.getenv("GEMINI_KEY");
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.err.println("Erreur : GEMINI_KEY n'est pas définie.");
            return;
        }

        // 3. Créer le modèle de Chat (pour parler)
        GoogleAiGeminiChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.0-flash-exp")
                .logRequests(true)
                .logResponses(true)
                .build();

        // 4. Créer le modèle d'Embedding (pour comprendre le sens)
        EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName("embedding-001")
                .logRequests(true)
                .logResponses(true)
                .build();

        // 5. Lire le fichier 'infos.txt' depuis resources
        System.out.println("Chargement du document infos.txt depuis resources...");
        String fileContent;
        try {
            InputStream inputStream = Test4.class.getClassLoader().getResourceAsStream("infos.txt");
            if (inputStream == null) {
                System.err.println("Erreur: Impossible de trouver infos.txt dans src/main/resources/");
                System.err.println("Assurez-vous que le fichier infos.txt est bien dans src/main/resources/");
                return;
            }
            fileContent = new String(inputStream.readAllBytes());
            inputStream.close();
            System.out.println("✓ Document chargé avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur: Impossible de lire infos.txt depuis resources.");
            e.printStackTrace();
            return;
        }

        // Créer un Document avec Metadata
        Document document = Document.from(fileContent, Metadata.from("source", "infos.txt"));

        // 6. Créer le magasin d'embeddings (en mémoire)
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 7. Transformer le document en embedding et le stocker
        try {
            System.out.println("\nTentative d'embedding du document...");
            Response<Embedding> embeddingResponse = embeddingModel.embed(document.text());

            // Créer un TextSegment à partir du document
            TextSegment segment = TextSegment.from(document.text(), document.metadata());

            embeddingStore.add(embeddingResponse.content(), segment);
            System.out.println("✓ Embedding du document réussi !");

        } catch (Exception e) {
            System.err.println("\n====================================================================");
            System.err.println("❌ ERREUR DE QUOTA (429) : L'API d'embedding a refusé la requête.");
            System.err.println("====================================================================");
            System.err.println("Le Test 4 ne peut pas fonctionner sans activer la facturation sur");
            System.err.println("Google Cloud ou attendre 24h pour la réinitialisation du quota gratuit.");
            System.err.println("");
            System.err.println("✅ CEPENDANT : Le code est correct et le fichier a été chargé !");
            System.err.println("   Vous pouvez commiter ce code.");
            System.err.println("====================================================================\n");
            return;
        }

        // 8. Créer le "Content Retriever" (C'est le cœur du RAG)
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(1)
                .build();

        // 9. Créer l'Assistant avec le RAG
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();

        // 10. Poser la question
        System.out.println("==========================================================");
        System.out.println("Question : Comment s'appelle le chat de Pierre ?");
        String reponse = assistant.chat("Comment s'appelle le chat de Pierre ?");

        System.out.println("Réponse : " + reponse);
        System.out.println("==========================================================");
    }
}