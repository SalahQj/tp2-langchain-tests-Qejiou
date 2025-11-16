package ma.emsi.QejiouSalaheddine;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.data.message.UserMessage;

public class Test1 {

    public static void main(String[] args) {

        System.out.println("Début du Test 1... Vérification de la clé API.");
        String geminiApiKey = System.getenv("GEMINI_KEY");

        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.err.println("ERREUR : La variable d'environnement GEMINI_KEY n'est pas définie.");
            return;
        }
        System.out.println("Clé API trouvée.");

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash")   // <-- MODIFICATION 1 (Correction de l'erreur 404)
                .temperature(0.7)
                .logRequests(true)
                .logResponses(true)
                .build();

        System.out.println("Envoi de la question au LLM...");
        String question = "Bonjour, quel est l'objectif principal de Jakarta EE ?";

        ChatResponse response = model.chat(
                UserMessage.from(question)
        );

        System.out.println("================= RÉPONSE DU LLM ==================");
        System.out.println(response.aiMessage().text());
        System.out.println("===================================================");
    }
}