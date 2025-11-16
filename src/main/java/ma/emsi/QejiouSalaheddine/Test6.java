package ma.emsi.QejiouSalaheddine;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import java.util.Scanner;

public class Test6 {

    public static void main(String[] args) {

        String geminiApiKey = System.getenv("GEMINI_KEY");
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.err.println("Erreur : GEMINI_KEY n'est pas définie.");
            return;
        }

        // 1. Créer le ChatModel
        // IMPORTANT : On active les logs comme demandé dans le TP
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash") // Votre modèle qui fonctionne
                .logRequests(true)  // <-- ACTIVÉ (demandé par le TP)
                .logResponses(true) // <-- ACTIVÉ (demandé par le TP)
                .build();

        // 2. Créer l'assistant en lui donnant l'outil Météo
        AssistantMeteo assistant = AiServices.builder(AssistantMeteo.class)
                .chatModel(model)
                .tools(new MeteoTool())  // <-- On ajoute l'outil !
                .build();

        // 3. Lancer la boucle de conversation
        System.out.println("Assistant Météo prêt. (tapez 'fin' pour quitter)");
        conversationAvec(assistant);
    }

    /**
     * Gère la boucle de conversation avec l'assistant.
     * (Code fourni par le TP)
     */
    private static void conversationAvec(AssistantMeteo assistant) {
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
                // L'assistant va décider s'il doit utiliser l'outil
                String reponse = assistant.chat(question);
                System.out.println("Assistant : " + reponse);
            }
        }
    }
}