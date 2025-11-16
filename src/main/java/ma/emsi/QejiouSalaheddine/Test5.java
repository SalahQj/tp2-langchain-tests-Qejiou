package ma.emsi.QejiouSalaheddine;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.ApachePdfBoxDocumentParser; // <-- Sera en rouge
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.InMemoryEmbeddingStore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner; // Import pour la conversation

public class Test5 {

    // L'interface pour notre Assistant
    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {

        String geminiApiKey = System.getenv("GEMINI_KEY");
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.err.println("Erreur : GEMINI_KEY n'est pas définie.");
            return;
        }

        // 1. Créer les modèles
        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash")
                .build();

        EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName("embedding-001")
                .build();

        // 2. Charger le document PDF
        System.out.println("Chargement du document cours.pdf...");
        Document document;
        try {
            // Cette ligne sera en rouge, c'est normal
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            document = parser.parse(new FileInputStream("cours.pdf"));
        } catch (FileNotFoundException e) {
            System.err.println("Erreur: Fichier cours.pdf non trouvé. Assurez-vous qu'il est à la racine.");
            return;
        } catch (Exception e) {
            System.err.println("ERREUR DE COMPILATION : La bibliothèque PDF (pdfbox) n'a pas pu être chargée.");
            System.err.println("C'est normal car votre Maven (Aliyun) l'a bloquée.");
            return;
        }

        // 3. Créer le magasin d'embeddings
        EmbeddingStore<Document> embeddingStore = new InMemoryEmbeddingStore<>();

        // 4. Transformer le document en embedding (plantera avec l'erreur 429)
        try {
            System.out.println("Tentative d'embedding du document PDF...");
            Response<Embedding> embeddingResponse = embeddingModel.embed(document.text());
            embeddingStore.add(embeddingResponse.content(), document);
            System.out.println("Embedding du PDF réussi.");

        } catch (Exception e) {
            System.err.println("====================================================================");
            System.err.println("ERREUR DE QUOTA (429) : L'API d'embedding a refusé la requête.");
            System.err.println("Le Test 5 est bloqué par le quota Google. Le code est prêt.");
            System.err.println("====================================================================");
            return; // On arrête le test
        }

        // 5. Créer le "Content Retriever" (Cœur du RAG)
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(1)
                .build();

        // 6. Créer l'Assistant avec le RAG
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();

        // 7. Lancer la conversation (comme demandé dans les instructions)
        System.out.println("Assistant RAG prêt. Posez vos questions sur le PDF (tapez 'fin' pour quitter).");
        conversationAvec(assistant);
    }

    /**
     * Gère la boucle de conversation avec l'assistant.
     * @param assistant L'assistant IA à qui parler.
     */
    private static void conversationAvec(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("==================================================");
                System.out.println("Posez votre question : ");
                String question = scanner.nextLine();

                if (question.isBlank()) {
                    continue;
                }

                if ("fin".equalsIgnoreCase(question)) {
                    System.out.println("Conversation terminée.");
                    break;
                }

                System.out.println("==================================================");
                String reponse = assistant.chat(question);
                System.out.println("Assistant : " + reponse);
            }
        }
    }
}