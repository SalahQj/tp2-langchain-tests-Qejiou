package ma.emsi.QejiouSalaheddine;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.HashMap;
import java.util.Map;

public class Test2 {

    public static void main(String[] args) {

        // 1. Initialisation (comme pour le Test 1)
        String geminiApiKey = System.getenv("GEMINI_KEY");
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.err.println("Erreur : GEMINI_KEY n'est pas définie.");
            return;
        }

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-2.5-flash") // Le modèle qui fonctionne pour vous
                .temperature(0.3) // Température basse pour une traduction précise
                .logRequests(true)
                .logResponses(true)
                .build();

        // 2. Créer le PromptTemplate (le "modèle" de prompt)
        // La variable est entourée de {{ }}
        String templateString = "Traduis le texte suivant en anglais : {{texte_a_traduire}}";
        PromptTemplate promptTemplate = PromptTemplate.from(templateString);

        // 3. Définir la valeur de la variable
        // On crée une "Map" pour stocker nos variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("texte_a_traduire", "Bonjour, je suis un étudiant en ingénierie informatique.");

        // 4. Appliquer les variables au template pour créer le Prompt final
        Prompt promptFinal = promptTemplate.apply(variables);

        // Affiche le prompt final juste pour voir
        System.out.println("Prompt final envoyé au LLM : " + promptFinal.text());

        // 5. Envoyer le Prompt final au modèle
        System.out.println("Envoi du prompt de traduction au LLM...");
        ChatResponse reponse = model.chat(
                UserMessage.from(promptFinal.text())
        );

        // 6. Afficher la réponse
        System.out.println("================= RÉPONSE (Traduction) ==================");
        System.out.println(reponse.aiMessage().text());
        System.out.println("=========================================================");
    }
}